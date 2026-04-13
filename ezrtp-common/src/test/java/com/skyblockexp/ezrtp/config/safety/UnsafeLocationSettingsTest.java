package com.skyblockexp.ezrtp.config.safety;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.skyblockexp.ezrtp.config.safety.UnsafeLocationSettings;

class UnsafeLocationSettingsTest {

    @Test
    void testDefaultsHaveMonitoringDisabled() {
        UnsafeLocationSettings settings = UnsafeLocationSettings.defaults();

        assertFalse(settings.getMonitoring().isEnabled());
    }

    @Test
    void testDefaultLoggingValues() {
        UnsafeLocationSettings.LoggingSettings logging = UnsafeLocationSettings.defaults().getLogging();

        assertTrue(logging.isWarnOnUnsafe());
        assertTrue(logging.isLogToConsole());
        assertTrue(logging.isLogToFile());
        assertNotNull(logging.getFilePath());
        assertFalse(logging.getFilePath().isBlank());
        assertTrue(logging.getWarnThresholdPercent() > 0);
    }

    @Test
    void testDefaultMetricsDisabled() {
        UnsafeLocationSettings.MetricsSettings metrics = UnsafeLocationSettings.defaults().getMetrics();

        assertFalse(metrics.isEnabled());
        assertTrue(metrics.getExportIntervalMinutes() > 0);
        assertNotNull(metrics.getExportPath());
        assertFalse(metrics.getExportPath().isBlank());
    }

    @Test
    void testDefaultCausesTrackEnabled() {
        assertTrue(UnsafeLocationSettings.defaults().getCauses().isTrack());
    }

    @Test
    void testFromConfigurationNullReturnsDefaults() {
        UnsafeLocationSettings settings = UnsafeLocationSettings.fromConfiguration(null);

        assertFalse(settings.getMonitoring().isEnabled());
        assertTrue(settings.getCauses().isTrack());
    }

    @Test
    void testFromConfigurationMissingSectionReturnsDefaults() {
        YamlConfiguration root = new YamlConfiguration();

        UnsafeLocationSettings settings = UnsafeLocationSettings.fromConfiguration(root);

        assertFalse(settings.getMonitoring().isEnabled());
    }

    @Test
    void testFromConfigurationParsesMonitoringEnabled() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("unsafe-location-monitoring.monitoring").set("enabled", true);

        UnsafeLocationSettings settings = UnsafeLocationSettings.fromConfiguration(root);

        assertTrue(settings.getMonitoring().isEnabled());
    }

    @Test
    void testFromConfigurationParsesLogging() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("unsafe-location-monitoring.logging").set("warn-on-unsafe", false);
        root.getConfigurationSection("unsafe-location-monitoring.logging").set("log-to-console", false);
        root.getConfigurationSection("unsafe-location-monitoring.logging").set("log-to-file", true);
        root.getConfigurationSection("unsafe-location-monitoring.logging").set("file-path", "/tmp/unsafe.log");
        root.getConfigurationSection("unsafe-location-monitoring.logging").set("warn-threshold-percent", 35.0);

        UnsafeLocationSettings.LoggingSettings logging =
                UnsafeLocationSettings.fromConfiguration(root).getLogging();

        assertFalse(logging.isWarnOnUnsafe());
        assertFalse(logging.isLogToConsole());
        assertTrue(logging.isLogToFile());
        assertEquals("/tmp/unsafe.log", logging.getFilePath());
        assertEquals(35.0, logging.getWarnThresholdPercent(), 0.001);
    }

    @Test
    void testFromConfigurationParsesMetrics() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("unsafe-location-monitoring.metrics").set("enabled", true);
        root.getConfigurationSection("unsafe-location-monitoring.metrics")
                .set("export-interval-minutes", 10);
        root.getConfigurationSection("unsafe-location-monitoring.metrics")
                .set("export-path", "/tmp/unsafe-metrics.json");

        UnsafeLocationSettings.MetricsSettings metrics =
                UnsafeLocationSettings.fromConfiguration(root).getMetrics();

        assertTrue(metrics.isEnabled());
        assertEquals(10, metrics.getExportIntervalMinutes());
        assertEquals("/tmp/unsafe-metrics.json", metrics.getExportPath());
    }

    @Test
    void testFromConfigurationParsesCausesTrackFalse() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("unsafe-location-monitoring.causes").set("track", false);

        UnsafeLocationSettings.CausesSettings causes =
                UnsafeLocationSettings.fromConfiguration(root).getCauses();

        assertFalse(causes.isTrack());
    }
}
