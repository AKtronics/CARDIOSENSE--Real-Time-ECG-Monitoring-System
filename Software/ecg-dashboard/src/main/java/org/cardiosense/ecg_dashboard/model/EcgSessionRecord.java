package org.cardiosense.ecg_dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class EcgSessionRecord {
    public String sessionId;
    public long startTimeMs;
    public long endTimeMs;
    public int finalBpm = 0;
    public List<EcgTelemetry> telemetryData;
    
    public EcgSessionRecord(String sessionId, long startTimeMs) {
        this.sessionId = sessionId;
        this.startTimeMs = startTimeMs;
        this.telemetryData = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EcgSessionRecord that = (EcgSessionRecord) o;
        return sessionId != null ? sessionId.equals(that.sessionId) : that.sessionId == null;
    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}
