package com.skyblockexp.ezrtp.config;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

/** Encapsulates the {@code performance:} block from {@code performance.yml}. */
public final class PerformanceSettings {

    private static final int DEFAULT_SLOW_THRESHOLD_MS = 100;
    private static final int DEFAULT_EXPORT_INTERVAL_MINUTES = 5;
    private static final String DEFAULT_FILE_PATH = "plugins/EzRTP/performance.log";
    private static final String DEFAULT_EXPORT_PATH = "plugins/EzRTP/metrics.json";
    private static final List<Integer> DEFAULT_BUCKETS = List.of(50, 75, 90, 95, 99);

    private final MonitoringSettings monitoring;
    private final WarningsSettings warnings;
    private final MetricsSettings metrics;
    private final PercentilesSettings percentiles;

    private PerformanceSettings(
            MonitoringSettings monitoring,
            WarningsSettings warnings,
            MetricsSettings metrics,
            PercentilesSettings percentiles) {
        this.monitoring = monitoring;
        this.warnings = warnings;
        this.metrics = metrics;
        this.percentiles = percentiles;
    }

    public MonitoringSettings getMonitoring() {
        return monitoring;
    }

    public WarningsSettings getWarnings() {
        return warnings;
    }

    public MetricsSettings getMetrics() {
        return metrics;
    }

    public PercentilesSettings getPercentiles() {
        return percentiles;
    }

    /** Returns a settings instance with all defaults (monitoring disabled). */
    public static PerformanceSettings defaults() {
        return new PerformanceSettings(
                new MonitoringSettings(false),
                new WarningsSettings(DEFAULT_SLOW_THRESHOLD_MS, true, true, DEFAULT_FILE_PATH),
                new MetricsSettings(false, DEFAULT_EXPORT_INTERVAL_MINUTES, DEFAULT_EXPORT_PATH),
                new PercentilesSettings(true, DEFAULT_BUCKETS));
    }

    /**
     * Parses performance settings from the root of a {@code performance.yml}
     * {@link ConfigurationSection}. Missing values fall back to defaults.
     */
    public static PerformanceSettings fromConfiguration(ConfigurationSection root) {
        if (root == null) {
            return defaults();
        }
        ConfigurationSection perfSection = root.getConfigurationSection("performance");
        if (perfSection == null) {
            return defaults();
        }

        PerformanceSettings def = defaults();

        // monitoring
        ConfigurationSection monSection = perfSection.getConfigurationSection("monitoring");
        boolean monEnabled =
                monSection != null
                        ? monSection.getBoolean("enabled", false)
                        : false;
        MonitoringSettings monitoring = new MonitoringSettings(monEnabled);

        // warnings
        ConfigurationSection warnSection = perfSection.getConfigurationSection("warnings");
        WarningsSettings warnings;
        if (warnSection != null) {
            int threshold =
                    warnSection.getInt(
                            "slow-rtp-threshold-ms", def.getWarnings().getSlowRtpThresholdMs());
            boolean logConsole =
                    warnSection.getBoolean("log-to-console", def.getWarnings().isLogToConsole());
            boolean logFile =
                    warnSection.getBoolean("log-to-file", def.getWarnings().isLogToFile());
            String filePath =
                    warnSection.getString("file-path", def.getWarnings().getFilePath());
            warnings = new WarningsSettings(threshold, logConsole, logFile, filePath);
        } else {
            warnings = def.getWarnings();
        }

        // metrics
        ConfigurationSection metSection = perfSection.getConfigurationSection("metrics");
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

        // percentiles
        ConfigurationSection pctSection = perfSection.getConfigurationSection("percentiles");
        PercentilesSettings percentiles;
        if (pctSection != null) {
            boolean track = pctSection.getBoolean("track", def.getPercentiles().isTrack());
            List<Integer> buckets;
            if (pctSection.isList("buckets")) {
                List<Integer> raw = pctSection.getIntegerList("buckets");
                buckets = raw.isEmpty() ? def.getPercentiles().getBuckets() : List.copyOf(raw);
            } else {
                buckets = def.getPercentiles().getBuckets();
            }
            percentiles = new PercentilesSettings(track, buckets);
        } else {
            percentiles = def.getPercentiles();
        }

        return new PerformanceSettings(monitoring, warnings, metrics, percentiles);
    }

    // -------------------------------------------------------------------------
    // Nested setting records
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

    public static final class WarningsSettings {
        private final int slowRtpThresholdMs;
        private final boolean logToConsole;
        private final boolean logToFile;
        private final String filePath;

        public WarningsSettings(
                int slowRtpThresholdMs,
                boolean logToConsole,
                boolean logToFile,
                String filePath) {
            this.slowRtpThresholdMs = slowRtpThresholdMs;
            this.logToConsole = logToConsole;
            this.logToFile = logToFile;
            this.filePath = filePath != null ? filePath : DEFAULT_FILE_PATH;
        }

        public int getSlowRtpThresholdMs() {
            return slowRtpThresholdMs;
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

    public static final class PercentilesSettings {
        private final boolean track;
        private final List<Integer> buckets;

        public PercentilesSettings(boolean track, List<Integer> buckets) {
            this.track = track;
            this.buckets =
                    (buckets != null && !buckets.isEmpty()) ? List.copyOf(buckets) : DEFAULT_BUCKETS;
        }

        public boolean isTrack() {
            return track;
        }

        public List<Integer> getBuckets() {
            return buckets;
        }
    }
}
