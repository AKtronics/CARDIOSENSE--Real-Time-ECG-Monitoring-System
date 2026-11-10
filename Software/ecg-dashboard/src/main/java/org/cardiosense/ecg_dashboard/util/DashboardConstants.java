package org.cardiosense.ecg_dashboard.util;

import java.io.File;

public class DashboardConstants {
    public static final int BAUD_RATE = 115200;
    public static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + ".ecg_dashboard";
    public static final int UI_WIDTH = 1200;
    public static final int UI_HEIGHT = 800;
    public static final int CHART_MAX_AGE_MS = 5000; // 5 seconds of rolling data
}
