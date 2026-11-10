package org.cardiosense.ecg_dashboard.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.cardiosense.ecg_dashboard.model.EcgTelemetry;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SerialManager {

    private volatile SerialPort activePort;
    private Consumer<EcgTelemetry> dataCallback;
    private Runnable disconnectCallback;
    private StringBuilder buffer = new StringBuilder();
    private java.util.Timer watchdogTimer;
    private volatile long lastReceiveTime;

    public void setDataCallback(Consumer<EcgTelemetry> dataCallback) {
        this.dataCallback = dataCallback;
    }

    public void setDisconnectCallback(Runnable disconnectCallback) {
        this.disconnectCallback = disconnectCallback;
    }

    public List<String> getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        List<String> portNames = new ArrayList<>();
        for (SerialPort port : ports) {
            portNames.add(port.getSystemPortName());
        }
        return portNames;
    }

    public boolean autoConnectAndStart(int baudRate) {
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0)
            return false;

        System.out.println("Starting auto-discovery for ESP32SPP...");

        for (SerialPort port : ports) {
            String desc = port.getDescriptivePortName().toLowerCase();
            String portDesc = port.getPortDescription().toLowerCase();

            // Filter strictly for the ESP32SPP descriptive name
            if (desc.contains("esp32spp") || portDesc.contains("esp32spp")) {
                System.out.println("Found matching ESP32SPP port: " + port.getSystemPortName());
                if (tryPort(port, baudRate)) {
                    System.out.println("Auto-discovery successful! Locked onto: " + activePort.getSystemPortName());
                    return true;
                }
            }
        }

        System.out.println("Auto-discovery finished. Device not found.");
        return false;
    }

    private boolean tryPort(SerialPort port, int baudRate) {
        String name = port.getSystemPortName();
        System.out.println("Trying port: " + name);
        try {
            if (port.openPort()) {
                port.setBaudRate(baudRate);

                OutputStream out = port.getOutputStream();
                Thread.sleep(500); // Bluetooth Settle Time

                CountDownLatch latch = new CountDownLatch(1);
                boolean[] success = { false };

                // 1. Setup the bulletproof event listener for the PING handshake
                SerialPortDataListener handshakeListener = new SerialPortDataListener() {
                    @Override
                    public int getListeningEvents() {
                        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                    }

                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                            return;

                        byte[] newData = new byte[port.bytesAvailable()];
                        int numRead = port.readBytes(newData, newData.length);

                        if (numRead > 0) {
                            String received = new String(newData, 0, numRead, StandardCharsets.UTF_8);
                            if (received.contains("ESP32_READY")) {
                                System.out.println("\n[ESP32 says]: ESP32_READY");
                                success[0] = true;
                                latch.countDown();
                            }
                        }
                    }
                };

                port.addDataListener(handshakeListener);

                // 2. Send the PING command over the OutputStream
                String messageToSend = "PING\n";
                out.write(messageToSend.getBytes(StandardCharsets.UTF_8));
                out.flush();
                System.out.println("Sent: " + messageToSend.trim());

                // Wait up to 5 seconds for the latch to be triggered by ESP32_READY
                latch.await(5, TimeUnit.SECONDS);

                port.removeDataListener(); // Clean up the handshake listener

                if (success[0]) {
                    return setActivePortAndStart(port);
                } else {
                    System.out.println("  -> " + name + " rejected PING or timed out.");
                    port.closePort();
                }
            } else {
                System.out.println("  -> " + name + " failed to open (possibly in use).");
            }
        } catch (Exception e) {
            System.out.println("  -> " + name + " failed: " + e.getMessage());
            port.removeDataListener();
            if (port.isOpen())
                port.closePort();
        }
        return false;
    }

    private synchronized boolean setActivePortAndStart(SerialPort port) {
        if (activePort == null) {
            activePort = port;
            setupDataListener();
            sendCommand("START");
            lastReceiveTime = System.currentTimeMillis();
            startWatchdog();
            return true;
        }
        return false;
    }

    private void startWatchdog() {
        stopWatchdog();
        watchdogTimer = new java.util.Timer("SerialWatchdog", true);
        watchdogTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (activePort != null && activePort.isOpen()) {
                    if (System.currentTimeMillis() - lastReceiveTime > 1000) {
                        if (disconnectCallback != null) {
                            disconnectCallback.run();
                        }
                        disconnect();
                    }
                }
            }
        }, 1000, 500);
    }

    private void stopWatchdog() {
        if (watchdogTimer != null) {
            watchdogTimer.cancel();
            watchdogTimer = null;
        }
    }

    private void setupDataListener() {
        // Set up the main data listener for the continuous streaming
        activePort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                    if (disconnectCallback != null) {
                        disconnectCallback.run();
                    }
                    return;
                }

                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;

                byte[] newData = new byte[activePort.bytesAvailable()];
                int numRead = activePort.readBytes(newData, newData.length);

                if (numRead > 0) {
                    lastReceiveTime = System.currentTimeMillis();
                    String text = new String(newData, 0, numRead, StandardCharsets.UTF_8);
                    buffer.append(text);

                    int newlineIdx;
                    while ((newlineIdx = buffer.indexOf("\n")) != -1) {
                        String line = buffer.substring(0, newlineIdx).trim();
                        buffer.delete(0, newlineIdx + 1);

                        if (line.equals("EOF") || line.isEmpty()) {
                            continue;
                        }

                        if (dataCallback != null) {
                            List<EcgTelemetry> batch = EcgDataListener.parseData(line);
                            if (batch == null || batch.isEmpty()) {
                                System.out.println("[PARSE ERROR] Failed to parse: " + line);
                            } else {
                                for (EcgTelemetry data : batch) {
                                    dataCallback.accept(data);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void disconnect() {
        stopWatchdog();
        if (activePort != null && activePort.isOpen()) {
            sendCommand("STOP");
            activePort.removeDataListener();
            activePort.closePort();
            activePort = null;
        }
    }

    public void sendCommand(String command) {
        if (activePort != null && activePort.isOpen()) {
            try {
                OutputStream out = activePort.getOutputStream();
                byte[] bytes = (command + "\n").getBytes(StandardCharsets.UTF_8);
                out.write(bytes);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
