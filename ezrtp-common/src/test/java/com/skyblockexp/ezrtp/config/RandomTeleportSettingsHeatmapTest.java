package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomTeleportSettingsHeatmapTest {

    private static final Logger LOGGER = Logger.getLogger("RandomTeleportSettingsHeatmapTest");

    @Test
    void heatmapDisabledByDefault() {
        // A configuration that does not mention heatmap at all
        YamlConfiguration configuration = new YamlConfiguration();

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertFalse(settings.isHeatmapEnabled(),
                "Heatmap should be disabled by default when key is absent");
    }

    @Test
    void heatmapDisabledWhenExplicitlyFalse() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("heatmap.enabled", false);

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertFalse(settings.isHeatmapEnabled(),
                "Heatmap should be disabled when heatmap.enabled=false");
    }

    @Test
    void heatmapEnabledWhenExplicitlyTrue() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("heatmap.enabled", true);

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertTrue(settings.isHeatmapEnabled(),
                "Heatmap should be enabled when heatmap.enabled=true");
    }

    @Test
    void heatmapDisabledWhenNullSection() {
        // fromConfiguration(null, logger) returns hard-coded defaults
        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(null, LOGGER);

        assertFalse(settings.isHeatmapEnabled(),
                "Heatmap should be disabled when config section is null");
    }
}
