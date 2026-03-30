package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomTeleportSettingsSearchPatternTest {

    private static final Logger LOGGER = Logger.getLogger("RandomTeleportSettingsSearchPatternTest");

    @Test
    void parsesExplicitPatternValue() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("search-pattern", "circle");

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertEquals(SearchPattern.CIRCLE, settings.getSearchPattern(),
            "Explicit search-pattern should be parsed case-insensitively");
    }

    @Test
    void reusesFallbackPatternWhenUnset() {
        YamlConfiguration fallbackConfiguration = new YamlConfiguration();
        fallbackConfiguration.set("search-pattern", "square");

        RandomTeleportSettings fallback = RandomTeleportSettings.fromConfiguration(fallbackConfiguration, LOGGER);
        YamlConfiguration override = new YamlConfiguration();

        RandomTeleportSettings merged = RandomTeleportSettings.fromConfiguration(override, LOGGER, fallback);
        assertEquals(SearchPattern.SQUARE, merged.getSearchPattern(),
            "Fallback pattern should carry over when not overridden");
    }

    @Test
    void parsesTrianglePattern() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("search-pattern", "triangle");

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertEquals(SearchPattern.TRIANGLE, settings.getSearchPattern(),
            "Explicit 'triangle' search-pattern should be parsed case-insensitively");
    }

    @Test
    void parsesDiamondPattern() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("search-pattern", "diamond");

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertEquals(SearchPattern.DIAMOND, settings.getSearchPattern(),
            "Explicit 'diamond' search-pattern should be parsed case-insensitively");
    }
}
