package org.cardiosense.ecg_dashboard.ui.live;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class OledDataPanel extends JPanel {
    private JLabel bpmLabel = new JLabel("-- BPM");
    private JLabel sqiLabel = new JLabel("--");
    private JLabel sampleLabel = new JLabel("-- Hz");
    private JLabel timeLabel = new JLabel("00:00:00");

    public OledDataPanel() {
        setLayout(new GridLayout(0, 2, 10, 5));
        TitledBorder border = BorderFactory.createTitledBorder("OLED DATA");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 16));
        setBorder(border);
        
        addInfoRow("BPM", bpmLabel);
        addInfoRow("SQI", sqiLabel);
        addInfoRow("SAMPLE", sampleLabel);
        addInfoRow("TIME", timeLabel);
    }
    
    private void addInfoRow(String labelText, JLabel valueLabel) {
        JLabel title = new JLabel(labelText);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        title.setForeground(Color.DARK_GRAY);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(title);
        add(valueLabel);
    }
    
    public void updateBpm(int bpm) { bpmLabel.setText(bpm + " BPM"); }
    public void updateSqi(String sqi) { sqiLabel.setText(sqi); }
    public void updateSamplingRate(int rate) { sampleLabel.setText(rate + " Hz"); }
    public void updateTime(String time) { timeLabel.setText(time); }
}
