package org.cardiosense.ecg_dashboard.dsp;

import org.cardiosense.ecg_dashboard.model.EcgTelemetry;
import java.util.function.Consumer;

/**
 * Real-time Digital Signal Processing (DSP) engine for ECG streams.
 * Uses a sliding window and adaptive peak detection to delineate QRS, P, and T waves,
 * and extracts morphological parameters natively in Java.
 */
public class EcgAnalyzer {

    private static final int BUFFER_SIZE = 2048; // Holds about 8 seconds of data at 250Hz
    private double[] vBuf = new double[BUFFER_SIZE];
    private long[] tBuf = new long[BUFFER_SIZE];
    private int head = 0;
    private int count = 0;

    private long lastRPeakTime = 0;
    private long lastAnalyzedRTime = 0;
    private int pendingRPeakIdx = -1;
    
    // Adaptive thresholding
    private double recentMax = 0.0;
    
    private Consumer<EcgTelemetry> metricCallback;

    public EcgAnalyzer(Consumer<EcgTelemetry> callback) {
        this.metricCallback = callback;
    }

    public void process(EcgTelemetry pt) {
        int idx = head;
        vBuf[idx] = pt.voltage;
        tBuf[idx] = pt.timestampMs;
        count = Math.min(count + 1, BUFFER_SIZE);

        // Slowly decay the recentMax to adapt to changing signal amplitudes, but maintain a noise floor
        recentMax = recentMax * 0.999;
        if (pt.voltage > recentMax) {
            recentMax = pt.voltage;
        }

        // 1. R-Peak Detection (Requires 300ms refractory period and enough buffered data)
        if (pt.timestampMs - lastRPeakTime > 300 && count > 50) {
            // Look 5 samples behind so we can detect when a peak starts falling
            int checkIdx = getWrappedIndex(head, -5);
            double checkV = vBuf[checkIdx];
            
            // Dynamic threshold: 50% of recent max
            if (checkV > recentMax * 0.5 && checkV > 0.05) {
                boolean isMax = true;
                // Check if it's the absolute max in the local 15-sample neighborhood
                for (int i = -15; i <= 0; i++) {
                    if (vBuf[getWrappedIndex(head, i)] > checkV) {
                        isMax = false; 
                        break;
                    }
                }
                if (isMax) {
                    pendingRPeakIdx = checkIdx;
                    lastRPeakTime = tBuf[checkIdx];
                }
            }
        }

        // 2. Delayed Morphology Analysis
        // We wait 400ms after detecting the R-peak so the S and T waves have fully entered the buffer!
        if (pendingRPeakIdx != -1) {
            if (pt.timestampMs - tBuf[pendingRPeakIdx] > 400) {
                analyzeMorphology(pendingRPeakIdx);
                pendingRPeakIdx = -1;
            }
        }

        head = (head + 1) % BUFFER_SIZE;
    }

    private void analyzeMorphology(int rIdx) {
        long rTime = tBuf[rIdx];
        double rVolt = vBuf[rIdx];

        // Create a result object to hold our freshly calculated metrics
        EcgTelemetry result = new EcgTelemetry(rTime, rVolt);
        
        // Calculate HR and RR Interval
        if (lastAnalyzedRTime > 0) {
            long rr = rTime - lastAnalyzedRTime;
            if (rr > 200 && rr < 3000) { // Sanity check (20 BPM to 300 BPM)
                result.rrIntervalMs = (int) rr;
                result.bpm = (int) (60000 / rr);
            }
        }
        lastAnalyzedRTime = rTime;
        
        // Find Q-wave (Minimum within 80ms before R)
        int qIdx = findMinIndex(rIdx, -80);
        long qTime = tBuf[qIdx];
        
        // Find S-wave (Minimum within 80ms after R)
        int sIdx = findMinIndex(rIdx, 80);
        long sTime = tBuf[sIdx];
        
        // QRS Duration
        result.qrsDurationMs = (int)(sTime - qTime);
        if (result.qrsDurationMs < 0) result.qrsDurationMs = 0;
        
        // Find P-wave (Maximum within 200ms before Q)
        int pIdx = findMaxIndex(qIdx, -200);
        long pTime = tBuf[pIdx];
        
        // PR Interval
        result.prIntervalMs = (int)(rTime - pTime);
        if (result.prIntervalMs < 0) result.prIntervalMs = 0;
        
        // Find T-wave (Maximum within 400ms after S)
        int tIdx = findMaxIndex(sIdx, 400);
        long tTime = tBuf[tIdx];
        
        // Approximate T-end as T-peak + 60ms
        long tEndTime = tTime + 60;
        
        // QT Interval
        result.qtIntervalMs = (int)(tEndTime - qTime);
        if (result.qtIntervalMs < 0) result.qtIntervalMs = 0;
        
        // QTc (Bazett's Formula)
        if (result.rrIntervalMs > 0) {
            double rrSec = result.rrIntervalMs / 1000.0;
            result.qtcMs = (int)(result.qtIntervalMs / Math.sqrt(rrSec));
        }
        
        // ST Segment Level: voltage roughly 60ms after the S wave (J-point)
        int stIdx = findIndexByTimeOffset(sIdx, 60);
        result.stLevelMv = Math.round(vBuf[stIdx] * 1000.0) / 1000.0;
        
        // Signal Quality Index (Basic heuristic)
        if (result.bpm > 40 && result.bpm < 150 && result.qrsDurationMs < 200) {
            result.sqi = "GOOD";
        } else {
            result.sqi = "POOR";
        }
        
        // Populate Detection Flags
        result.qrsDetected = true; // Always true if analyzeMorphology was triggered by an R-peak
        result.pWaveDetected = (result.prIntervalMs > 80 && result.prIntervalMs < 300);
        result.tWaveDetected = (result.qtIntervalMs > 200 && result.qtIntervalMs < 600);
        
        // Fire callback to UI!
        if (metricCallback != null) {
            metricCallback.accept(result);
        }
    }

    // --- DSP Helper Methods ---

    private int findMinIndex(int startIdx, int timeWindowMs) {
        long startTime = tBuf[startIdx];
        int bestIdx = startIdx;
        double minV = vBuf[startIdx];
        
        int step = timeWindowMs > 0 ? 1 : -1;
        long maxDiff = Math.abs(timeWindowMs);
        
        int currIdx = startIdx;
        for (int i = 0; i < BUFFER_SIZE; i++) {
            currIdx = getWrappedIndex(currIdx, step);
            long diff = Math.abs(tBuf[currIdx] - startTime);
            if (diff > maxDiff || tBuf[currIdx] == 0) break; // End of window or array
            
            if (vBuf[currIdx] < minV) {
                minV = vBuf[currIdx];
                bestIdx = currIdx;
            }
        }
        return bestIdx;
    }

    private int findMaxIndex(int startIdx, int timeWindowMs) {
        long startTime = tBuf[startIdx];
        int bestIdx = startIdx;
        double maxV = vBuf[startIdx];
        
        int step = timeWindowMs > 0 ? 1 : -1;
        long maxDiff = Math.abs(timeWindowMs);
        
        int currIdx = startIdx;
        for (int i = 0; i < BUFFER_SIZE; i++) {
            currIdx = getWrappedIndex(currIdx, step);
            long diff = Math.abs(tBuf[currIdx] - startTime);
            if (diff > maxDiff || tBuf[currIdx] == 0) break;
            
            if (vBuf[currIdx] > maxV) {
                maxV = vBuf[currIdx];
                bestIdx = currIdx;
            }
        }
        return bestIdx;
    }

    private int findIndexByTimeOffset(int startIdx, int timeOffsetMs) {
        long targetTime = tBuf[startIdx] + timeOffsetMs;
        int step = timeOffsetMs > 0 ? 1 : -1;
        
        int currIdx = startIdx;
        for (int i = 0; i < BUFFER_SIZE; i++) {
            int nextIdx = getWrappedIndex(currIdx, step);
            if (tBuf[nextIdx] == 0) return currIdx;
            
            if (step > 0 && tBuf[nextIdx] >= targetTime) return nextIdx;
            if (step < 0 && tBuf[nextIdx] <= targetTime) return nextIdx;
            
            currIdx = nextIdx;
        }
        return currIdx;
    }

    private int getWrappedIndex(int idx, int offset) {
        int res = (idx + offset) % BUFFER_SIZE;
        if (res < 0) res += BUFFER_SIZE;
        return res;
    }
}
