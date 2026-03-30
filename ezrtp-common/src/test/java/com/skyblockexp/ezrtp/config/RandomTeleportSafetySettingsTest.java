package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomTeleportSafetySettingsTest {

    private static final Logger LOGGER = Logger.getLogger("RandomTeleportSafetySettingsTest");

    @Test
    void defaultsSurfaceScanDepthToTwenty() {
        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(new YamlConfiguration(), LOGGER);

        assertEquals(20, settings.getSafetySettings().getMaxSurfaceScanDepth(),
            "Default max-surface-scan-depth should preserve legacy 20-block behavior");
        assertEquals(128, settings.getSafetySettings().getMaxSurfaceScanDepthNether(),
            "Default max-surface-scan-depth-nether should support deep nether recovery");
    }

    @Test
    void parsesConfiguredSurfaceScanDepth() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("safety.recovery.max-surface-scan-depth", 36);

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertEquals(36, settings.getSafetySettings().getMaxSurfaceScanDepth(),
            "Configured safety.recovery.max-surface-scan-depth should be parsed from rtp.yml");
    }

    @Test
    void parsesConfiguredNetherSurfaceScanDepth() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("safety.recovery.max-surface-scan-depth-nether", 72);

        RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(configuration, LOGGER);

        assertEquals(72, settings.getSafetySettings().getMaxSurfaceScanDepthNether(),
            "Configured safety.recovery.max-surface-scan-depth-nether should be parsed from rtp.yml");
    }
}
