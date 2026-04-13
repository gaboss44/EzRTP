package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.queue.TeleportQueueManager;
import org.bukkit.Server;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TeleportExecutorTimeoutTest {

    private TeleportExecutor executor;

    @BeforeEach
    void setUp() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);

        executor = new TeleportExecutor(
            plugin,
            mock(MessageProvider.class),
            mock(RtpStatistics.class),
            mock(TeleportCostCalculator.class),
            mock(CountdownManager.class),
            mock(LocationFinder.class),
            mock(TeleportQueueManager.class),
            () -> null
        );
    }

    @Test
    void resolveSearchTimeoutMillis_nonRareSearchDoesNotUseRareTimeoutWhenStandardIsZero() {
        RandomTeleportSettings settings = settingsWithWallClockLimits(0, 8_000, Set.of(Biome.MUSHROOM_FIELDS));

        long timeoutMillis = executor.resolveSearchTimeoutMillis(settings, false);

        assertEquals(0L, timeoutMillis, "Non-rare searches should keep no wall-clock cap when max-wait-seconds is 0.");
    }

    @Test
    void resolveSearchTimeoutMillis_rareSearchUsesRareTimeoutWithSchedulerGrace() {
        RandomTeleportSettings settings = settingsWithWallClockLimits(0, 8_000, Set.of(Biome.MUSHROOM_FIELDS));

        long timeoutMillis = executor.resolveSearchTimeoutMillis(settings, true);

        assertEquals(13_000L, timeoutMillis);
    }

    @Test
    void resolveSearchTimeoutMillis_bothZeroDisablesTimeoutCap() {
        RandomTeleportSettings settings = settingsWithWallClockLimits(0, 0, Set.of(Biome.PLAINS));

        long timeoutMillisNonRare = executor.resolveSearchTimeoutMillis(settings, false);
        long timeoutMillisRare = executor.resolveSearchTimeoutMillis(settings, true);

        assertEquals(0L, timeoutMillisNonRare);
        assertEquals(0L, timeoutMillisRare);
    }

    private RandomTeleportSettings settingsWithWallClockLimits(int standardMillis,
                                                               int rareMillis,
                                                               Set<Biome> include) {
        BiomeSearchSettings biomeSearchSettings = new BiomeSearchSettings(
            standardMillis,
            rareMillis,
            0,
            0,
            0,
            0,
            1,
            BiomeSearchSettings.FailoverMode.CACHE
        );

        return new RandomTeleportSettings(
            null,
            "world",
            0,
            0,
            100,
            500,
            10,
            false,
            java.util.Collections.emptySet(),
            null,
            null,
            null,
            null,
            null,
            0.0,
            0,
            true,
            false,
            null,
            null,
            include,
            java.util.Collections.emptySet(),
            null,
            null,
            null,
            null,
            true,
            biomeSearchSettings,
            true,
            true,
            null,
            null,
            null
        );
    }
}
