package org.cardiosense.ecg_dashboard.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimatedStatusLine extends JPanel {
    private float progress = 0.0f; // 0.0 to 1.0
    private Timer timer;
    private boolean isGreen = false;

    public AnimatedStatusLine() {
        setPreferredSize(new Dimension(0, 4)); // 4px thick line
        setBackground(new Color(220, 220, 220)); // Light grey base
    }

    public void setRecordingState(boolean recording) {
        if (recording == isGreen) return;
        isGreen = recording;
        
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        int delay = 16; // ~60 FPS

        timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float target = isGreen ? 1.0f : 0.0f;
                // Exponential decay for smooth ease-out (reaches ~99.5% in 1000ms)
                progress += (target - progress) * 0.08f;

                if (Math.abs(target - progress) < 0.005f) {
                    progress = target;
                    timer.stop();
                }
                repaint();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (progress > 0) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(102, 187, 106)); // Soft green from START button
            
            int width = getWidth();
            int height = getHeight();
            int rectWidth = (int) (width * progress);
            int x = (width - rectWidth) / 2; // Center outward
            
            g2.fillRect(x, 0, rectWidth, height);
        }
    }
}
