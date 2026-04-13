package com.skyblockexp.ezrtp.config;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Encapsulates the {@code unsafe-location-monitoring:} block from
 * {@code unsafe-location-monitoring.yml}.
 */
public final class UnsafeLocationSettings {

    private static final double DEFAULT_WARN_THRESHOLD_PERCENT = 20.0;
    private static final int DEFAULT_EXPORT_INTERVAL_MINUTES = 5;
    private static final String DEFAULT_LOG_FILE_PATH = "plugins/EzRTP/unsafe-locations.log";
    private static final String DEFAULT_EXPORT_PATH = "plugins/EzRTP/unsafe-metrics.json";

    private final MonitoringSettings monitoring;
    private final LoggingSettings logging;
    private final MetricsSettings metrics;
    private final CausesSettings causes;

    private UnsafeLocationSettings(
            MonitoringSettings monitoring,
            LoggingSettings logging,
            MetricsSettings metrics,
            CausesSettings causes) {
        this.monitoring = monitoring;
        this.logging = logging;
        this.metrics = metrics;
        this.causes = causes;
    }

    public MonitoringSettings getMonitoring() {
        return monitoring;
    }

    public LoggingSettings getLogging() {
        return logging;
    }

    public MetricsSettings getMetrics() {
        return metrics;
    }

    public CausesSettings getCauses() {
        return causes;
    }

    /** Returns a settings instance with all defaults (monitoring disabled). */
    public static UnsafeLocationSettings defaults() {
        return new UnsafeLocationSettings(
                new MonitoringSettings(false),
                new LoggingSettings(true, true, true, DEFAULT_LOG_FILE_PATH, DEFAULT_WARN_THRESHOLD_PERCENT),
                new MetricsSettings(false, DEFAULT_EXPORT_INTERVAL_MINUTES, DEFAULT_EXPORT_PATH),
                new CausesSettings(true));
    }

    /**
     * Parses settings from the root of an {@code unsafe-location-monitoring.yml}
     * {@link ConfigurationSection}. Missing values fall back to defaults.
     */
    public static UnsafeLocationSettings fromConfiguration(ConfigurationSection root) {
        if (root == null) {
            return defaults();
        }
        ConfigurationSection section = root.getConfigurationSection("unsafe-location-monitoring");
        if (section == null) {
            return defaults();
        }

        UnsafeLocationSettings def = defaults();

        // monitoring
        ConfigurationSection monSection = section.getConfigurationSection("monitoring");
        boolean monEnabled = monSection != null && monSection.getBoolean("enabled", false);
        MonitoringSettings monitoring = new MonitoringSettings(monEnabled);

        // logging
        ConfigurationSection logSection = section.getConfigurationSection("logging");
        LoggingSettings logging;
        if (logSection != null) {
            boolean warnOnUnsafe =
                    logSection.getBoolean("warn-on-unsafe", def.getLogging().isWarnOnUnsafe());
            boolean logToConsole =
                    logSection.getBoolean("log-to-console", def.getLogging().isLogToConsole());
            boolean logToFile =
                    logSection.getBoolean("log-to-file", def.getLogging().isLogToFile());
            String filePath =
                    logSection.getString("file-path", def.getLogging().getFilePath());
            double warnThreshold =
                    logSection.getDouble(
                            "warn-threshold-percent", def.getLogging().getWarnThresholdPercent());
            logging = new LoggingSettings(warnOnUnsafe, logToConsole, logToFile, filePath, warnThreshold);
        } else {
            logging = def.getLogging();
        }

        // metrics
        ConfigurationSection metSection = section.getConfigurationSection("metrics");
        MetricsSettings metrics;
        if (metSection != null) {
            boolean metEnabled = metSection.getBoolean("enabled", def.getMetrics().isEnabled());
            int interval =
                    metSection.getInt(
                            "export-interval-minutes",
                            def.getMetrics().getExportIntervalMinutes());
            String exportPath =
                    metSection.getString("export-path", def.getMetrics().getExportPath());
            metrics = new MetricsSettings(metEnabled, interval, exportPath);
        } else {
            metrics = def.getMetrics();
        }

        // causes
        ConfigurationSection causeSection = section.getConfigurationSection("causes");
        CausesSettings causes;
        if (causeSection != null) {
            boolean track = causeSection.getBoolean("track", def.getCauses().isTrack());
            causes = new CausesSettings(track);
        } else {
            causes = def.getCauses();
        }

        return new UnsafeLocationSettings(monitoring, logging, metrics, causes);
    }

    // -------------------------------------------------------------------------
    // Nested setting classes
    // -------------------------------------------------------------------------

    public static final class MonitoringSettings {
        private final boolean enabled;

        public MonitoringSettings(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public static final class LoggingSettings {
        private final boolean warnOnUnsafe;
        private final boolean logToConsole;
        private final boolean logToFile;
        private final String filePath;
        private final double warnThresholdPercent;

        public LoggingSettings(
                boolean warnOnUnsafe,
                boolean logToConsole,
                boolean logToFile,
                String filePath,
                double warnThresholdPercent) {
            this.warnOnUnsafe = warnOnUnsafe;
            this.logToConsole = logToConsole;
            this.logToFile = logToFile;
            this.filePath = filePath != null ? filePath : DEFAULT_LOG_FILE_PATH;
            this.warnThresholdPercent =
                    (warnThresholdPercent > 0 && warnThresholdPercent <= 100)
                            ? warnThresholdPercent
                            : DEFAULT_WARN_THRESHOLD_PERCENT;
        }

        public boolean isWarnOnUnsafe() {
            return warnOnUnsafe;
        }

        public boolean isLogToConsole() {
            return logToConsole;
        }

        public boolean isLogToFile() {
            return logToFile;
        }

        public String getFilePath() {
            return filePath;
        }

        public double getWarnThresholdPercent() {
            return warnThresholdPercent;
        }
    }

    public static final class MetricsSettings {
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

    public static final class CausesSettings {
        private final boolean track;

        public CausesSettings(boolean track) {
            this.track = track;
        }

        public boolean isTrack() {
            return track;
        }
    }
}
