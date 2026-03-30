package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.ParticleSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.SafetySettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.queue.TeleportQueueManager;
import com.skyblockexp.ezrtp.teleport.SearchResult.SearchLimitType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import com.skyblockexp.ezrtp.util.BlockCompat;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Coordinates the teleportation process, handling countdowns, queueing, payment,
 * and final teleport execution.
 */
public final class TeleportExecutor {

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final TeleportPreCheckHandler preCheckHandler;
    private final TeleportResultHandler resultHandler;
    private final TeleportQueueManager queueManager;
    private final Supplier<RandomTeleportSettings> defaultSettingsSupplier;
    private final MessageProvider messageProvider;
    private final RtpStatistics statistics;
    private final TeleportCostCalculator costCalculator;
    private final CountdownManager countdownManager;
    private final LocationFinder locationFinder;
    private final Set<UUID> activeTeleportAttempts = ConcurrentHashMap.newKeySet();

    public TeleportExecutor(org.bukkit.plugin.java.JavaPlugin plugin,
                            MessageProvider messageProvider,
                            RtpStatistics statistics,
                            TeleportCostCalculator costCalculator,
                            CountdownManager countdownManager,
                            LocationFinder locationFinder,
                            TeleportQueueManager queueManager,
                            Supplier<RandomTeleportSettings> defaultSettingsSupplier) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        this.preCheckHandler = new TeleportPreCheckHandler(plugin, messageProvider, costCalculator);
        this.resultHandler = new TeleportResultHandler(plugin, messageProvider, statistics, locationFinder);
        this.queueManager = queueManager;
        this.defaultSettingsSupplier = defaultSettingsSupplier;
        this.messageProvider = messageProvider;
        this.statistics = statistics;
        this.costCalculator = costCalculator;
        this.countdownManager = countdownManager;
        this.locationFinder = locationFinder;

        this.queueManager.setExecutionHandler(this::teleportQueuedPlayer);
    }

    public void teleportPlayer(Player player, TeleportReason reason) {
        teleportPlayer(player, defaultSettingsSupplier.get(), reason, null);
    }

    public void teleportPlayer(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
        teleportPlayer(player, teleportSettings, reason, null);
    }

    public void teleportPlayer(Player player, TeleportReason reason, Consumer<Boolean> callback) {
        teleportPlayer(player, defaultSettingsSupplier.get(), reason, callback);
    }

    public void teleportPlayer(Player player,
                               RandomTeleportSettings teleportSettings,
                               TeleportReason reason,
                               Consumer<Boolean> callback) {
        UUID playerId = player.getUniqueId();
        if (!activeTeleportAttempts.add(playerId)) {
            if (callback != null) callback.accept(false);
            return;
        }

        boolean handedOff = false;
        try {
            if (teleportSettings == null) {
                if (callback != null) callback.accept(false);
                return;
            }

            Consumer<Boolean> trackedCallback = withActiveAttemptCleanup(playerId, callback);

            // Critical memory check - prevent teleport if memory is critically low
            if (!preCheckHandler.checkMemorySafety(player, trackedCallback)) {
                return;
            }

            // Check payment before queuing
            if (!preCheckHandler.checkPayment(player, teleportSettings, reason)) {
                trackedCallback.accept(false);
                return;
            }

            if (queueManager.enqueueIfNeeded(player, teleportSettings, reason)) {
                trackedCallback.accept(false);
                return;
            }

            handedOff = true;
            countdownManager.startCountdown(player, teleportSettings, reason, trackedCallback,
                () -> performTeleport(player, teleportSettings, reason, trackedCallback, () -> clearActiveAttempt(playerId)));
        } finally {
            if (!handedOff) {
                clearActiveAttempt(playerId);
            }
        }
    }

    private void teleportQueuedPlayer(Player player,
                                      RandomTeleportSettings teleportSettings,
                                      TeleportReason reason,
                                      Runnable completionHook) {
        UUID playerId = player.getUniqueId();
        if (!activeTeleportAttempts.add(playerId)) {
            if (completionHook != null) {
                completionHook.run();
            }
            return;
        }

        Runnable trackedCompletionHook = () -> {
            try {
                if (completionHook != null) {
                    completionHook.run();
                }
            } finally {
                clearActiveAttempt(playerId);
            }
        };
        performTeleport(player, teleportSettings, reason, null, trackedCompletionHook);
    }

    private Consumer<Boolean> withActiveAttemptCleanup(UUID playerId, Consumer<Boolean> callback) {
        return success -> {
            try {
                if (callback != null) {
                    callback.accept(success);
                }
            } finally {
                clearActiveAttempt(playerId);
            }
        };
    }

    private void clearActiveAttempt(UUID playerId) {
        activeTeleportAttempts.remove(playerId);
    }

    private void performTeleport(Player player,
                                 RandomTeleportSettings teleportSettings,
                                 TeleportReason reason,
                                 Consumer<Boolean> callback,
                                 Runnable completionHook) {
        World world = Bukkit.getWorld(teleportSettings.getWorldName());
        if (world == null) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.WORLD_MISSING,
                Map.of("world", teleportSettings.getWorldName()), player));
            if (callback != null) callback.accept(false);
            if (completionHook != null) completionHook.run();
            return;
        }

        if (reason == TeleportReason.JOIN) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.JOIN_SEARCHING, player));
        } else {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORTING, player));
        }

        long startTime = System.currentTimeMillis();
        CompletableFuture<SearchResult> locationSearchFuture = locationFinder.findSafeLocationAsync(world, teleportSettings);
        boolean rareSearch = locationFinder.isRareSearch(teleportSettings);
        long searchTimeoutMillis = resolveSearchTimeoutMillis(teleportSettings, rareSearch);
        CompletableFuture<SearchResult> completionFuture = locationSearchFuture;
        if (searchTimeoutMillis > 0) {
            completionFuture = locationSearchFuture.orTimeout(searchTimeoutMillis, TimeUnit.MILLISECONDS);
        }

        completionFuture.whenComplete((result, throwable) -> {
            Runnable task = () -> {
                long duration = System.currentTimeMillis() - startTime;
                boolean success = false;
                org.bukkit.block.Biome biome = null;
                boolean cacheHit = result != null && result.cacheHit();
                boolean cacheChecked = result != null && result.cacheChecked();
                Location validLocation = null;

                try {
                    if (throwable != null || result == null || result.location().isEmpty()) {
                        if (throwable != null && teleportSettings.isDebugRejectionLoggingEnabled()) {
                            plugin.getLogger().warning("EzRTP debug: location search failed for player '"
                                    + player.getName() + "' in world '" + world.getName() + "' after "
                                    + duration + "ms: " + throwable.getClass().getSimpleName() + " - "
                                    + throwable.getMessage());
                        }
                        resultHandler.handleFailure(player, teleportSettings, result, duration, biome, cacheHit, cacheChecked);
                        if (callback != null) callback.accept(false);
                        return;
                    }

                    validLocation = result.location().get();

                    if (!player.isOnline()) {
                        locationFinder.cacheValidLocation(validLocation, teleportSettings);
                        statistics.recordPlayerOfflineOrCancelledFailure();
                        statistics.recordAttempt(false, duration, biome, cacheHit, cacheChecked);
                        if (callback != null) callback.accept(false);
                        return;
                    }

                    double resolvedCost = costCalculator.calculateCost(player, teleportSettings);
                    if (costCalculator.requiresPayment(reason, resolvedCost)
                            && !costCalculator.withdraw(player, resolvedCost)) {
                        locationFinder.cacheValidLocation(validLocation, teleportSettings);
                        com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.INSUFFICIENT_FUNDS,
                            Map.of("cost", String.format(java.util.Locale.US, "%.2f", resolvedCost)), player));
                        statistics.recordEconomyFailure();
                        statistics.recordAttempt(false, duration, biome, cacheHit, cacheChecked);
                        if (callback != null) callback.accept(false);
                        return;
                    }

                    Location destination = TeleportDestinationAdjuster.adjustForSafety(validLocation, teleportSettings);
                    biome = destination.getBlock().getBiome();
                    Location oldLocation = player.getLocation(); // Store old location before teleport
                    success = player.teleport(destination);

                    if (success) {
                        resultHandler.handleSuccess(player, destination, result, teleportSettings, duration, biome, cacheHit, cacheChecked, validLocation);
                    } else {
                        locationFinder.cacheValidLocation(validLocation, teleportSettings);
                        com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FAILED, player));
                        statistics.recordTeleportApiFailure();
                        statistics.recordAttempt(false, duration, biome, cacheHit, cacheChecked);
                    }
                } finally {
                    if (callback != null) {
                        callback.accept(success);
                    }
                    if (completionHook != null) {
                        completionHook.run();
                    }
                }
            };

            try {
                scheduler.runTask(plugin, task);
            } catch (IllegalStateException ex) {
                if (callback != null) callback.accept(false);
                if (completionHook != null) completionHook.run();
            }
        });
    }

    long resolveSearchTimeoutMillis(RandomTeleportSettings settings, boolean rareSearch) {
        if (settings == null || settings.getBiomeSearchSettings() == null) {
            return 30_000L;
        }

        int selectedLimit = rareSearch
            ? settings.getBiomeSearchSettings().getMaxWallClockMillisRare()
            : settings.getBiomeSearchSettings().getMaxWallClockMillis();
        if (selectedLimit <= 0) {
            return 0L;
        }

        // Small grace period for scheduler hand-off and chunk queue processing.
        return Math.max(5_000L, selectedLimit + 5_000L);
    }

    private void handleTeleportFailure(Player player,
                                       RandomTeleportSettings teleportSettings,
                                       SearchResult result,
                                       long duration,
                                       org.bukkit.block.Biome biome,
                                       boolean cacheHit,
                                       boolean cacheChecked,
                                       Consumer<Boolean> callback) {
        resultHandler.handleFailure(player, teleportSettings, result, duration, biome, cacheHit, cacheChecked);
        if (callback != null) callback.accept(false);
    }

    public void shutdown() {
        countdownManager.shutdown();
        queueManager.shutdown();
    }
}
