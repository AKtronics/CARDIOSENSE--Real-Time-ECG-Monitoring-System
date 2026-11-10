package org.cardiosense.ecg_dashboard.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class ModernCheckBox extends JCheckBox {
    private Color activeColor = new Color(52, 152, 219); // Modern Blue
    private Color inactiveColor = new Color(200, 200, 200); // Light Gray
    private int iconSize = 20;

    public ModernCheckBox() {
        super();
        init();
    }

    public ModernCheckBox(String text) {
        super(text);
        init();
    }

    private void init() {
        setOpaque(false);
        setFocusable(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setIcon(new CustomIcon(false));
        setSelectedIcon(new CustomIcon(true));
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setIconTextGap(8); // Space between custom icon and text
    }

    public void setActiveColor(Color color) {
        this.activeColor = color;
        repaint();
    }

    private class CustomIcon implements Icon {
        private boolean selected;
        
        public CustomIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (!c.isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            }
            
            if (selected) {
                // Draw filled rounded rectangle
                g2.setColor(activeColor);
                g2.fill(new RoundRectangle2D.Float(x, y, iconSize, iconSize, 8, 8));
                
                // Draw white checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D.Float check = new Path2D.Float();
                check.moveTo(x + iconSize * 0.25f, y + iconSize * 0.5f);
                check.lineTo(x + iconSize * 0.45f, y + iconSize * 0.7f);
                check.lineTo(x + iconSize * 0.75f, y + iconSize * 0.3f);
                g2.draw(check);
            } else {
                // Draw hollow rounded rectangle
                g2.setColor(inactiveColor);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, iconSize - 2, iconSize - 2, 8, 8));
            }
            
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return iconSize;
        }

        @Override
        public int getIconHeight() {
            return iconSize;
        }
    }
}
