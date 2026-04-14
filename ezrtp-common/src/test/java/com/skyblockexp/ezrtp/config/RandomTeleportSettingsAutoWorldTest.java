package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomTeleportSettingsAutoWorldTest {

    private static final Logger LOGGER = Logger.getLogger("RandomTeleportSettingsAutoWorldTest");

    @Test
    void isAutoWorldFalseByDefault() {
        YamlConfiguration config = new YamlConfiguration();

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(config, LOGGER);

        assertFalse(settings.isAutoWorld(),
                "isAutoWorld() should be false when no world key is present");
    }

    @Test
    void isAutoWorldFalseForExplicitWorld() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("world", "myworld");

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(config, LOGGER);

        assertFalse(settings.isAutoWorld(),
                "isAutoWorld() should be false for an explicit world name");
    }

    @Test
    void isAutoWorldTrueForAutoLowercase() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("world", "auto");

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(config, LOGGER);

        assertTrue(settings.isAutoWorld(),
                "isAutoWorld() should be true when world is 'auto'");
    }

    @Test
    void isAutoWorldTrueForAutoUppercase() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("world", "AUTO");

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(config, LOGGER);

        assertTrue(settings.isAutoWorld(),
                "isAutoWorld() should be true when world is 'AUTO' (case-insensitive)");
    }

    @Test
    void withWorldNameReplacesWorldName() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("world", "auto");

        RandomTeleportSettings original = RandomTeleportSettings.fromConfiguration(config, LOGGER);
        RandomTeleportSettings resolved = original.withWorldName("farm_nether");

        assertEquals("farm_nether", resolved.getWorldName(),
                "withWorldName() should set the new world name");
        assertFalse(resolved.isAutoWorld(),
                "isAutoWorld() should return false after withWorldName() replaces the sentinel");
    }

    @Test
    void withWorldNamePreservesOtherFields() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("world", "auto");
        config.set("radius.min", 200);
        config.set("radius.max", 3000);
        config.set("max-attempts", 20);
        config.set("search-pattern", "circle");

        RandomTeleportSettings original = RandomTeleportSettings.fromConfiguration(config, LOGGER);
        RandomTeleportSettings resolved = original.withWorldName("some_world");

        assertEquals(200, resolved.getMinimumRadius(),
                "withWorldName() should preserve minimumRadius");
        assertEquals(3000, resolved.getMaximumRadius(),
                "withWorldName() should preserve maximumRadius");
        assertEquals(20, resolved.getMaxAttempts(),
                "withWorldName() should preserve maxAttempts");
        assertEquals(com.skyblockexp.ezrtp.config.teleport.SearchPattern.CIRCLE, resolved.getSearchPattern(),
                "withWorldName() should preserve searchPattern");
    }
}
