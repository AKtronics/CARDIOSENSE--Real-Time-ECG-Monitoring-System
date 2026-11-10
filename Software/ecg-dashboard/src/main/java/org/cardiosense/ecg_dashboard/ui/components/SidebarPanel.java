package org.cardiosense.ecg_dashboard.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarPanel extends JPanel {
    private JButton liveMonitorBtn;
    private JButton pastRecordsBtn;
    private JButton trashBtn;
    
    private final Color COLOR_DEFAULT = new Color(40, 44, 52);
    private final Color COLOR_HOVER = new Color(60, 68, 82);
    private final Color COLOR_ACTIVE = new Color(80, 92, 110);
    
    private JButton activeBtn = null;

    public SidebarPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(COLOR_DEFAULT); // VS Code dark theme sidebar color
        setPreferredSize(new Dimension(180, 0)); // Decreased width
        
        JLabel title = new JLabel("CardioSense");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Better font and size
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(25, 10, 30, 10));
        add(title);
        
        liveMonitorBtn = createMenuButton("Live Monitor");
        pastRecordsBtn = createMenuButton("Past Records");
        trashBtn = createMenuButton("Trash");
        
        add(liveMonitorBtn);
        add(pastRecordsBtn);
        add(trashBtn);
        
        add(Box.createVerticalGlue()); // Push buttons to top
    }
    
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setBackground(COLOR_DEFAULT);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Better font for buttons
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // Taller buttons, no gap between them
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) {
                    btn.setBackground(COLOR_HOVER);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) {
                    btn.setBackground(COLOR_DEFAULT);
                }
            }
        });
        
        return btn;
    }
    
    public void setActiveTab(JButton btn) {
        if (activeBtn != null) {
            activeBtn.setBackground(COLOR_DEFAULT);
            activeBtn.setForeground(Color.LIGHT_GRAY);
            activeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        }
        activeBtn = btn;
        if (activeBtn != null) {
            activeBtn.setBackground(COLOR_ACTIVE);
            activeBtn.setForeground(Color.WHITE);
            activeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        }
    }
    
    public JButton getLiveMonitorBtn() { return liveMonitorBtn; }
    public JButton getPastRecordsBtn() { return pastRecordsBtn; }
    public JButton getTrashBtn() { return trashBtn; }
}
