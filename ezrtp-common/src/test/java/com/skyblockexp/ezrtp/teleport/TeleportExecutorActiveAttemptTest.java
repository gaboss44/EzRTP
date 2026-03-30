package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.queue.TeleportQueueManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeleportExecutorActiveAttemptTest {

    @Test
    void burstRequestsForSamePlayerOnlyEmitOneSearchingAndSuccessSequence() throws Exception {
        JavaPlugin plugin = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        MessageProvider messageProvider = mock(MessageProvider.class);
        RtpStatistics statistics = mock(RtpStatistics.class);
        TeleportCostCalculator costCalculator = mock(TeleportCostCalculator.class);
        CountdownManager countdownManager = mock(CountdownManager.class);
        LocationFinder locationFinder = mock(LocationFinder.class);
        TeleportQueueManager queueManager = mock(TeleportQueueManager.class);
        Player player = mock(Player.class);
        World world = mock(World.class);
        Block block = mock(Block.class);

        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        when(player.getName()).thenReturn("BurstTester");
        when(player.isOnline()).thenReturn(true);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(new Location(world, 10.0D, 70.0D, 10.0D));
        when(player.teleport(any(Location.class))).thenReturn(true);
        when(world.getName()).thenReturn("world");
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(block);
        when(world.getBlockAt(any(Location.class))).thenReturn(block);
        when(block.getBiome()).thenReturn(Biome.PLAINS);

        when(costCalculator.calculateCost(eq(player), any(RandomTeleportSettings.class))).thenReturn(0.0D);
        when(costCalculator.requiresPayment(any(TeleportReason.class), eq(0.0D))).thenReturn(false);
        when(queueManager.enqueueIfNeeded(eq(player), any(RandomTeleportSettings.class), eq(TeleportReason.COMMAND))).thenReturn(false);
        when(messageProvider.format(any(MessageKey.class), any())).thenReturn(Component.text("msg"));
        when(messageProvider.format(any(MessageKey.class), any(), any())).thenReturn(Component.text("msg"));

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTask(eq(plugin), any(Runnable.class));
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTaskLater(eq(plugin), any(Runnable.class), anyLong());

        CompletableFuture<SearchResult> pendingSearch = new CompletableFuture<>();
        when(locationFinder.findSafeLocationAsync(eq(world), any(RandomTeleportSettings.class))).thenReturn(pendingSearch);
        when(locationFinder.isRareSearch(any(RandomTeleportSettings.class))).thenReturn(false);

        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(4);
            onComplete.run();
            return null;
        }).when(countdownManager).startCountdown(eq(player), any(RandomTeleportSettings.class), eq(TeleportReason.COMMAND), any(), any(Runnable.class));

        TeleportExecutor executor = new TeleportExecutor(
            plugin,
            messageProvider,
            statistics,
            costCalculator,
            countdownManager,
            locationFinder,
            queueManager,
            this::basicSettings
        );

        try (MockedStatic<Bukkit> bukkit = org.mockito.Mockito.mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
            CountDownLatch firstTeleportCompleted = new CountDownLatch(1);
            AtomicBoolean firstTeleportSuccess = new AtomicBoolean(false);

            executor.teleportPlayer(player, TeleportReason.COMMAND, success -> {
                firstTeleportSuccess.set(success);
                firstTeleportCompleted.countDown();
            });
            executor.teleportPlayer(player, TeleportReason.COMMAND);

            pendingSearch.complete(new SearchResult(
                Optional.of(new Location(world, 200.0D, 80.0D, -150.0D)),
                false,
                false
            ));
            assertTrue(firstTeleportCompleted.await(2, TimeUnit.SECONDS));
            assertTrue(firstTeleportSuccess.get());
        }

        verify(locationFinder, times(1)).findSafeLocationAsync(eq(world), any(RandomTeleportSettings.class));
        verify(messageProvider, times(1)).format(eq(MessageKey.TELEPORTING), eq(player));
        verify(player, times(1)).teleport(any(Location.class));
        verify(messageProvider, never()).format(eq(MessageKey.TELEPORT_FAILED), eq(player));
    }

    private RandomTeleportSettings basicSettings() {
        BiomeSearchSettings biomeSearchSettings = new BiomeSearchSettings(
            10_000,
            15_000,
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
            Set.of(Biome.PLAINS),
            java.util.Collections.emptySet(),
            null,
            null,
            null,
            null,
            true,
            biomeSearchSettings,
            null,
            null,
            null
        );
    }
}
