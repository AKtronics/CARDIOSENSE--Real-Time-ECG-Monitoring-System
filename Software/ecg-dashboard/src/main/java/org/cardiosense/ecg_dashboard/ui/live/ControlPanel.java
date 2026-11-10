package org.cardiosense.ecg_dashboard.ui.live;

import org.cardiosense.ecg_dashboard.ui.components.RoundedButton;
import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private RoundedButton startBtn;
    private RoundedButton stopBtn;

    public ControlPanel() {
        setLayout(new GridLayout(1, 2, 20, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        startBtn = new RoundedButton("START", 13);
        startBtn.setBackground(new Color(102, 187, 106)); // Lighter Green
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startBtn.setPreferredSize(new Dimension(0, 35));

        stopBtn = new RoundedButton("STOP", 13);
        stopBtn.setBackground(new Color(239, 83, 80)); // Lighter Red
        stopBtn.setForeground(Color.WHITE);
        stopBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stopBtn.setPreferredSize(new Dimension(0, 35));
        stopBtn.setEnabled(false); // Initially disabled

        add(startBtn);
        add(stopBtn);
    }

    public JButton getStartBtn() {
        return startBtn;
    }

    public JButton getStopBtn() {
        return stopBtn;
    }
}
