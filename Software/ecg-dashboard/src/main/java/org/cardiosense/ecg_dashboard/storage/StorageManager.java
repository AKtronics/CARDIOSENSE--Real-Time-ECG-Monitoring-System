package org.cardiosense.ecg_dashboard.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cardiosense.ecg_dashboard.model.EcgSessionRecord;
import org.cardiosense.ecg_dashboard.util.DashboardConstants;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class StorageManager {

    private final Gson gson;
    private final File storageDir;
    private final File trashDir;

    public StorageManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        storageDir = new File(DashboardConstants.STORAGE_DIR);
        trashDir = new File(storageDir, "trash");
        
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        if (!trashDir.exists()) {
            trashDir.mkdirs();
        }
    }

    public void saveSession(EcgSessionRecord record) {
        File file = new File(storageDir, record.sessionId + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(record, writer);
        } catch (IOException e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }

    public EcgSessionRecord loadSession(String sessionId) {
        File file = new File(storageDir, sessionId + ".json");
        if (!file.exists()) return null;
        
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, EcgSessionRecord.class);
        } catch (IOException e) {
            System.err.println("Failed to load session: " + e.getMessage());
            return null;
        }
    }

    public List<EcgSessionRecord> loadAllSessions() {
        List<EcgSessionRecord> sessions = new ArrayList<>();
        File[] files = storageDir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    EcgSessionRecord record = gson.fromJson(reader, EcgSessionRecord.class);
                    if (record != null) {
                        sessions.add(record);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load session " + file.getName() + ": " + e.getMessage());
                }
            }
        }
        // Sort by start time descending (newest first)
        sessions.sort((a, b) -> Long.compare(b.startTimeMs, a.startTimeMs));
        return sessions;
    }

    public void moveToTrash(EcgSessionRecord record) {
        File file = new File(storageDir, record.sessionId + ".json");
        File trashedFile = new File(trashDir, record.sessionId + ".json");
        if (file.exists()) {
            try {
                Files.move(file.toPath(), trashedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Failed to move session to trash: " + e.getMessage());
            }
        }
    }

    public void restoreFromTrash(EcgSessionRecord record) {
        File trashedFile = new File(trashDir, record.sessionId + ".json");
        File file = new File(storageDir, record.sessionId + ".json");
        if (trashedFile.exists()) {
            try {
                Files.move(trashedFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Failed to restore session from trash: " + e.getMessage());
            }
        }
    }

    public void permanentlyDelete(EcgSessionRecord record) {
        File trashedFile = new File(trashDir, record.sessionId + ".json");
        if (trashedFile.exists()) {
            trashedFile.delete();
        }
    }

    public List<EcgSessionRecord> loadTrashedSessions() {
        List<EcgSessionRecord> sessions = new ArrayList<>();
        File[] files = trashDir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    EcgSessionRecord record = gson.fromJson(reader, EcgSessionRecord.class);
                    if (record != null) {
                        sessions.add(record);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load trashed session " + file.getName() + ": " + e.getMessage());
                }
            }
        }
        // Sort by start time descending (newest first)
        sessions.sort((a, b) -> Long.compare(b.startTimeMs, a.startTimeMs));
        return sessions;
    }
    
    public void purgeOldRecords() {
        long thirtyDaysMs = 30L * 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        
        File[] files = storageDir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                if (now - file.lastModified() > thirtyDaysMs) {
                    file.delete();
                }
            }
        }
    }
}
