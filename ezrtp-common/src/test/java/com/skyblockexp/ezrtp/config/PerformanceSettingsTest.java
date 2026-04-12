package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceSettingsTest {

    @Test
    void testDefaultsHaveMonitoringDisabled() {
        PerformanceSettings settings = PerformanceSettings.defaults();

        assertFalse(settings.getMonitoring().isEnabled());
    }

    @Test
    void testDefaultWarningValues() {
        PerformanceSettings.WarningsSettings w = PerformanceSettings.defaults().getWarnings();

        assertEquals(100, w.getSlowRtpThresholdMs());
        assertTrue(w.isLogToConsole());
        assertTrue(w.isLogToFile());
        assertNotNull(w.getFilePath());
        assertFalse(w.getFilePath().isBlank());
    }

    @Test
    void testDefaultMetricsDisabled() {
        PerformanceSettings.MetricsSettings m = PerformanceSettings.defaults().getMetrics();

        assertFalse(m.isEnabled());
        assertTrue(m.getExportIntervalMinutes() > 0);
        assertNotNull(m.getExportPath());
    }

    @Test
    void testDefaultPercentileTrackingEnabled() {
        PerformanceSettings.PercentilesSettings p = PerformanceSettings.defaults().getPercentiles();

        assertTrue(p.isTrack());
        assertFalse(p.getBuckets().isEmpty());
    }

    @Test
    void testFromConfigurationNullReturnsDefaults() {
        PerformanceSettings settings = PerformanceSettings.fromConfiguration(null);

        assertFalse(settings.getMonitoring().isEnabled());
        assertEquals(100, settings.getWarnings().getSlowRtpThresholdMs());
    }

    @Test
    void testFromConfigurationMissingPerformanceSectionReturnsDefaults() {
        YamlConfiguration root = new YamlConfiguration();

        PerformanceSettings settings = PerformanceSettings.fromConfiguration(root);

        assertFalse(settings.getMonitoring().isEnabled());
    }

    @Test
    void testFromConfigurationParsesMonitoringEnabled() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("performance.monitoring").set("enabled", true);

        PerformanceSettings settings = PerformanceSettings.fromConfiguration(root);

        assertTrue(settings.getMonitoring().isEnabled());
    }

    @Test
    void testFromConfigurationParsesWarnings() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("performance.warnings").set("slow-rtp-threshold-ms", 250);
        root.getConfigurationSection("performance.warnings").set("log-to-console", false);
        root.getConfigurationSection("performance.warnings").set("log-to-file", true);
        root.getConfigurationSection("performance.warnings").set("file-path", "/tmp/slow.log");

        PerformanceSettings settings = PerformanceSettings.fromConfiguration(root);
        PerformanceSettings.WarningsSettings w = settings.getWarnings();

        assertEquals(250, w.getSlowRtpThresholdMs());
        assertFalse(w.isLogToConsole());
        assertTrue(w.isLogToFile());
        assertEquals("/tmp/slow.log", w.getFilePath());
    }

    @Test
    void testFromConfigurationParsesMetrics() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("performance.metrics").set("enabled", true);
        root.getConfigurationSection("performance.metrics").set("export-interval-minutes", 10);
        root.getConfigurationSection("performance.metrics").set("export-path", "/tmp/metrics.json");

        PerformanceSettings settings = PerformanceSettings.fromConfiguration(root);
        PerformanceSettings.MetricsSettings m = settings.getMetrics();

        assertTrue(m.isEnabled());
        assertEquals(10, m.getExportIntervalMinutes());
        assertEquals("/tmp/metrics.json", m.getExportPath());
    }

    @Test
    void testFromConfigurationParsesBuckets() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("performance.percentiles").set("track", true);
        root.getConfigurationSection("performance.percentiles").set("buckets", List.of(50, 90, 99));

        PerformanceSettings settings = PerformanceSettings.fromConfiguration(root);

        assertEquals(List.of(50, 90, 99), settings.getPercentiles().getBuckets());
    }

    @Test
    void testMetricsExportIntervalClampedToPositive() {
        PerformanceSettings.MetricsSettings m = new PerformanceSettings.MetricsSettings(true, 0, "/tmp/x.json");

        assertTrue(m.getExportIntervalMinutes() > 0);
    }
}
