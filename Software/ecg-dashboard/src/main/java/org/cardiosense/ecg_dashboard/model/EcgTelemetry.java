package org.cardiosense.ecg_dashboard.model;

public class EcgTelemetry {
    public long timestampMs;
    public double voltage;
    
    // OLED Data
    public int bpm = 0;
    public String sqi = "UNKNOWN";
    public int packetId = 0;
    public int samplingRate = 0;
    
    // Morphology Data
    public int rrIntervalMs = 0;
    public int prIntervalMs = 0;
    public int qrsDurationMs = 0;
    public int qtIntervalMs = 0;
    public int qtcMs = 0;
    public double stLevelMv = 0.0;
    public boolean pWaveDetected = false;
    public boolean qrsDetected = false;
    public boolean tWaveDetected = false;
    
    public EcgTelemetry(long timestampMs, double voltage) {
        this.timestampMs = timestampMs;
        this.voltage = voltage;
    }
}
