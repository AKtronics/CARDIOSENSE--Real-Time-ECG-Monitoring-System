package org.cardiosense.ecg_dashboard.ui;

import org.cardiosense.ecg_dashboard.ui.components.SidebarPanel;
import org.cardiosense.ecg_dashboard.ui.live.LiveDashboardPanel;
import org.cardiosense.ecg_dashboard.ui.records.RecordExplorerPanel;
import org.cardiosense.ecg_dashboard.ui.records.TrashPanel;
import org.cardiosense.ecg_dashboard.ui.components.AnimatedStatusLine;
import org.cardiosense.ecg_dashboard.util.DashboardConstants;
import org.cardiosense.ecg_dashboard.serial.SerialManager;
import org.cardiosense.ecg_dashboard.dsp.EcgAnalyzer;
import org.cardiosense.ecg_dashboard.storage.StorageManager;
import org.cardiosense.ecg_dashboard.model.EcgSessionRecord;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    private SidebarPanel sidebar;
    private JPanel workspace;
    private CardLayout cardLayout;

    private LiveDashboardPanel liveDashboard;
    private RecordExplorerPanel recordExplorer;
    private TrashPanel trashPanel;

    private SerialManager serialManager;
    private EcgAnalyzer ecgAnalyzer;
    private StorageManager storageManager;
    private EcgSessionRecord currentSession;
    private long sessionStartTime = 0;
    private boolean isFirstDataPoint = true;
    private long bpmSum = 0;
    private int bpmCount = 0;

    public MainFrame() {
        setTitle("CardioSense");
        setSize(DashboardConstants.UI_WIDTH, DashboardConstants.UI_HEIGHT);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open maximized by default

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(screenSize.width / 2, screenSize.height / 2));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        sidebar = new SidebarPanel();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        workspace = new JPanel(cardLayout);

        liveDashboard = new LiveDashboardPanel();
        storageManager = new StorageManager();
        recordExplorer = new RecordExplorerPanel(storageManager);
        trashPanel = new TrashPanel(storageManager);

        workspace.add(liveDashboard, "LIVE");
        workspace.add(recordExplorer, "RECORDS");
        workspace.add(trashPanel, "TRASH");

        AnimatedStatusLine statusLine = new AnimatedStatusLine();

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(statusLine, BorderLayout.NORTH);
        centerContainer.add(workspace, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        serialManager = new SerialManager();
        serialManager.setDisconnectCallback(() -> {
            SwingUtilities.invokeLater(() -> {
                statusLine.setRecordingState(false);
                liveDashboard.getControlPanel().getStartBtn().setText("RESTART");
                liveDashboard.getControlPanel().getStartBtn().setBackground(new java.awt.Color(66, 165, 245));
                liveDashboard.getControlPanel().getStartBtn().setEnabled(true);
                
                liveDashboard.getControlPanel().getStopBtn().setText("SAVE");
                liveDashboard.getControlPanel().getStopBtn().setBackground(new java.awt.Color(171, 71, 188)); // Purple
                liveDashboard.getControlPanel().getStopBtn().setEnabled(true);
                
                if (currentSession != null) {
                    currentSession.endTimeMs = System.currentTimeMillis();
                    int avgBpm = bpmCount > 0 ? (int) (bpmSum / bpmCount) : 0;
                    currentSession.finalBpm = avgBpm;
                    liveDashboard.getMetricsPanel().getOledPanel().updateBpm(avgBpm);
                }
                showUnfocusedMessageDialog("Device disconnected unexpectedly.", "Disconnected", JOptionPane.WARNING_MESSAGE);
            });
        });
        serialManager.setDataCallback(telemetry -> {
            if (currentSession != null) {
                currentSession.telemetryData.add(telemetry);
            }
            // Update graph on UI thread
            SwingUtilities.invokeLater(() -> {
                if (isFirstDataPoint) {
                    sessionStartTime = telemetry.timestampMs;
                    isFirstDataPoint = false;
                }
                
                double timeSec = (telemetry.timestampMs - sessionStartTime) / 1000.0;
                liveDashboard.getChartPanel().addDataPoint(timeSec, telemetry.voltage);
                
                // Update time string and sampling rate
                liveDashboard.getMetricsPanel().getOledPanel().updateSamplingRate(telemetry.samplingRate);
                int totalSeconds = (int) timeSec;
                int hours = totalSeconds / 3600;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;
                String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                liveDashboard.getMetricsPanel().getOledPanel().updateTime(timeString);
                
                // Directly update from hardware packet if available
                if (telemetry.bpm > 0) {
                    bpmSum += telemetry.bpm;
                    bpmCount++;
                    liveDashboard.getMetricsPanel().getOledPanel().updateBpm(telemetry.bpm);
                    if (telemetry.sqi != null && !telemetry.sqi.equals("UNKNOWN")) {
                        liveDashboard.getMetricsPanel().getOledPanel().updateSqi(telemetry.sqi);
                    }
                }
            });
            
            // Process DSP in the background
            if (ecgAnalyzer != null) {
                ecgAnalyzer.process(telemetry);
            }
        });

        liveDashboard.getControlPanel().getStopBtn().setEnabled(false);

        liveDashboard.getControlPanel().getStartBtn().addActionListener(e -> {
            liveDashboard.getControlPanel().getStartBtn().setEnabled(false);
            liveDashboard.getControlPanel().getStopBtn().setEnabled(false);
            
            String sessionId = "ecg_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            currentSession = new EcgSessionRecord(sessionId, System.currentTimeMillis());
            currentSession.telemetryData = Collections.synchronizedList(new ArrayList<>());
            bpmSum = 0;
            bpmCount = 0;
            
            // Clean up the graph for a fresh start
            isFirstDataPoint = true;
            liveDashboard.getChartPanel().clear();
            
            // Instantiate a fresh analyzer for the new session
            ecgAnalyzer = new EcgAnalyzer(analyzedMetrics -> {
                SwingUtilities.invokeLater(() -> {
                    // Only use DSP bpm if hardware didn't send one
                    if (analyzedMetrics.bpm > 0 && bpmCount == 0) {
                        liveDashboard.getMetricsPanel().getOledPanel().updateBpm(analyzedMetrics.bpm);
                        bpmSum += analyzedMetrics.bpm;
                        bpmCount++;
                    }
                    if (analyzedMetrics.sqi != null && !analyzedMetrics.sqi.equals("UNKNOWN") && bpmCount == 0) {
                        liveDashboard.getMetricsPanel().getOledPanel().updateSqi(analyzedMetrics.sqi);
                    }
                    
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updateRrInterval(analyzedMetrics.rrIntervalMs);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updatePrInterval(analyzedMetrics.prIntervalMs);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updateQrsDuration(analyzedMetrics.qrsDurationMs);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updateQtInterval(analyzedMetrics.qtIntervalMs);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updateQtc(analyzedMetrics.qtcMs);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updateStLevel(analyzedMetrics.stLevelMv);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updatePWave(analyzedMetrics.pWaveDetected);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updateQrs(analyzedMetrics.qrsDetected);
                    liveDashboard.getMetricsPanel().getMorphologyPanel().updateTWave(analyzedMetrics.tWaveDetected);
                });
            });

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return serialManager.autoConnectAndStart(DashboardConstants.BAUD_RATE);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            statusLine.setRecordingState(true);
                            liveDashboard.getControlPanel().getStopBtn().setText("STOP");
                            liveDashboard.getControlPanel().getStopBtn().setBackground(new java.awt.Color(239, 83, 80)); // Red
                            liveDashboard.getControlPanel().getStopBtn().setEnabled(true);
                        } else {
                            showUnfocusedMessageDialog("Could not find CardioSense device. Please ensure it is paired in Windows Bluetooth settings.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                            liveDashboard.getControlPanel().getStartBtn().setEnabled(true);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        liveDashboard.getControlPanel().getStartBtn().setEnabled(true);
                    }
                }
            }.execute();
        });

        liveDashboard.getControlPanel().getStopBtn().addActionListener(e -> {
            String text = liveDashboard.getControlPanel().getStopBtn().getText();
            
            if (text.equals("STOP")) {
                statusLine.setRecordingState(false);
                serialManager.disconnect();
                liveDashboard.getControlPanel().getStartBtn().setText("RESTART");
                liveDashboard.getControlPanel().getStartBtn().setBackground(new java.awt.Color(66, 165, 245));
                liveDashboard.getControlPanel().getStartBtn().setEnabled(true);
                
                liveDashboard.getControlPanel().getStopBtn().setText("SAVE");
                liveDashboard.getControlPanel().getStopBtn().setBackground(new java.awt.Color(171, 71, 188)); // Purple
                
                if (currentSession != null) {
                    currentSession.endTimeMs = System.currentTimeMillis();
                    int avgBpm = bpmCount > 0 ? (int) (bpmSum / bpmCount) : 0;
                    currentSession.finalBpm = avgBpm;
                    liveDashboard.getMetricsPanel().getOledPanel().updateBpm(avgBpm);
                }
            } else if (text.equals("SAVE")) {
                if (currentSession != null) {
                    storageManager.saveSession(currentSession);
                    showUnfocusedMessageDialog("Recording saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                    currentSession = null;
                    liveDashboard.getControlPanel().getStopBtn().setEnabled(false);
                }
            }
        });

        // Navigation wiring
        sidebar.getLiveMonitorBtn().addActionListener(e -> {
            cardLayout.show(workspace, "LIVE");
            sidebar.setActiveTab(sidebar.getLiveMonitorBtn());
        });
        sidebar.getPastRecordsBtn().addActionListener(e -> {
            recordExplorer.loadRecords();
            cardLayout.show(workspace, "RECORDS");
            sidebar.setActiveTab(sidebar.getPastRecordsBtn());
        });
        sidebar.getTrashBtn().addActionListener(e -> {
            trashPanel.loadRecords();
            cardLayout.show(workspace, "TRASH");
            sidebar.setActiveTab(sidebar.getTrashBtn());
        });

        // Default view
        cardLayout.show(workspace, "LIVE");
        sidebar.setActiveTab(sidebar.getLiveMonitorBtn());
    }

    private void showUnfocusedMessageDialog(String message, String title, int messageType) {
        JOptionPane pane = new JOptionPane(message, messageType);
        removeFocus(pane);
        JDialog dialog = pane.createDialog(this, title);
        dialog.setVisible(true);
    }

    private void removeFocus(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).setFocusable(false);
                ((JButton) comp).setFocusPainted(false);
            } else if (comp instanceof Container) {
                removeFocus((Container) comp);
            }
        }
    }
}
