package org.cardiosense.ecg_dashboard.ui.live;

import javax.swing.*;
import java.awt.*;

public class LiveDashboardPanel extends JPanel {
    private EcgChartPanel chartPanel;
    private ControlPanel controlPanel;
    private MetricsContainerPanel metricsPanel;

    public LiveDashboardPanel() {
        setLayout(new BorderLayout());

        chartPanel = new EcgChartPanel();
        controlPanel = new ControlPanel();
        metricsPanel = new MetricsContainerPanel();

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(metricsPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, bottomPanel);
        splitPane.setResizeWeight(0.95); // 95% of vertical space goes to the chart
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    public EcgChartPanel getChartPanel() {
        return chartPanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public MetricsContainerPanel getMetricsPanel() {
        return metricsPanel;
    }
}
