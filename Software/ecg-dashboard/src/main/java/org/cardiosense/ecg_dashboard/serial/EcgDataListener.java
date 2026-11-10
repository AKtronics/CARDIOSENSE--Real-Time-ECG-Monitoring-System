package org.cardiosense.ecg_dashboard.serial;

import org.cardiosense.ecg_dashboard.model.EcgTelemetry;
import java.util.ArrayList;
import java.util.List;

public class EcgDataListener {

    /**
     * Parses the ESP32 UART string formatted with `|` and `,` delimiters.
     * Handles packets containing multiple comma-separated voltages.
     */
    public static List<EcgTelemetry> parseData(String dataString) {
        if (dataString == null || dataString.trim().isEmpty()) return null;
        
        try {
            String[] parts = dataString.split("\\|");
            if (parts.length < 1) return null;
            
            // The first part is a comma-separated list of voltages
            String[] voltagesStr = parts[0].split(",");
            List<EcgTelemetry> telemetryList = new ArrayList<>();
            
            long packetTimestamp = System.currentTimeMillis();
            int packetId = 0;
            int samplingRate = 0;

            if (parts.length > 2) {
                String[] metadata = parts[2].split(",");
                if (metadata.length > 0) packetTimestamp = Long.parseLong(metadata[0].trim());
                if (metadata.length > 1) packetId = Integer.parseInt(metadata[1].trim());
                if (metadata.length > 2) samplingRate = Integer.parseInt(metadata[2].trim());
            }

            int timeSpreadMs = (samplingRate > 0) ? (1000 / samplingRate) : 4;
            
            // To ensure the graph draws a continuous line without overlapping on the exact same X coordinate,
            // we artificially spread the batched voltages across time.
            for (int i = 0; i < voltagesStr.length; i++) {
                double voltage = Double.parseDouble(voltagesStr[i].trim());
                EcgTelemetry telemetry = new EcgTelemetry(packetTimestamp + (long)i * timeSpreadMs, voltage);
                
                if (parts.length > 1) {
                    String[] oled = parts[1].split(",");
                    if (oled.length >= 2) {
                        telemetry.bpm = Integer.parseInt(oled[0].trim());
                        telemetry.sqi = oled[1].trim();
                    }
                }
                
                telemetry.packetId = packetId;
                telemetry.samplingRate = samplingRate;
                
                telemetryList.add(telemetry);
            }
            
            return telemetryList;
        } catch (Exception e) {
            // Silently ignore parse errors for corrupt frames
            return null;
        }
    }
}
