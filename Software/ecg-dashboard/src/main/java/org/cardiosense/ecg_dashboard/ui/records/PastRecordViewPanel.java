package org.cardiosense.ecg_dashboard.ui.records;

import javax.swing.*;
import java.awt.*;

public class PastRecordViewPanel extends JPanel {
    public PastRecordViewPanel() {
        setLayout(new BorderLayout());
        JLabel label = new JLabel("Past Record Viewer (Static Chart of JSON data)");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }
}
