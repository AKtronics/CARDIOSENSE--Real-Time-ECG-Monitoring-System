package org.cardiosense.ecg_dashboard.ui.records;

import org.cardiosense.ecg_dashboard.dsp.EcgAnalyzer;
import org.cardiosense.ecg_dashboard.model.EcgSessionRecord;
import org.cardiosense.ecg_dashboard.model.EcgTelemetry;
import org.cardiosense.ecg_dashboard.ui.components.RoundedButton;
import org.cardiosense.ecg_dashboard.ui.live.EcgChartPanel;
import org.cardiosense.ecg_dashboard.ui.live.MetricsContainerPanel;

import javax.swing.*;
import java.awt.*;

public class RecordDetailPanel extends JPanel {
    private EcgChartPanel chartPanel;
    private MetricsContainerPanel metricsPanel;
    private Runnable onBack;
    private Runnable onDelete;
    private EcgSessionRecord currentSession;

    public RecordDetailPanel(Runnable onBack, Runnable onDelete) {
        this.onBack = onBack;
        this.onDelete = onDelete;
        setLayout(new BorderLayout());

        // Main content
        chartPanel = new EcgChartPanel();
        metricsPanel = new MetricsContainerPanel();
        
        // Bottom Control Panel
        JPanel bottomControls = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomControls.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        RoundedButton backBtn = new RoundedButton("BACK", 13);
        backBtn.setBackground(new Color(33, 150, 243)); // Material Blue
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setPreferredSize(new Dimension(0, 35));
        backBtn.addActionListener(e -> {
            if (this.onBack != null) this.onBack.run();
        });

        RoundedButton deleteBtn = new RoundedButton("DELETE", 13);
        deleteBtn.setBackground(new Color(239, 83, 80)); // Red
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setPreferredSize(new Dimension(0, 35));
        deleteBtn.addActionListener(e -> {
            if (this.onDelete != null) this.onDelete.run();
        });
        
        bottomControls.add(backBtn);
        bottomControls.add(deleteBtn);
        
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(bottomControls, BorderLayout.NORTH);
        bottomContainer.add(metricsPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, bottomContainer);
        splitPane.setResizeWeight(0.95);
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    public void loadSession(EcgSessionRecord session) {
        this.currentSession = session;
        chartPanel.clear();

        if (session.telemetryData == null || session.telemetryData.isEmpty()) {
            return;
        }

        // Plot data
        long startTime = session.telemetryData.get(0).timestampMs;
        chartPanel.loadBulkData(session.telemetryData, startTime);

        // Calculate metrics for the session in a background thread
        new Thread(() -> {
            EcgAnalyzer analyzer = new EcgAnalyzer(analyzedMetrics -> {
                SwingUtilities.invokeLater(() -> {
                    metricsPanel.getOledPanel().updateSqi(analyzedMetrics.sqi);
                    metricsPanel.getMorphologyPanel().updateRrInterval(analyzedMetrics.rrIntervalMs);
                    metricsPanel.getMorphologyPanel().updatePrInterval(analyzedMetrics.prIntervalMs);
                    metricsPanel.getMorphologyPanel().updateQrsDuration(analyzedMetrics.qrsDurationMs);
                    metricsPanel.getMorphologyPanel().updateQtInterval(analyzedMetrics.qtIntervalMs);
                    metricsPanel.getMorphologyPanel().updateQtc(analyzedMetrics.qtcMs);
                    metricsPanel.getMorphologyPanel().updateStLevel(analyzedMetrics.stLevelMv);
                    metricsPanel.getMorphologyPanel().updatePWave(analyzedMetrics.pWaveDetected);
                    metricsPanel.getMorphologyPanel().updateQrs(analyzedMetrics.qrsDetected);
                    metricsPanel.getMorphologyPanel().updateTWave(analyzedMetrics.tWaveDetected);
                });
            });

            // Feed data to analyzer
            for (EcgTelemetry data : session.telemetryData) {
                analyzer.process(data);
            }
        }).start();

        // Set fixed duration in UI
        double durationSec = (session.endTimeMs - session.startTimeMs) / 1000.0;
        int totalSeconds = (int) durationSec;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        metricsPanel.getOledPanel().updateTime(timeString);
        
        // Ensure the average BPM is displayed finally, instead of the last momentary BPM
        SwingUtilities.invokeLater(() -> {
            metricsPanel.getOledPanel().updateBpm(session.finalBpm);
            if (!session.telemetryData.isEmpty()) {
                metricsPanel.getOledPanel().updateSamplingRate(session.telemetryData.get(0).samplingRate);
            }
        });
    }

    public EcgSessionRecord getCurrentSession() {
        return currentSession;
    }
}
