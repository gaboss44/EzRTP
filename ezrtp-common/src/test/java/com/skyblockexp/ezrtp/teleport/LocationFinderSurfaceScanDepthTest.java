package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.BiomePreCacheSettings;
import com.skyblockexp.ezrtp.config.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.ChunkLoadingSettings;
import com.skyblockexp.ezrtp.config.ChunkyIntegrationSettings;
import com.skyblockexp.ezrtp.config.CountdownBossBarSettings;
import com.skyblockexp.ezrtp.config.CountdownParticleSettings;
import com.skyblockexp.ezrtp.config.OnJoinTeleportSettings;
import com.skyblockexp.ezrtp.config.ParticleSettings;
import com.skyblockexp.ezrtp.config.ProtectionSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.RareBiomeOptimizationSettings;
import com.skyblockexp.ezrtp.config.SafetySettings;
import com.skyblockexp.ezrtp.config.SearchPattern;
import com.skyblockexp.ezrtp.platform.PlatformRuntime;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformWorldAccess;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationFinderSurfaceScanDepthTest {

    @Test
    void deepOceanSearchWindowExpandsWhenDepthIsIncreased() throws Exception {
        LocationFinder finder = createLocationFinder();
        Method method = LocationFinder.class.getDeclaredMethod(
            "resolveSearchMinY", World.class, int.class, int.class, RandomTeleportSettings.class);
        method.setAccessible(true);

        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        int defaultWindowMinY = (Integer) method.invoke(finder, world, 100, 0, buildSettingsWithSurfaceScanDepth(20));
        int deepWindowMinY = (Integer) method.invoke(finder, world, 100, 0, buildSettingsWithSurfaceScanDepth(30));

        assertEquals(80, defaultWindowMinY, "20 should preserve historical 20-block scan behavior");
        assertEquals(70, deepWindowMinY, "30 should allow deeper scan for deep-ocean style candidates");
    }

    @Test
    void tallColumnWindowUsesConservativeUpperBound() throws Exception {
        LocationFinder finder = createLocationFinder();
        Method method = LocationFinder.class.getDeclaredMethod(
            "resolveSearchMinY", World.class, int.class, int.class, RandomTeleportSettings.class);
        method.setAccessible(true);

        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
        int boundedMinY = (Integer) method.invoke(finder, world, 220, 0, buildSettingsWithSurfaceScanDepth(10_000));

        assertEquals(92, boundedMinY, "Extremely large configured depth should be clamped to a conservative maximum");
    }

    @Test
    void netherRoofStartKeepsExistingShallowRecoveryWindow() throws Exception {
        LocationFinder finder = createLocationFinder();
        Method method = LocationFinder.class.getDeclaredMethod(
            "resolveSearchMinY", World.class, int.class, int.class, RandomTeleportSettings.class);
        method.setAccessible(true);

        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.NETHER);
        RandomTeleportSettings settings = buildSettingsWithSurfaceScanDepth(20, 20);

        int searchMinY = (Integer) method.invoke(finder, world, 126, 0, settings);

        assertEquals(106, searchMinY, "Nether roof candidates should still scan 20 blocks by default override values");
    }

    @Test
    void netherRoofStartExpandsRecoveryWindowForDeeperFloor() throws Exception {
        LocationFinder finder = createLocationFinder();
        Method method = LocationFinder.class.getDeclaredMethod(
            "resolveSearchMinY", World.class, int.class, int.class, RandomTeleportSettings.class);
        method.setAccessible(true);

        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.NETHER);
        RandomTeleportSettings settings = buildSettingsWithSurfaceScanDepth(20, 64);

        int searchMinY = (Integer) method.invoke(finder, world, 126, 0, settings);

        assertEquals(62, searchMinY, "Nether-specific depth should include deep floors that are below the legacy 20-block window");
    }

    @Test
    void endWorldUsesOverworldSurfaceScanDepth() throws Exception {
        LocationFinder finder = createLocationFinder();
        Method method = LocationFinder.class.getDeclaredMethod(
            "resolveSearchMinY", World.class, int.class, int.class, RandomTeleportSettings.class);
        method.setAccessible(true);

        World world = mock(World.class);
        when(world.getEnvironment()).thenReturn(World.Environment.THE_END);
        RandomTeleportSettings settings = buildSettingsWithSurfaceScanDepth(30, 64);

        int searchMinY = (Integer) method.invoke(finder, world, 100, 0, settings);

        assertEquals(70, searchMinY, "The End should use the main surface scan depth to match non-nether RTP behavior");
    }

    @Test
    void resolveSurfaceScanDepthUsesNetherOverrideOnlyInNether() throws Exception {
        LocationFinder finder = createLocationFinder();
        Method method = LocationFinder.class.getDeclaredMethod(
            "resolveSurfaceScanDepth", World.class, RandomTeleportSettings.class);
        method.setAccessible(true);

        RandomTeleportSettings settings = buildSettingsWithSurfaceScanDepth(24, 64);
        World overworld = mock(World.class);
        World nether = mock(World.class);
        World end = mock(World.class);
        when(overworld.getEnvironment()).thenReturn(World.Environment.NORMAL);
        when(nether.getEnvironment()).thenReturn(World.Environment.NETHER);
        when(end.getEnvironment()).thenReturn(World.Environment.THE_END);

        int overworldDepth = (Integer) method.invoke(finder, overworld, settings);
        int netherDepth = (Integer) method.invoke(finder, nether, settings);
        int endDepth = (Integer) method.invoke(finder, end, settings);

        assertEquals(24, overworldDepth, "Overworld should use safety.recovery.max-surface-scan-depth");
        assertEquals(64, netherDepth, "Nether should use safety.recovery.max-surface-scan-depth-nether");
        assertEquals(24, endDepth, "The End should keep using safety.recovery.max-surface-scan-depth");
    }

    private static RandomTeleportSettings buildSettingsWithSurfaceScanDepth(int depth) {
        return buildSettingsWithSurfaceScanDepth(depth, 128);
    }

    private static RandomTeleportSettings buildSettingsWithSurfaceScanDepth(int depth, int netherDepth) {
        SafetySettings safety = new SafetySettings(false, true, 6, depth, netherDepth, Material.DIRT);
        return new RandomTeleportSettings(
            null, "world", 0, 0, 100, 1000, 10, false,
            Collections.emptySet(), RandomTeleportSettings.TeleportMessages.defaultMessages(), ParticleSettings.disabled(),
            OnJoinTeleportSettings.fromConfiguration(null), CountdownBossBarSettings.disabled(), CountdownParticleSettings.disabled(),
            0.0D, 0, true, false, null, null, Collections.emptySet(), Collections.emptySet(),
            new ProtectionSettings(false, Collections.emptyList()), BiomePreCacheSettings.disabled(), RareBiomeOptimizationSettings.disabled(),
            ChunkLoadingSettings.defaults(), true, BiomeSearchSettings.defaults(), true, true, safety, SearchPattern.RANDOM,
            ChunkyIntegrationSettings.defaults()
        );
    }

    private static LocationFinder createLocationFinder() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        PlatformRuntime platformRuntime = mock(PlatformRuntime.class);
        PlatformScheduler scheduler = mock(PlatformScheduler.class);
        PlatformWorldAccess worldAccess = mock(PlatformWorldAccess.class);
        when(platformRuntime.scheduler()).thenReturn(scheduler);
        when(platformRuntime.worldAccess()).thenReturn(worldAccess);

        return new LocationFinder(
            plugin,
            mock(com.skyblockexp.ezrtp.statistics.RtpStatistics.class),
            mock(com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache.class),
            mock(com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry.class),
            mock(com.skyblockexp.ezrtp.teleport.queue.ChunkLoadQueue.class),
            mock(LocationValidator.class),
            mock(com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy.class),
            platformRuntime,
            null,
            null
        );
    }
}
