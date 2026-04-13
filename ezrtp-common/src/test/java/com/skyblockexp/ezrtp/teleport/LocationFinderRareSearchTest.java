package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.biome.BiomePreCacheSettings;
import com.skyblockexp.ezrtp.config.biome.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.teleport.ChunkLoadingSettings;
import com.skyblockexp.ezrtp.config.biome.ChunkyIntegrationSettings;
import com.skyblockexp.ezrtp.config.effects.CountdownBossBarSettings;
import com.skyblockexp.ezrtp.config.effects.CountdownParticleSettings;
import com.skyblockexp.ezrtp.config.teleport.OnJoinTeleportSettings;
import com.skyblockexp.ezrtp.config.effects.ParticleSettings;
import com.skyblockexp.ezrtp.config.safety.ProtectionSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.teleport.TeleportMessages;
import com.skyblockexp.ezrtp.config.biome.RareBiomeOptimizationSettings;
import com.skyblockexp.ezrtp.config.safety.SafetySettings;
import com.skyblockexp.ezrtp.config.teleport.SearchPattern;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationFinderRareSearchTest {

    @Test
    void isRareSearch_returnsFalseWhenRareOptimizationIsDisabled() throws Exception {
        LocationFinder finder = allocateLocationFinderWithoutConstructor();
        RareBiomeRegistry registry = mock(RareBiomeRegistry.class);
        when(registry.isEnabled()).thenReturn(true);
        when(registry.isRareBiome(Biome.MUSHROOM_FIELDS)).thenReturn(true);
        setField(finder, "rareBiomeRegistry", registry);

        RandomTeleportSettings settings = settingsWithRareOptimization(false, Set.of(Biome.MUSHROOM_FIELDS));

        assertFalse(finder.isRareSearch(settings));
    }

    @Test
    void isRareSearch_returnsTrueWhenOptimizationEnabledAndAllIncludesAreRare() throws Exception {
        LocationFinder finder = allocateLocationFinderWithoutConstructor();
        RareBiomeRegistry registry = mock(RareBiomeRegistry.class);
        when(registry.isEnabled()).thenReturn(true);
        when(registry.isRareBiome(Biome.MUSHROOM_FIELDS)).thenReturn(true);
        setField(finder, "rareBiomeRegistry", registry);

        RandomTeleportSettings settings = settingsWithRareOptimization(true, Set.of(Biome.MUSHROOM_FIELDS));

        assertTrue(finder.isRareSearch(settings));
    }

    private static RandomTeleportSettings settingsWithRareOptimization(boolean rareOptimizationEnabled,
                                                                       Set<Biome> include) {
        RareBiomeOptimizationSettings rareSettings = new RareBiomeOptimizationSettings(
            rareOptimizationEnabled,
            include,
            true,
            true,
            false,
            48,
            8,
            true,
            2,
            3L,
            true,
            "none",
            "",
            "",
            ""
        );

        return new RandomTeleportSettings(
            null, "world", 0, 0, 100, 1000, 10, false,
            Collections.emptySet(), TeleportMessages.defaultMessages(), ParticleSettings.disabled(),
            OnJoinTeleportSettings.fromConfiguration(null), CountdownBossBarSettings.disabled(), CountdownParticleSettings.disabled(),
            0.0D, 0, true, false, null, null, include, Collections.emptySet(),
            new ProtectionSettings(false, Collections.emptyList()), BiomePreCacheSettings.disabled(), rareSettings,
            ChunkLoadingSettings.defaults(), true, BiomeSearchSettings.defaults(), true, true, SafetySettings.defaults(), SearchPattern.RANDOM,
            ChunkyIntegrationSettings.defaults()
        );
    }

    private static LocationFinder allocateLocationFinderWithoutConstructor() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (LocationFinder) allocateInstance.invoke(unsafe, LocationFinder.class);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = LocationFinder.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
