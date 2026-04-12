package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.ChunkLoadingSettings;
import com.skyblockexp.ezrtp.platform.ChunkLoadStrategy;
import com.skyblockexp.ezrtp.platform.PlatformRuntime;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.RareBiomeOptimizationSettings;
import com.skyblockexp.ezrtp.config.SearchPattern;
import com.skyblockexp.ezrtp.config.TeleportQueueSettings;
import com.skyblockexp.ezrtp.economy.EconomyService;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.protection.ProtectionRegistry;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.CircularSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.TriangleSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.DiamondSearchStrategy;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import com.skyblockexp.ezrtp.teleport.search.SquareSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;
import com.skyblockexp.ezrtp.teleport.biome.WeightedRareBiomeStrategy;
import com.skyblockexp.ezrtp.teleport.queue.ChunkLoadQueue;
import com.skyblockexp.ezrtp.teleport.queue.TeleportQueueManager;
import com.skyblockexp.ezrtp.util.DebugFileLogger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
// ChunkyAPI is optional at runtime; keep a runtime reference without a compile-time dependency.

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Thin service wrapper that wires together the teleport helper components.
 */
public final class RandomTeleportService implements com.skyblockexp.ezrtp.api.TeleportService {

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final RtpStatistics statistics = new RtpStatistics();
    private final BiomeLocationCache biomeCache;
    private final RareBiomeRegistry rareBiomeRegistry;
    private final ChunkLoadQueue chunkLoadQueue;
    private final LocationValidator locationValidator;
    private final LocationFinder locationFinder;
    private final CountdownManager countdownManager;
    private final TeleportQueueManager queueManager;
    private final TeleportCostCalculator costCalculator;
    private final TeleportExecutor teleportExecutor;
    private final DebugFileLogger debugFileLogger;

    private final ChunkyProvider chunkyAPI;
    private final com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator chunkyWarmupCoordinator;

    private RandomTeleportSettings settings;
    private TeleportQueueSettings queueSettings;
    private BiomeSearchStrategy searchStrategy;

    public RandomTeleportService(org.bukkit.plugin.java.JavaPlugin plugin,
                                 RandomTeleportSettings settings,
                                 TeleportQueueSettings queueSettings,
                                 EconomyService economyService,
                                 BiFunction<Player, RandomTeleportSettings, Double> costResolver,
                                 ProtectionRegistry protectionRegistry,
                                 MessageProvider messageProvider,
                                 ChunkLoadStrategy chunkLoadStrategy,
                                 PlatformRuntime platformRuntime,
                                 ChunkyProvider chunkyAPI,
                                 com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator chunkyWarmupCoordinator) {
        this.plugin = plugin;
        this.chunkyAPI = chunkyAPI;
        this.chunkyWarmupCoordinator = chunkyWarmupCoordinator;
        this.debugFileLogger = new DebugFileLogger(plugin);
        this.settings = settings;
        this.queueSettings = queueSettings != null ? queueSettings : TeleportQueueSettings.disabled();

        this.biomeCache = createBiomeCache(settings);
        RareBiomeOptimizationSettings rareSettings = settings != null ? settings.getRareBiomeOptimizationSettings() : null;
        this.rareBiomeRegistry = new RareBiomeRegistry(plugin, rareSettings != null ? rareSettings.getRareBiomes() : null);
        this.chunkLoadQueue = new ChunkLoadQueue(plugin, chunkLoadStrategy, platformRuntime.scheduler());
        this.locationValidator = new LocationValidator(plugin, protectionRegistry);
        this.searchStrategy = createPatternStrategy(settings);
        this.locationFinder = new LocationFinder(plugin, statistics, biomeCache, rareBiomeRegistry, chunkLoadQueue, locationValidator, searchStrategy, platformRuntime, chunkyAPI, chunkyWarmupCoordinator);
        this.countdownManager = new CountdownManager(plugin, messageProvider);
        this.queueManager = new TeleportQueueManager(plugin, this.queueSettings, messageProvider);
        this.costCalculator = new TeleportCostCalculator(economyService, costResolver);
        this.teleportExecutor = new TeleportExecutor(plugin, messageProvider, statistics,
                costCalculator, countdownManager, locationFinder, queueManager, () -> this.settings);

        applyChunkLoadingSettings(this.settings);
        applyRareBiomeSettings(this.settings);
        applyCacheSettings(this.settings);
    }

    public CompletableFuture<Location> generateSafeLocationForChunk(org.bukkit.World world, int chunkX, int chunkZ, RandomTeleportSettings teleportSettings) {
        if (world == null || teleportSettings == null) return CompletableFuture.completedFuture(null);
        return locationFinder.generateSafeLocationForChunk(world, chunkX, chunkZ, teleportSettings);
    }

    public void reload(RandomTeleportSettings newSettings, TeleportQueueSettings newQueueSettings) {
        this.settings = newSettings;
        this.queueSettings = newQueueSettings != null ? newQueueSettings : TeleportQueueSettings.disabled();
        queueManager.reload(this.queueSettings);

        applyChunkLoadingSettings(newSettings);
        applyRareBiomeSettings(newSettings);
        applyCacheSettings(newSettings);
    }

    public void setEconomyService(EconomyService economyService) {
        costCalculator.setEconomyService(economyService);
    }

    public void setCostResolver(BiFunction<Player, RandomTeleportSettings, Double> costResolver) {
        costCalculator.setCostResolver(costResolver);
    }

    public void setProtectionRegistry(ProtectionRegistry protectionRegistry) {
        locationValidator.setProtectionRegistry(protectionRegistry);
    }

    public RtpStatistics getStatistics() {
        return statistics;
    }

    public BiomeLocationCache getBiomeCache() {
        return biomeCache;
    }

    public RareBiomeRegistry getRareBiomeRegistry() {
        return rareBiomeRegistry;
    }

    public ChunkLoadQueue getChunkLoadQueue() {
        return chunkLoadQueue;
    }

    public void shutdown() {
        teleportExecutor.shutdown();
        biomeCache.shutdown();
        rareBiomeRegistry.shutdown();
        chunkLoadQueue.shutdown();
    }

    public CompletableFuture<Location> generateSafeLocationForCache(World world, RandomTeleportSettings teleportSettings) {
        return locationFinder.generateSafeLocationForCache(world, teleportSettings);
    }

    public void teleportPlayer(@NotNull Player player, @NotNull TeleportReason reason) {
        teleportExecutor.teleportPlayer(player, reason);
    }

    @Override
    public void teleportPlayer(@NotNull Player player, Object settings, @NotNull TeleportReason reason) {
        if (settings instanceof RandomTeleportSettings) {
            teleportPlayer(player, (RandomTeleportSettings) settings, reason);
        } else {
            teleportPlayer(player, reason);
        }
    }

    public void teleportPlayer(@NotNull Player player,
                               RandomTeleportSettings teleportSettings,
                               @NotNull TeleportReason reason) {
        teleportExecutor.teleportPlayer(player, teleportSettings, reason);
    }

    public void teleportPlayer(@NotNull Player player,
                               @NotNull TeleportReason reason,
                               Consumer<Boolean> callback) {
        teleportExecutor.teleportPlayer(player, reason, callback);
    }

    public void teleportPlayer(@NotNull Player player,
                               RandomTeleportSettings teleportSettings,
                               @NotNull TeleportReason reason,
                               Consumer<Boolean> callback) {
        teleportExecutor.teleportPlayer(player, teleportSettings, reason, callback);
    }

    
    public void teleportPlayer(@NotNull Player player, Object settings, @NotNull TeleportReason reason, Consumer<Boolean> callback) {
        if (settings instanceof RandomTeleportSettings) {
            teleportPlayer(player, (RandomTeleportSettings) settings, reason, callback);
        } else {
            teleportPlayer(player, reason, callback);
        }
    }

    private void applyCacheSettings(RandomTeleportSettings currentSettings) {
        if (currentSettings != null && currentSettings.getPreCacheSettings() != null) {
            boolean shouldEnable = currentSettings.getPreCacheSettings().isEnabled();
            boolean hasBiomeFilters = LocationValidator.hasBiomeFilters(currentSettings);
            
            // Check memory before enabling cache
            Runtime runtime = Runtime.getRuntime();
            long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
            long totalMemoryMb = runtime.totalMemory() / (1024L * 1024L);
            
            if (freeMemoryMb < 512) { // Less than 512MB free memory
                if (shouldEnable) {
                    plugin.getLogger().warning(String.format(
                        "[EzRTP] Disabling biome pre-cache due to low memory: %dMB free / %dMB total",
                        freeMemoryMb, totalMemoryMb
                    ));
                    shouldEnable = false;
                }
            }
            
            if (!shouldEnable && hasBiomeFilters && currentSettings.getPreCacheSettings().isAutoEnableForFilters()) {
                if (freeMemoryMb >= 512) { // Only auto-enable if we have enough memory
                    shouldEnable = true;
                    plugin.getLogger().info(String.format(
                        "[EzRTP] Biome filters detected; auto-enabling pre-cache as configured (Memory: %dMB free / %dMB total)",
                        freeMemoryMb, totalMemoryMb
                    ));
                } else {
                    plugin.getLogger().warning(String.format(
                        "[EzRTP] Biome filters detected but pre-cache not enabled due to low memory: %dMB free / %dMB total",
                        freeMemoryMb, totalMemoryMb
                    ));
                }
            }

            if (hasBiomeFilters && !shouldEnable) {
                plugin.getLogger().warning("[EzRTP] Biome filtering is configured but pre-caching is disabled.");
                plugin.getLogger().warning("[EzRTP] RECOMMENDATION: Enable 'biomes.pre-cache.enabled: true' in config.yml to improve RTP success rates.");
            }

            biomeCache.setEnabled(shouldEnable);

            if (shouldEnable && hasBiomeFilters) {
                World world = Bukkit.getWorld(currentSettings.getWorldName());
                if (world != null) {
                    // Start Chunky pregeneration for the world if integration is enabled
                    if (chunkyAPI != null && currentSettings.getChunkyIntegrationSettings().isEnabled()) {
                        // Check memory safety before starting Chunky tasks
                        if (chunkyWarmupCoordinator != null && !chunkyWarmupCoordinator.hasSufficientMemory()) {
                            plugin.getLogger().info("[EzRTP] Skipping Chunky pregeneration for biome pre-caching due to low memory");
                        } else {
                            String worldName = world.getName();
                            try {
                                if (!chunkyAPI.isRunning(worldName)) {
                                    int radius = Math.max(10000, (int) (world.getWorldBorder().getSize() / 2));
                                    boolean started = chunkyAPI.startTask(worldName, currentSettings.getChunkyIntegrationSettings().getShape(), 0, 0, radius, radius, currentSettings.getChunkyIntegrationSettings().getPattern());
                                    if (started) {
                                        plugin.getLogger().info("[EzRTP] Started Chunky pregeneration for world '" + worldName + "' to support biome pre-caching.");
                                    }
                                }
                            } catch (Throwable t) {
                                plugin.getLogger().warning("[EzRTP] Failed to interact with Chunky API: " + t.getMessage());
                            }
                        }
                    }
                    plugin.getLogger().info(String.format(
                        "[EzRTP] Starting biome cache warmup for world '%s' (Memory: %dMB free / %dMB total)",
                        world.getName(), freeMemoryMb, totalMemoryMb
                    ));
                    biomeCache.startWarmup(world, currentSettings, this);
                }
            }
        } else {
            biomeCache.setEnabled(false);
        }
    }

    private void applyRareBiomeSettings(RandomTeleportSettings currentSettings) {
        RareBiomeOptimizationSettings rareSettings = currentSettings != null ? currentSettings.getRareBiomeOptimizationSettings() : null;
        boolean chunkQueueOverridden = false;
        BiomeSearchStrategy patternStrategy = createPatternStrategy(currentSettings);

        if (rareSettings != null) {
            boolean hasBiomeFilters = LocationValidator.hasBiomeFilters(currentSettings);
            boolean enabled = rareSettings.isEnabled();
            if (!enabled && hasBiomeFilters && rareSettings.isAutoEnableForFilters()) {
                enabled = true;
                plugin.getLogger().info("[EzRTP] Biome filters detected; auto-enabling rare biome optimizations as configured.");
            }
            rareBiomeRegistry.setEnabled(enabled && rareSettings.isHotspotTrackingEnabled());
            rareBiomeRegistry.setBackgroundScanningEnabled(enabled && rareSettings.isBackgroundScanningEnabled());
            if (enabled && rareSettings.useChunkLoadQueue()) {
                chunkLoadQueue.configure(rareSettings.getChunkProcessingIntervalTicks(), rareSettings.getMaxChunksPerTick());
                chunkLoadQueue.setEnabled(true);
                chunkQueueOverridden = true;
            }
            if (enabled && rareSettings.useWeightedSearch()) {
                this.searchStrategy = new WeightedRareBiomeStrategy(patternStrategy);
            } else {
                this.searchStrategy = patternStrategy;
            }
        } else {
            rareBiomeRegistry.setEnabled(false);
            rareBiomeRegistry.setBackgroundScanningEnabled(false);
            this.searchStrategy = patternStrategy;
        }

        if (!chunkQueueOverridden) {
            applyChunkLoadingSettings(currentSettings);
        }

        locationFinder.setSearchStrategy(this.searchStrategy);
        logSearchConfiguration(currentSettings, patternStrategy, this.searchStrategy);
    }

    private void applyChunkLoadingSettings(RandomTeleportSettings currentSettings) {
        ChunkLoadingSettings chunkSettings = currentSettings != null
            ? currentSettings.getChunkLoadingSettings()
            : ChunkLoadingSettings.defaults();

        if (chunkSettings != null && chunkSettings.isEnabled()) {
            chunkLoadQueue.configure(chunkSettings.getProcessingIntervalTicks(), chunkSettings.getMaxChunksPerTick());
            chunkLoadQueue.setEnabled(true);
        } else {
            chunkLoadQueue.setEnabled(false);
        }

        // Configure memory safety for chunk loading
        if (currentSettings != null && currentSettings.getChunkyIntegrationSettings() != null) {
            var chunkySettings = currentSettings.getChunkyIntegrationSettings();
            if (chunkySettings.isMemorySafetyEnabled()) {
                chunkLoadQueue.configureMemorySafety(chunkySettings.getMinFreeMemoryMb());
            }
        }
    }

    private BiomeSearchStrategy createPatternStrategy(RandomTeleportSettings currentSettings) {
        SearchPattern pattern = currentSettings != null ? currentSettings.getSearchPattern() : SearchPattern.RANDOM;
        return switch (pattern) {
            case CIRCLE -> new CircularSearchStrategy();
            case DIAMOND -> new DiamondSearchStrategy();
            case TRIANGLE -> new TriangleSearchStrategy();
            case SQUARE -> new SquareSearchStrategy();
            default -> new UniformSearchStrategy();
        };
    }

    private BiomeLocationCache createBiomeCache(RandomTeleportSettings initialSettings) {
        if (initialSettings != null && initialSettings.getPreCacheSettings() != null) {
            var preCache = initialSettings.getPreCacheSettings();
            BiomeLocationCache cache = new BiomeLocationCache(
                    plugin,
                    preCache.getMaxPerBiome(),
                    preCache.getWarmupSize(),
                    preCache.getExpirationMinutes(),
                    this.chunkyWarmupCoordinator);
            cache.setEnabled(preCache.isEnabled());
            return cache;
        }

        BiomeLocationCache cache = new BiomeLocationCache(plugin, BiomeLocationCache.DEFAULT_MAX_LOCATIONS_PER_BIOME, BiomeLocationCache.DEFAULT_CACHE_WARMUP_SIZE, BiomeLocationCache.DEFAULT_EXPIRATION_MINUTES, this.chunkyWarmupCoordinator);
        cache.setEnabled(false);
        return cache;
    }

    private void logSearchConfiguration(RandomTeleportSettings currentSettings,
                                        BiomeSearchStrategy patternStrategy,
                                        BiomeSearchStrategy activeStrategy) {
        if (currentSettings == null) {
            debugFileLogger.log("Search configuration unavailable (settings are null).");
            return;
        }

        SearchPattern pattern = currentSettings.getSearchPattern() != null
                ? currentSettings.getSearchPattern()
                : SearchPattern.RANDOM;
        boolean debugEnabled = currentSettings.isDebugRejectionLoggingEnabled();
        boolean weightedSearchActive = activeStrategy instanceof WeightedRareBiomeStrategy;
        boolean rareOptimizationEnabled = currentSettings.getRareBiomeOptimizationSettings() != null
                && currentSettings.getRareBiomeOptimizationSettings().isEnabled();
        boolean biomeFiltersActive = LocationValidator.hasBiomeFilters(currentSettings);

        StringBuilder sb = new StringBuilder();
        sb.append("[EzRTP] Search strategy configured: pattern=").append(pattern.getConfigKey());
        sb.append(", baseStrategy=").append(patternStrategy != null ? patternStrategy.getClass().getSimpleName() : "none");
        sb.append(", activeStrategy=").append(activeStrategy != null ? activeStrategy.getClass().getSimpleName() : "none");
        sb.append(", weightedRareSearch=").append(weightedSearchActive);
        sb.append(", rareOptimizationEnabled=").append(rareOptimizationEnabled);
        sb.append(", biomeFiltersActive=").append(biomeFiltersActive);
        sb.append(", debugRejectionLogging=").append(debugEnabled);
        debugFileLogger.log(sb.toString());
    }
}
