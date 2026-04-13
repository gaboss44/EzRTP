package com.skyblockexp.ezrtp.config.safety;

public final class MetricsSettings {
    private static final int DEFAULT_EXPORT_INTERVAL_MINUTES = 5;
    private static final String DEFAULT_EXPORT_PATH = "plugins/EzRTP/unsafe-metrics.json";

    private final boolean enabled;
    private final int exportIntervalMinutes;
    private final String exportPath;

    public MetricsSettings(boolean enabled, int exportIntervalMinutes, String exportPath) {
        this.enabled = enabled;
        this.exportIntervalMinutes =
                exportIntervalMinutes > 0 ? exportIntervalMinutes : DEFAULT_EXPORT_INTERVAL_MINUTES;
        this.exportPath = exportPath != null ? exportPath : DEFAULT_EXPORT_PATH;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getExportIntervalMinutes() {
        return exportIntervalMinutes;
    }

    public String getExportPath() {
        return exportPath;
    }
}
