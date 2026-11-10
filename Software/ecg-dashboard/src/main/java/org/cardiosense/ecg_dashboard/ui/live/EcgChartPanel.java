package org.cardiosense.ecg_dashboard.ui.live;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class EcgChartPanel extends JPanel {
    private XYSeries ecgSeries;
    private JScrollBar scrollBar;
    private ValueAxis domainAxis;

    private double windowSize = 5.0; // 5 seconds visible
    private boolean isAutoScrolling = true;

    // Scale factor for JScrollBar (which uses ints)
    private static final int SCROLL_SCALE = 100;

    private boolean isUpdatingProgrammatically = false;
    private volatile double latestTime = 0.0;
    private Timer renderTimer;

    public EcgChartPanel() {
        setLayout(new BorderLayout());

        ecgSeries = new XYSeries("ECG Signal");
        // Removed setMaximumItemCount to keep all historical data for scrolling
        XYSeriesCollection dataset = new XYSeriesCollection(ecgSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Real-Time ECG Data",
                "Time (s)",
                "Amplitude (mV)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(0xffffe5));
        plot.setDomainGridlinePaint(Color.RED);
        plot.setRangeGridlinePaint(Color.RED);
        plot.getRenderer().setSeriesPaint(0, Color.BLACK);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));

        domainAxis = plot.getDomainAxis();
        domainAxis.setAutoRange(false); // We manage range manually
        domainAxis.setRange(0.0, windowSize);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(-1.0, 2.0);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(false); // Disable JFreeChart default zoom
        chartPanel.setPopupMenu(null); // Disable right-click popup menu
        chartPanel.setDomainZoomable(false); // Disable drag-to-zoom on X axis
        chartPanel.setRangeZoomable(false); // Disable drag-to-zoom on Y axis

        // Custom Mouse Wheel Listener for horizontal panning
        chartPanel.addMouseWheelListener(e -> {
            if (scrollBar.isVisible()) {
                int scrollAmount = e.getWheelRotation() * (SCROLL_SCALE / 2); // 0.5s per notch
                int newValue = scrollBar.getValue() + scrollAmount;
                scrollBar.setValue(newValue);
            }
        });

        add(chartPanel, BorderLayout.CENTER);

        // Add Horizontal Scrollbar
        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        // Make it always visible to prevent the graph from resizing when it appears,
        // but disable it initially
        scrollBar.setVisible(true);
        scrollBar.setEnabled(false);

        scrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!isUpdatingProgrammatically && !e.getValueIsAdjusting()) {
                    // Check if we are at the right edge (with a tiny 5 unit tolerance)
                    isAutoScrolling = (scrollBar.getMaximum()
                            - (scrollBar.getValue() + scrollBar.getVisibleAmount()) <= 5);
                }

                double startTime = (double) scrollBar.getValue() / SCROLL_SCALE;
                domainAxis.setRange(startTime, startTime + windowSize);
            }
        });

        add(scrollBar, BorderLayout.SOUTH);

        renderTimer = new Timer(33, e -> {
            if (!ecgSeries.isEmpty()) {
                ecgSeries.fireSeriesChanged();
                updateScrollBounds(latestTime);
            }
        });
        renderTimer.start();
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> {
            ecgSeries.clear();
            domainAxis.setRange(0.0, windowSize);
            scrollBar.setValue(0);
            scrollBar.setEnabled(false);
            isAutoScrolling = true;
        });
    }

    public void addDataPoint(double time, double voltage) {
        latestTime = time;
        SwingUtilities.invokeLater(() -> {
            ecgSeries.add(time, voltage, false);
        });
    }

    public void loadBulkData(java.util.List<org.cardiosense.ecg_dashboard.model.EcgTelemetry> telemetryData, long startTimeMs) {
        SwingUtilities.invokeLater(() -> {
            ecgSeries.clear();
            for (int i = 0; i < telemetryData.size(); i++) {
                org.cardiosense.ecg_dashboard.model.EcgTelemetry data = telemetryData.get(i);
                double timeSec = (data.timestampMs - startTimeMs) / 1000.0;
                boolean isLast = (i == telemetryData.size() - 1);
                ecgSeries.add(timeSec, data.voltage, isLast);
                latestTime = timeSec;
            }
            updateScrollBounds(latestTime);
            // Snap to the beginning for historical data
            scrollBar.setValue(0);
            domainAxis.setRange(0.0, windowSize);
            isAutoScrolling = false;
            
            ecgSeries.fireSeriesChanged();
        });
    }

    private void updateScrollBounds(double latestTime) {
        if (latestTime <= windowSize) {
            return; // Still fits in initial window
        }

        if (!scrollBar.isEnabled()) {
            scrollBar.setEnabled(true);
        }

        int maxVal = (int) (latestTime * SCROLL_SCALE);
        int extent = (int) (windowSize * SCROLL_SCALE);

        isUpdatingProgrammatically = true;

        scrollBar.setMaximum(maxVal);
        scrollBar.setVisibleAmount(extent);

        if (isAutoScrolling) {
            scrollBar.setValue(maxVal - extent);
        }

        isUpdatingProgrammatically = false;
    }
}
