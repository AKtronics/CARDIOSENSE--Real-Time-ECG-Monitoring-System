package org.cardiosense.ecg_dashboard.ui.live;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class MorphologyPanel extends JPanel {
    private JLabel rrIntervalLabel = new JLabel("-- ms");
    private JLabel prIntervalLabel = new JLabel("-- ms");
    private JLabel qrsDurationLabel = new JLabel("-- ms");
    private JLabel qtIntervalLabel = new JLabel("-- ms");
    private JLabel qtcLabel = new JLabel("-- ms");
    private JLabel stLevelLabel = new JLabel("-- mV");
    private JLabel pWaveLabel = new JLabel("--");
    private JLabel qrsLabel = new JLabel("--");
    private JLabel tWaveLabel = new JLabel("--");

    public MorphologyPanel() {
        setLayout(new BorderLayout());
        TitledBorder border = BorderFactory.createTitledBorder("ESTIMATED ECG MORPHOLOGY");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 16));
        setBorder(border);

        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        
        addInfoRow(gridPanel, "RR Interval", rrIntervalLabel);
        addInfoRow(gridPanel, "PR Interval", prIntervalLabel);
        addInfoRow(gridPanel, "QRS Duration", qrsDurationLabel);
        addInfoRow(gridPanel, "QT Interval", qtIntervalLabel);
        addInfoRow(gridPanel, "QTc", qtcLabel);
        addInfoRow(gridPanel, "ST Level", stLevelLabel);
        addInfoRow(gridPanel, "P Wave", pWaveLabel);
        addInfoRow(gridPanel, "QRS", qrsLabel);
        addInfoRow(gridPanel, "T Wave", tWaveLabel);
        
        add(gridPanel, BorderLayout.CENTER);
        
        JLabel disclaimer = new JLabel("<html><i>* Educational prototype. Not for clinical diagnostic use.</i></html>");
        disclaimer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        disclaimer.setForeground(Color.GRAY);
        disclaimer.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        add(disclaimer, BorderLayout.SOUTH);
    }

    private void addInfoRow(JPanel panel, String labelText, JLabel valueLabel) {
        JLabel title = new JLabel(labelText);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        title.setForeground(Color.DARK_GRAY);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title);
        panel.add(valueLabel);
    }

    public void updateRrInterval(int rr) { rrIntervalLabel.setText(rr + " ms"); }
    public void updatePrInterval(int pr) { prIntervalLabel.setText(pr + " ms"); }
    public void updateQrsDuration(int qrs) { qrsDurationLabel.setText(qrs + " ms"); }
    public void updateQtInterval(int qt) { qtIntervalLabel.setText(qt + " ms"); }
    public void updateQtc(int qtc) { qtcLabel.setText(qtc + " ms"); }
    public void updateStLevel(double st) { stLevelLabel.setText(String.format("%.2f mV", st)); }
    public void updatePWave(boolean detected) { pWaveLabel.setText(detected ? "Detected" : "Not Detected"); }
    public void updateQrs(boolean detected) { qrsLabel.setText(detected ? "Detected" : "Not Detected"); }
    public void updateTWave(boolean detected) { tWaveLabel.setText(detected ? "Detected" : "Not Detected"); }
}
