package org.cardiosense.ecg_dashboard.ui.components;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;

public class SvgIcons {
    public static Icon getTrashIcon(int size, Color color) {
        return new Icon() {
            private Shape shape;
            {
                Path2D.Float p = new Path2D.Float();
                p.moveTo(232.7, 69.9);
                p.curveTo(237.1, 56.8, 249.3, 48, 263.1, 48);
                p.lineTo(377, 48);
                p.curveTo(390.8, 48, 403, 56.8, 407.4, 69.9);
                p.lineTo(416, 96);
                p.lineTo(512, 96);
                p.curveTo(529.7, 96, 544, 110.3, 544, 128);
                p.curveTo(544, 145.7, 529.7, 160, 512, 160);
                p.lineTo(128, 160);
                p.curveTo(110.3, 160, 96, 145.7, 96, 128);
                p.curveTo(96, 110.3, 110.3, 96, 128, 96);
                p.lineTo(224, 96);
                p.lineTo(232.7, 69.9);
                p.closePath();

                p.moveTo(128, 208);
                p.lineTo(512, 208);
                p.lineTo(512, 512);
                p.curveTo(512, 547.3, 483.3, 576, 448, 576);
                p.lineTo(192, 576);
                p.curveTo(156.7, 576, 128, 547.3, 128, 512);
                p.lineTo(128, 208);
                p.closePath();

                p.moveTo(216, 272);
                p.curveTo(202.7, 272, 192, 282.7, 192, 296);
                p.lineTo(192, 488);
                p.curveTo(192, 501.3, 202.7, 512, 216, 512);
                p.curveTo(229.3, 512, 240, 501.3, 240, 488);
                p.lineTo(240, 296);
                p.curveTo(240, 282.7, 229.3, 272, 216, 272);
                p.closePath();

                p.moveTo(320, 272);
                p.curveTo(306.7, 272, 296, 282.7, 296, 296);
                p.lineTo(296, 488);
                p.curveTo(296, 501.3, 306.7, 512, 320, 512);
                p.curveTo(333.3, 512, 344, 501.3, 344, 488);
                p.lineTo(344, 296);
                p.curveTo(344, 282.7, 333.3, 272, 320, 272);
                p.closePath();

                p.moveTo(424, 272);
                p.curveTo(410.7, 272, 400, 282.7, 400, 296);
                p.lineTo(400, 488);
                p.curveTo(400, 501.3, 410.7, 512, 424, 512);
                p.curveTo(437.3, 512, 448, 501.3, 448, 488);
                p.lineTo(448, 296);
                p.curveTo(448, 282.7, 437.3, 272, 424, 272);
                p.closePath();
                
                // Base size is 640x640. We scale it to (size*0.65) so it has padding.
                double targetSize = size * 0.65;
                double scale = targetSize / 640.0;
                double offset = (size - targetSize) / 2.0;
                
                AffineTransform tx = new AffineTransform();
                tx.translate(offset, offset);
                tx.scale(scale, scale);
                shape = tx.createTransformedShape(p);
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.translate(x, y);
                g2.fill(shape);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    public static Icon getUndoIcon(int size, Color color) {
        return new Icon() {
            private Shape shape;
            {
                Path2D.Float p = new Path2D.Float();
                p.moveTo(48.5, 224);
                p.lineTo(40, 224);
                p.curveTo(26.7, 224, 16, 213.3, 16, 200);
                p.lineTo(16, 72);
                p.curveTo(16, 62.3, 21.8, 53.5, 30.8, 49.8);
                p.curveTo(39.8, 46.1, 50.1, 48.1, 57.0, 55.0);
                p.lineTo(98.6, 96.6);
                p.curveTo(186.2, 10.1, 327.3, 10.4, 414.4, 97.6);
                p.curveTo(501.9, 185.1, 501.9, 326.9, 414.4, 414.4);
                p.curveTo(326.9, 501.9, 185.1, 501.9, 97.6, 414.4);
                p.curveTo(85.1, 401.9, 85.1, 381.6, 97.6, 369.1);
                p.curveTo(110.1, 356.6, 130.4, 356.6, 142.9, 369.1);
                p.curveTo(205.4, 431.6, 306.7, 431.6, 369.2, 369.1);
                p.curveTo(431.7, 306.6, 431.7, 205.3, 369.2, 142.8);
                p.curveTo(307.0, 80.6, 206.5, 80.3, 143.9, 141.8);
                p.lineTo(185, 183);
                p.curveTo(191.9, 189.9, 193.9, 200.2, 190.2, 209.2);
                p.curveTo(186.5, 218.2, 177.7, 224.0, 168.0, 224.0);
                p.lineTo(48.5, 224.0);
                p.closePath();

                double targetSize = size * 0.70;
                double scale = targetSize / 512.0;
                double offset = (size - targetSize) / 2.0;
                
                AffineTransform tx = new AffineTransform();
                tx.translate(offset, offset);
                tx.scale(scale, scale);
                shape = tx.createTransformedShape(p);
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.translate(x, y);
                g2.fill(shape);
                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }
}
