package org.cardiosense.ecg_dashboard;

import org.cardiosense.ecg_dashboard.ui.MainFrame;
import org.cardiosense.ecg_dashboard.storage.StorageManager;
import javax.swing.SwingUtilities;

public class CardioSense {

    public static void main(String[] args) {
        // Init StorageManager and purge old records
        StorageManager storage = new StorageManager();
        storage.purgeOldRecords();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

}
