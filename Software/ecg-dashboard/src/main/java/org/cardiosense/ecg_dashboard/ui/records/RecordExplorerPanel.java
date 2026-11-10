package org.cardiosense.ecg_dashboard.ui.records;

import org.cardiosense.ecg_dashboard.model.EcgSessionRecord;
import org.cardiosense.ecg_dashboard.storage.StorageManager;
import org.cardiosense.ecg_dashboard.ui.components.ModernCheckBox;
import org.cardiosense.ecg_dashboard.ui.components.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecordExplorerPanel extends JPanel {
    private StorageManager storageManager;
    private JPanel listContainer;
    private RecordDetailPanel detailPanel;
    private CardLayout cardLayout;
    
    private Set<EcgSessionRecord> selectedSessions = new HashSet<>();
    private ModernCheckBox selectAllBox;
    private JButton bulkDeleteBtn;
    
    private static final String LIST_VIEW = "LIST";
    private static final String DETAIL_VIEW = "DETAIL";

    public RecordExplorerPanel(StorageManager storageManager) {
        this.storageManager = storageManager;
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // List View
        JPanel listWrapper = new JPanel(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("Past Records");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBackground(Color.WHITE);
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 20));
        
        selectAllBox = new ModernCheckBox("Select All");
        selectAllBox.setFocusable(false);
        selectAllBox.setBackground(Color.WHITE);
        selectAllBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectAllBox.addActionListener(e -> {
            boolean selected = selectAllBox.isSelected();
            List<EcgSessionRecord> sessions = storageManager.loadAllSessions();
            if (selected) {
                selectedSessions.addAll(sessions);
            } else {
                selectedSessions.clear();
            }
            loadRecords(); // Re-render to update checkboxes
        });
        
        bulkDeleteBtn = new JButton("Delete Selected");
        bulkDeleteBtn.setFocusable(false);
        bulkDeleteBtn.setBackground(new Color(239, 83, 80));
        bulkDeleteBtn.setForeground(Color.WHITE);
        bulkDeleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bulkDeleteBtn.setVisible(false); // Hidden by default
        bulkDeleteBtn.addActionListener(e -> {
            for (EcgSessionRecord s : selectedSessions) {
                storageManager.moveToTrash(s);
            }
            selectedSessions.clear();
            selectAllBox.setSelected(false);
            loadRecords();
        });
        
        toolbarPanel.add(selectAllBox);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(bulkDeleteBtn);
        
        headerPanel.add(toolbarPanel, BorderLayout.SOUTH);
        listWrapper.add(headerPanel, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        listWrapper.add(scrollPane, BorderLayout.CENTER);

        // Detail View
        detailPanel = new RecordDetailPanel(() -> {
            cardLayout.show(this, LIST_VIEW);
            loadRecords(); 
        }, () -> {
            // onDelete
            if (detailPanel.getCurrentSession() != null) {
                storageManager.moveToTrash(detailPanel.getCurrentSession());
                cardLayout.show(this, LIST_VIEW);
                loadRecords();
            }
        });

        add(listWrapper, LIST_VIEW);
        add(detailPanel, DETAIL_VIEW);

        cardLayout.show(this, LIST_VIEW);
    }

    public void loadRecords() {
        listContainer.removeAll();
        
        List<EcgSessionRecord> sessions = storageManager.loadAllSessions();
        selectedSessions.retainAll(sessions);
        selectAllBox.setSelected(!sessions.isEmpty() && selectedSessions.size() == sessions.size());
        selectAllBox.setEnabled(!sessions.isEmpty());
        bulkDeleteBtn.setVisible(!selectedSessions.isEmpty());
        if (sessions.isEmpty()) {
            JLabel emptyLabel = new JLabel("No records found.");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
            listContainer.add(emptyLabel);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a");
            
            for (EcgSessionRecord session : sessions) {
                JPanel itemPanel = createRecordItem(session, sdf);
                listContainer.add(itemPanel);
            }
            listContainer.add(Box.createVerticalGlue());
        }
        
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createRecordItem(EcgSessionRecord session, SimpleDateFormat sdf) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(800, 80));
        panel.setMinimumSize(new Dimension(200, 80));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(Color.WHITE);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Checkbox Panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        
        ModernCheckBox itemCheckbox = new ModernCheckBox();
        itemCheckbox.setFocusable(false);
        itemCheckbox.setOpaque(false);
        itemCheckbox.setSelected(selectedSessions.contains(session));
        itemCheckbox.addActionListener(e -> {
            if (itemCheckbox.isSelected()) {
                selectedSessions.add(session);
            } else {
                selectedSessions.remove(session);
            }
            List<EcgSessionRecord> all = storageManager.loadAllSessions();
            selectAllBox.setSelected(!all.isEmpty() && selectedSessions.size() == all.size());
            bulkDeleteBtn.setVisible(!selectedSessions.isEmpty());
        });
        
        leftPanel.add(itemCheckbox, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.WEST);

        // Format Date
        String dateStr = sdf.format(new Date(session.startTimeMs));
        
        // Format Duration
        long durationMs = session.endTimeMs - session.startTimeMs;
        int totalSeconds = (int) (durationMs / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String durationStr = String.format("%02d:%02d", minutes, seconds);

        // Labels
        JPanel infoPanel = new JPanel(new GridLayout(1, 3));
        infoPanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel("Date: " + dateStr);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel durationLabel = new JLabel("Duration: " + durationStr);
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel bpmLabel = new JLabel("Avg BPM: " + session.finalBpm);
        bpmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        infoPanel.add(dateLabel);
        infoPanel.add(durationLabel);
        infoPanel.add(bpmLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);

        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setOpaque(false);

        Icon deleteIcon = org.cardiosense.ecg_dashboard.ui.components.SvgIcons.getTrashIcon(24, Color.WHITE);

        RoundedButton deleteBtn = new RoundedButton("", 10);
        deleteBtn.setIcon(deleteIcon);
        deleteBtn.setMargin(new Insets(0, 0, 0, 0));
        deleteBtn.setBackground(new Color(239, 83, 80)); // Red
        deleteBtn.setPreferredSize(new Dimension(40, 40));
        deleteBtn.setToolTipText("Move to Trash");
        deleteBtn.addActionListener(e -> {
            storageManager.moveToTrash(session);
            loadRecords(); // Refresh list
        });

        actionsPanel.add(deleteBtn);
        panel.add(actionsPanel, BorderLayout.EAST);

        // Hover & Click
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(Color.WHITE);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                detailPanel.loadSession(session);
                cardLayout.show(RecordExplorerPanel.this, DETAIL_VIEW);
            }
        });

        return panel;
    }
}
