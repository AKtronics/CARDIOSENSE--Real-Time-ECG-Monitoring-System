package org.cardiosense.ecg_dashboard.ui.live;

import javax.swing.*;
import java.awt.*;

public class MetricsContainerPanel extends JPanel {
    
    private OledDataPanel oledPanel;
    private MorphologyPanel morphPanel;

    public MetricsContainerPanel() {
        setLayout(new GridLayout(1, 2, 20, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        oledPanel = new OledDataPanel();
        morphPanel = new MorphologyPanel();
        
        add(oledPanel);
        add(morphPanel);
    }
    
    public OledDataPanel getOledPanel() { return oledPanel; }
    public MorphologyPanel getMorphologyPanel() { return morphPanel; }
}
