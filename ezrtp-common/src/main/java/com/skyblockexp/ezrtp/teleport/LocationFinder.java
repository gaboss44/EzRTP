package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.SafetySettings;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;
import com.skyblockexp.ezrtp.teleport.biome.WeightedRareBiomeStrategy;
import com.skyblockexp.ezrtp.teleport.queue.ChunkLoadQueue;
import com.skyblockexp.ezrtp.teleport.SearchResult.SearchLimitType;
import com.skyblockexp.ezrtp.teleport.filter.SearchFilter;
import com.skyblockexp.ezrtp.teleport.filter.SearchFilterChain;
import com.skyblockexp.ezrtp.teleport.filter.SearchFilterResult;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationCause;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import com.skyblockexp.ezrtp.util.DebugFileLogger;

import org.bukkit.Location;
import org.bukkit.World;
import com.skyblockexp.ezrtp.platform.PlatformRuntime;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformWorldAccess;
import org.bukkit.block.Block;
import com.skyblockexp.ezrtp.util.BlockCompat;
import org.bukkit.block.BlockFace;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
// ChunkyAPI is optional at runtime; use Object and ChunkyAdapter for runtime calls

/**
 * Handles the asynchronous finding of safe teleport locations.
 */
public final class LocationFinder {

    private static final int NETHER_ROOF_Y = 127;
    private static final int DEFAULT_SURFACE_SCAN_DEPTH = 20;
    private static final int MAX_SURFACE_SCAN_DEPTH = 128;

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final PlatformScheduler scheduler;
    private final RtpStatistics statistics;
    private final BiomeLocationCache biomeCache;
    private final RareBiomeRegistry rareBiomeRegistry;
    private final ChunkLoadQueue chunkLoadQueue;
    private final LocationValidator validator;
    private final SearchFilterChain searchFilterChain;
    private volatile BiomeSearchStrategy searchStrategy;
    private final ChunkyProvider chunkyAPI;
    private final com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator chunkyWarmupCoordinator;
    private final PlatformWorldAccess platformWorldAccess;
    private final DebugFileLogger debugFileLogger;
    private final Executor biomeFilterExecutor;
    private volatile UnsafeLocationMonitor unsafeMonitor;

    public LocationFinder(org.bukkit.plugin.java.JavaPlugin plugin,
                          RtpStatistics statistics,
                          BiomeLocationCache biomeCache,
                          RareBiomeRegistry rareBiomeRegistry,
                          ChunkLoadQueue chunkLoadQueue,
                          LocationValidator validator,
                          BiomeSearchStrategy searchStrategy,
                          PlatformRuntime platformRuntime,
                          ChunkyProvider chunkyAPI,
                          com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator chunkyWarmupCoordinator) {
        this(plugin, statistics, biomeCache, rareBiomeRegistry, chunkLoadQueue, validator, searchStrategy,
            platformRuntime, chunkyAPI, chunkyWarmupCoordinator, null);
    }

    public LocationFinder(org.bukkit.plugin.java.JavaPlugin plugin,
                          RtpStatistics statistics,
                          BiomeLocationCache biomeCache,
                          RareBiomeRegistry rareBiomeRegistry,
                          ChunkLoadQueue chunkLoadQueue,
                          LocationValidator validator,
                          BiomeSearchStrategy searchStrategy,
                          PlatformRuntime platformRuntime,
                          ChunkyProvider chunkyAPI,
                          com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator chunkyWarmupCoordinator,
                          Executor biomeFilterExecutor) {
        this.plugin = plugin;
        this.scheduler = platformRuntime != null ? platformRuntime.scheduler() : null;
        this.statistics = statistics;
        this.biomeCache = biomeCache;
        this.rareBiomeRegistry = rareBiomeRegistry;
        this.chunkLoadQueue = chunkLoadQueue;
        this.validator = validator;
        this.searchStrategy = searchStrategy;
        this.chunkyAPI = chunkyAPI;
        this.chunkyWarmupCoordinator = chunkyWarmupCoordinator;
        this.platformWorldAccess = platformRuntime != null ? platformRuntime.worldAccess() : null;
        this.biomeFilterExecutor = biomeFilterExecutor;
        this.searchFilterChain = createSearchFilterChain();
        this.debugFileLogger = new DebugFileLogger(plugin);
        if (this.scheduler == null) {
            throw new IllegalArgumentException("PlatformRuntime with scheduler is required");
        }
    }

    public void setSearchStrategy(BiomeSearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public void setUnsafeLocationMonitor(UnsafeLocationMonitor monitor) {
        this.unsafeMonitor = monitor;
    }

    /**
     * Finds a safe location asynchronously, checking cache first.
     */
    public CompletableFuture<SearchResult> findSafeLocationAsync(World world, RandomTeleportSettings currentSettings) {
        if (world == null || currentSettings == null) {
            return CompletableFuture.completedFuture(new SearchResult(Optional.empty(), false, false, false, false, false, SearchLimitType.NONE));
        }

        // Check memory safety before starting expensive search operations
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
        if (chunkyWarmupCoordinator != null && !chunkyWarmupCoordinator.hasSufficientMemory()) {
            plugin.getLogger().warning(String.format("[EzRTP] Low memory detected (%dMB free), using cache-only mode", freeMemoryMb));
            boolean cacheChecked = false;
            // Try cache-only search first
            if (biomeCache.isEnabled() && LocationValidator.hasBiomeFilters(currentSettings)) {
                cacheChecked = true;
                Location cachedLocation = biomeCache.get(world, currentSettings.getBiomeInclude(), currentSettings.getBiomeExclude());
                if (cachedLocation != null && validator.isSafe(cachedLocation, currentSettings)
                        && !validator.isProtectedByClaims(cachedLocation, currentSettings)) {
                    return CompletableFuture.completedFuture(new SearchResult(Optional.of(cachedLocation), false, true, true, false, false, SearchLimitType.NONE));
                }
            }
            // Try fallback cache
            if (currentSettings.isEnableFallbackToCache() && biomeCache.isEnabled()) {
                cacheChecked = true;
                Location cachedLocation = getFallbackCachedLocation(world, currentSettings, false);
                if (cachedLocation != null) {
                    return CompletableFuture.completedFuture(new SearchResult(Optional.of(cachedLocation), false, true, true, true, false, SearchLimitType.NONE));
                }
            }
            // Memory too low for search
            return CompletableFuture.completedFuture(new SearchResult(Optional.empty(), false, false, cacheChecked, false, false, SearchLimitType.NONE));
        }

        // Reduce max attempts if memory is getting low (between 256MB and 512MB free)
        RandomTeleportSettings adjustedSettings = currentSettings;
        if (freeMemoryMb < 512) {
            int reducedAttempts = Math.max(8, currentSettings.getMaxAttempts() / 4); // At least 8 attempts, max 1/4 of normal
            if (reducedAttempts < currentSettings.getMaxAttempts()) {
                plugin.getLogger().info(String.format("[EzRTP] Reducing max attempts from %d to %d due to low memory (%dMB free)",
                    currentSettings.getMaxAttempts(), reducedAttempts, freeMemoryMb));
                // Create a modified settings object with reduced attempts
                adjustedSettings = new com.skyblockexp.ezrtp.config.RandomTeleportSettings(
                    null, // configSection
                    currentSettings.getWorldName(), currentSettings.getCenterX(), currentSettings.getCenterZ(),
                    currentSettings.getMinimumRadius(), currentSettings.getMaximumRadius(), reducedAttempts, currentSettings.useWorldBorderRadius(),
                    currentSettings.getUnsafeBlocks(), currentSettings.getMessages(), currentSettings.getParticleSettings(),
                    currentSettings.getOnJoinTeleportSettings(), currentSettings.getCountdownBossBarSettings(), currentSettings.getCountdownParticleSettings(),
                    currentSettings.getTeleportCost(), currentSettings.getCountdownSeconds(), currentSettings.isCountdownChatMessagesEnabled(), currentSettings.isDebugRejectionLoggingEnabled(),
                    currentSettings.getMinY(), currentSettings.getMaxY(), currentSettings.getBiomeInclude(), currentSettings.getBiomeExclude(),
                    currentSettings.getProtectionSettings(), currentSettings.getPreCacheSettings(), currentSettings.getRareBiomeOptimizationSettings(),
                    currentSettings.getChunkLoadingSettings(), currentSettings.isEnableFallbackToCache(), currentSettings.getBiomeSearchSettings(),
                    currentSettings.isBiomeFilteringEnabled(), currentSettings.isBiomeSystemEnabled(),
                    currentSettings.getSafetySettings(), currentSettings.getSearchPattern(), currentSettings.getChunkyIntegrationSettings()
                );
            }
        }

        boolean cacheChecked = false;
        if (biomeCache.isEnabled() && LocationValidator.hasBiomeFilters(currentSettings)) {
            cacheChecked = true;
            Location cachedLocation = biomeCache.get(world, currentSettings.getBiomeInclude(), currentSettings.getBiomeExclude());
            if (cachedLocation != null && validator.isSafe(cachedLocation, currentSettings)
                    && !validator.isProtectedByClaims(cachedLocation, currentSettings)) {
                return CompletableFuture.completedFuture(new SearchResult(Optional.of(cachedLocation), false, true, true, false, false, SearchLimitType.NONE));
            }
        }

        logSearchStart(world, adjustedSettings);
        SearchContext searchContext = new SearchContext(adjustedSettings.getBiomeSearchSettings(), isRareSearch(adjustedSettings));
        // When async-mode is false, serialise this search through the dedicated single-thread executor
        // so competing searches do not saturate the common fork-join pool.
        if (!adjustedSettings.getBiomeSearchSettings().isAsyncMode() && biomeFilterExecutor != null) {
            CompletableFuture<SearchResult> gate = new CompletableFuture<>();
            final RandomTeleportSettings finalSettings = adjustedSettings;
            final boolean finalCacheChecked = cacheChecked;
            biomeFilterExecutor.execute(() ->
                attemptFindLocation(world, finalSettings, 0, false, searchContext, finalCacheChecked)
                    .whenComplete((r, ex) -> {
                        if (ex != null) gate.completeExceptionally(ex);
                        else gate.complete(r);
                    })
            );
            return gate;
        }
        return attemptFindLocation(world, adjustedSettings, 0, false, searchContext, cacheChecked);
    }

    public CompletableFuture<Location> generateSafeLocationForCache(World world, RandomTeleportSettings teleportSettings) {
        if (world == null || teleportSettings == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Apply memory-based adjustments for cache generation too
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
        RandomTeleportSettings adjustedSettings = teleportSettings;
        if (freeMemoryMb < 512) {
            int reducedAttempts = Math.max(4, teleportSettings.getMaxAttempts() / 8); // Even more aggressive for cache generation
            if (reducedAttempts < teleportSettings.getMaxAttempts()) {
                plugin.getLogger().fine(String.format("[EzRTP] Reducing cache generation attempts from %d to %d due to low memory (%dMB free)",
                    teleportSettings.getMaxAttempts(), reducedAttempts, freeMemoryMb));
                adjustedSettings = new com.skyblockexp.ezrtp.config.RandomTeleportSettings(
                    null, // configSection
                    teleportSettings.getWorldName(), teleportSettings.getCenterX(), teleportSettings.getCenterZ(),
                    teleportSettings.getMinimumRadius(), teleportSettings.getMaximumRadius(), reducedAttempts, teleportSettings.useWorldBorderRadius(),
                    teleportSettings.getUnsafeBlocks(), teleportSettings.getMessages(), teleportSettings.getParticleSettings(),
                    teleportSettings.getOnJoinTeleportSettings(), teleportSettings.getCountdownBossBarSettings(), teleportSettings.getCountdownParticleSettings(),
                    teleportSettings.getTeleportCost(), teleportSettings.getCountdownSeconds(), teleportSettings.isCountdownChatMessagesEnabled(), teleportSettings.isDebugRejectionLoggingEnabled(),
                    teleportSettings.getMinY(), teleportSettings.getMaxY(), teleportSettings.getBiomeInclude(), teleportSettings.getBiomeExclude(),
                    teleportSettings.getProtectionSettings(), teleportSettings.getPreCacheSettings(), teleportSettings.getRareBiomeOptimizationSettings(),
                    teleportSettings.getChunkLoadingSettings(), teleportSettings.isEnableFallbackToCache(), teleportSettings.getBiomeSearchSettings(),
                    teleportSettings.isBiomeFilteringEnabled(), teleportSettings.isBiomeSystemEnabled(),
                    teleportSettings.getSafetySettings(), teleportSettings.getSearchPattern(), teleportSettings.getChunkyIntegrationSettings()
                );
            }
        }

        return findSafeLocationAsync(world, adjustedSettings).thenApply(result ->
            result != null && result.location().isPresent() ? result.location().get() : null
        );
    }

    public void cacheValidLocation(Location location, RandomTeleportSettings settings) {
        if (location != null && biomeCache.isEnabled() && LocationValidator.hasBiomeFilters(settings)) {
            biomeCache.cache(location);
        }
    }

    private CompletableFuture<SearchResult> attemptFindLocation(World world,
                                                                 RandomTeleportSettings currentSettings,
                                                                 int attempt,
                                                                 boolean rejectedForBiome,
                                                                 SearchContext context,
                                                                 boolean cacheChecked) {
        if (context.hasLimitBeenHit()) {
            return completeDueToLimit(world, currentSettings, context, cacheChecked);
        }

        int attemptLimit = resolveAttemptLimit(currentSettings, context);
        if (attempt >= attemptLimit) {
            boolean noValidBiome = rejectedForBiome
                    && (!currentSettings.getBiomeInclude().isEmpty() || !currentSettings.getBiomeExclude().isEmpty());

            statistics.recordTimeoutFailure();
            if (noValidBiome && !currentSettings.getBiomeInclude().isEmpty()) {
                currentSettings.getBiomeInclude().forEach(statistics::recordBiomeFailure);
            }

            if (currentSettings.isEnableFallbackToCache() && biomeCache.isEnabled()) {
                boolean attemptedCacheLookup = true;
                Location cachedLocation = getFallbackCachedLocation(world, currentSettings, false);
                if (cachedLocation != null) {
                    statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                    return CompletableFuture.completedFuture(new SearchResult(Optional.of(cachedLocation), false, true, true, true, false, SearchLimitType.NONE));
                }
                statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                return CompletableFuture.completedFuture(new SearchResult(Optional.empty(), noValidBiome, false, attemptedCacheLookup, false, true, SearchLimitType.NONE));
            }

            // Debug logging for why searches fail on certain worlds
            try {
                if (currentSettings.isDebugRejectionLoggingEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("EzRTP debug: search exhausted for world '").append(world.getName()).append("'\n");
                    sb.append("  center=(").append(currentSettings.getCenterX()).append(',').append(currentSettings.getCenterZ()).append(")\n");
                    sb.append("  radius=[").append(currentSettings.getMinimumRadius()).append(',').append(resolveMaximumRadius(world, currentSettings)).append("] useWorldBorder=").append(currentSettings.useWorldBorderRadius()).append("\n");
                    sb.append("  attemptsAllowed=").append(attemptLimit).append(" attemptsUsed=").append(attempt).append("\n");
                    sb.append("  minY=").append(getWorldMinHeight(world)).append(" maxY=").append(getWorldMaxHeight(world)).append("\n");
                    sb.append("  biomeInclude=").append(currentSettings.getBiomeInclude()).append("\n");
                    sb.append("  biomeExclude=").append(currentSettings.getBiomeExclude()).append("\n");
                    sb.append("  rareSearch=").append(isRareSearch(currentSettings)).append("\n");
                    debugFileLogger.log(sb.toString());
                }
            } catch (Exception ignored) {
            }

            statistics.recordBiomeRejectionCount(context.getBiomeRejections());
            return CompletableFuture.completedFuture(new SearchResult(Optional.empty(), noValidBiome, false, cacheChecked, false, false, SearchLimitType.NONE));
        }

        return generateCandidateLocationAsync(world, currentSettings, context).thenCompose(candidate -> {
            if (context.hasLimitBeenHit()) {
                return completeDueToLimit(world, currentSettings, context, cacheChecked);
            }
            SearchFilterResult filterResult = searchFilterChain.apply(candidate, currentSettings);
            if (!filterResult.passed()) {
                SearchFilterResult.RejectionReason reason = filterResult.rejectionReason();
                if (reason == SearchFilterResult.RejectionReason.UNSAFE_OR_NULL_CANDIDATE) {
                    Optional<Location> rescued = attemptSafetyRecovery(candidate, currentSettings);
                    if (rescued.isPresent()) {
                        Location recovered = rescued.get();
                        if (rareBiomeRegistry != null && rareBiomeRegistry.isEnabled()) {
                            org.bukkit.block.Biome biome = recovered.getBlock().getBiome();
                            if (rareBiomeRegistry.isRareBiome(biome)) {
                                rareBiomeRegistry.registerHotspot(recovered);
                            }
                        }
                        statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                        return CompletableFuture.completedFuture(new SearchResult(Optional.of(recovered), false, false, cacheChecked, false, false, SearchLimitType.NONE));
                    }
                    logCandidateRejection(world, currentSettings, attempt, candidate, "unsafe_or_null_candidate");
                    statistics.recordSafetyFailure();
                    recordUnsafeCause(candidate, currentSettings);
                    return attemptFindLocation(world, currentSettings, attempt + 1, rejectedForBiome, context, cacheChecked);
                }

                if (reason == SearchFilterResult.RejectionReason.BIOME_FILTERED) {
                    logCandidateRejection(world, currentSettings, attempt, candidate, "biome_filtered");
                    statistics.recordBiomeAttempt(candidate.getBlock().getBiome());
                    if (biomeCache.isEnabled() && !validator.isProtectedByClaims(candidate, currentSettings)) {
                        biomeCache.cache(candidate);
                    }
                    if (!context.recordBiomeRejection()) {
                        return completeDueToLimit(world, currentSettings, context, cacheChecked);
                    }
                    return attemptFindLocation(world, currentSettings, attempt + 1, true, context, cacheChecked);
                }

                if (reason == SearchFilterResult.RejectionReason.PROTECTED_CLAIM) {
                    logCandidateRejection(world, currentSettings, attempt, candidate, "protected_claim");
                    statistics.recordProtectionFailure();
                    return attemptFindLocation(world, currentSettings, attempt + 1, rejectedForBiome, context, cacheChecked);
                }
            }

            if (rareBiomeRegistry != null && rareBiomeRegistry.isEnabled()) {
                org.bukkit.block.Biome biome = candidate.getBlock().getBiome();
                if (rareBiomeRegistry.isRareBiome(biome)) {
                    rareBiomeRegistry.registerHotspot(candidate);
                }
            }

            statistics.recordBiomeRejectionCount(context.getBiomeRejections());
            return CompletableFuture.completedFuture(new SearchResult(Optional.of(candidate), false, false, cacheChecked, false, false, SearchLimitType.NONE));
        });
    }

    private SearchFilterChain createSearchFilterChain() {
        SearchFilter safetyFilter = (candidate, settings) -> {
            if (candidate == null || !validator.isSafe(candidate, settings)) {
                return SearchFilterResult.rejected(SearchFilterResult.RejectionReason.UNSAFE_OR_NULL_CANDIDATE);
            }
            return SearchFilterResult.pass();
        };

        SearchFilter biomeFilter = (candidate, settings) -> {
            if (!validator.isBiomeAllowed(candidate, settings)) {
                return SearchFilterResult.rejected(SearchFilterResult.RejectionReason.BIOME_FILTERED);
            }
            return SearchFilterResult.pass();
        };

        SearchFilter protectionFilter = (candidate, settings) -> {
            if (validator.isProtectedByClaims(candidate, settings)) {
                return SearchFilterResult.rejected(SearchFilterResult.RejectionReason.PROTECTED_CLAIM);
            }
            return SearchFilterResult.pass();
        };

        return new SearchFilterChain(java.util.List.of(safetyFilter, biomeFilter, protectionFilter));
    }

    private int resolveAttemptLimit(RandomTeleportSettings settings, SearchContext context) {
        int limit = settings != null ? settings.getMaxAttempts() : 0;
        if (settings != null && LocationValidator.hasBiomeFilters(settings)) {
            int minAttempts = settings.getBiomeSearchSettings() != null
                    ? settings.getBiomeSearchSettings().getMinBiomeAttempts()
                    : BiomeSearchSettings.defaults().getMinBiomeAttempts();
            if (context != null && context.getSettings() != null) {
                minAttempts = context.getSettings().getMinBiomeAttempts();
            }
            limit = Math.max(limit, minAttempts);
        }
        return limit;
    }

    private CompletableFuture<Location> generateCandidateLocationAsync(World world,
                                                                       RandomTeleportSettings currentSettings,
                                                                       SearchContext context) {
        if (context.hasLimitBeenHit()) {
            return CompletableFuture.completedFuture(null);
        }

        int min = currentSettings.getMinimumRadius();
        int max = resolveMaximumRadius(world, currentSettings);
        if (max < min) {
            max = min;
        }

        int centerX = currentSettings.getCenterX();
        int centerZ = currentSettings.getCenterZ();
        if (currentSettings.useWorldBorderRadius()) {
            java.util.Optional<Location> center = com.skyblockexp.ezrtp.util.compat.WorldBorderCompat.getCenter(world);
            if (center.isPresent()) {
                Location borderCenter = center.get();
                centerX = (int) Math.round(borderCenter.getX());
                centerZ = (int) Math.round(borderCenter.getZ());
            }
        }

        // Start Chunky pregeneration if enabled and not already running
        if (chunkyAPI != null && currentSettings.getChunkyIntegrationSettings().isEnabled() && currentSettings.getChunkyIntegrationSettings().isAutoPregenerate()) {
            // Check memory safety before starting Chunky tasks
            if (chunkyWarmupCoordinator != null && !chunkyWarmupCoordinator.hasSufficientMemory()) {
                plugin.getLogger().fine("[EzRTP] Skipping Chunky pregeneration due to low memory");
            } else {
                String worldName = world.getName();
                int centerChunkX = centerX >> 4;
                int centerChunkZ = centerZ >> 4;
                int radiusChunks = Math.max(0, max / 16);
                boolean alreadyPlanned = false;
                if (chunkyWarmupCoordinator != null) {
                    alreadyPlanned = chunkyWarmupCoordinator.isChunkPlannedOrGenerated(worldName, centerChunkX, centerChunkZ);
                }
                try {
                    if (!alreadyPlanned && !chunkyAPI.isRunning(worldName)) {
                        boolean started = chunkyAPI.startTask(worldName, currentSettings.getChunkyIntegrationSettings().getShape(), centerX, centerZ, max, max, currentSettings.getChunkyIntegrationSettings().getPattern());
                        if (started && chunkyWarmupCoordinator != null) {
                            chunkyWarmupCoordinator.markRegionPlanned(worldName, centerChunkX, centerChunkZ, radiusChunks);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        BiomeSearchStrategy strategy = this.searchStrategy != null ? this.searchStrategy : new UniformSearchStrategy();
        Measurements.incCandidateGeneration();
        int[] coordinates = strategy.generateCandidateCoordinates(
            world, centerX, centerZ, min, max,
            currentSettings.getBiomeInclude(),
            rareBiomeRegistry
        );

        if (strategy instanceof WeightedRareBiomeStrategy) {
            statistics.recordWeightedSearchUse();
        } else {
            statistics.recordUniformSearchUse();
        }

        int x = coordinates[0];
        int z = coordinates[1];
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        boolean wasChunkLoaded = world.isChunkLoaded(chunkX, chunkZ);

        // Platform fast-path: runtime module decides whether snapshot validation is supported.
        // Snapshot validation is loaded-chunk only. Unloaded chunks skip snapshot checks and are
        // routed through ChunkLoadQueue/ChunkLoadStrategy in the async branch below.
        if (wasChunkLoaded) {
            int highestY = surfaceY(world, x, z);
            int minY = getWorldMinHeight(world);
            if (highestY > minY) {
                Location snapValidated = snapshotValidate(world, x, z, highestY, minY);
                if (snapValidated != null) {
                    CompletableFuture<Location> immediate = new CompletableFuture<>();
                    immediate.complete(snapValidated);
                    return immediate;
                }
            }
        }

        CompletableFuture<Location> future = new CompletableFuture<>();

        if (!wasChunkLoaded) {
            if (!context.tryIncrementChunkLoad()) {
                return CompletableFuture.completedFuture(null);
            }
            if (chunkLoadQueue != null && chunkLoadQueue.isEnabled()) {
                Measurements.incChunkLoadRequest();
                statistics.recordChunkLoadQueueUse();
                chunkLoadQueue.requestChunkLoad(world, chunkX, chunkZ).thenAccept(chunk ->
                    scheduler.executeRegion(world, chunkX, chunkZ, () -> handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future))
                ).exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
            } else {
                scheduler.executeRegion(world, chunkX, chunkZ, () -> {
                    Measurements.incChunkLoadRequest();
                    world.loadChunk(chunkX, chunkZ);
                    handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future);
                });
            }
        } else {
            scheduler.executeRegion(world, chunkX, chunkZ, () -> handleCandidateResolution(world, x, z, chunkX, chunkZ, true, currentSettings, future));
        }

        return future;
    }

    private void handleCandidateResolution(World world,
                                           int x,
                                           int z,
                                           int chunkX,
                                           int chunkZ,
                                           boolean chunkWasLoaded,
                                           RandomTeleportSettings currentSettings,
                                           CompletableFuture<Location> future) {
        try {
            Location candidate = resolveCandidateLocation(world, x, z, currentSettings);
            if (candidate == null) {
                try {
                    if (currentSettings != null && currentSettings.isDebugRejectionLoggingEnabled()) {
                        int highestY = surfaceY(world, x, z);
                        int minY = getWorldMinHeight(world);
                        int maxY = getWorldMaxHeight(world) - 1;
                        StringBuilder sb = new StringBuilder();
                        sb.append("EzRTP debug: candidate null for world '").append(world.getName()).append("'\n");
                        sb.append("  coords=(").append(x).append(',').append(z).append(")\n");
                        sb.append("  highestY=").append(highestY).append(" minY=").append(minY).append(" maxY=").append(maxY).append("\n");
                        Block topBlock = null;
                        try {
                            topBlock = world.getBlockAt(x, highestY, z);
                        } catch (Throwable ignored) {}
                        if (topBlock != null) {
                            sb.append("  topBlock=").append(topBlock.getType()).append("\n");
                        }
                        debugFileLogger.log(sb.toString());
                    }
                } catch (Throwable ignored) {
                }
            }
            future.complete(candidate);
        } catch (Exception ex) {
            future.completeExceptionally(ex);
        } finally {
            scheduler.executeRegion(world, chunkX, chunkZ, () -> {
                if (!chunkWasLoaded && world.isChunkLoaded(chunkX, chunkZ)) {
                    world.unloadChunkRequest(chunkX, chunkZ);
                }
            });
        }
    }

    private Location resolveCandidateLocation(World world, int x, int z, RandomTeleportSettings settings) {
        int minY = getWorldMinHeight(world);
        int maxY = getWorldMaxHeight(world) - 1;
        if (world.getEnvironment() == World.Environment.NETHER) {
            maxY = Math.min(maxY, NETHER_ROOF_Y - 1);
        }

        if (world.getEnvironment() != World.Environment.NORMAL) {
            int highestY = Math.min(surfaceY(world, x, z), maxY);
            if (highestY <= minY) {
                return null;
            }
            return findPassableLocation(world, x, z, highestY, minY, settings);
        }

        int highestY = surfaceY(world, x, z);
        if (highestY <= minY) {
            return null;
        }
        return findPassableLocation(world, x, z, highestY, minY, settings);
    }

    private Location findPassableLocation(World world, int x, int z, int startY, int minY, RandomTeleportSettings settings) {
        int searchMinY = resolveSearchMinY(world, startY, minY, settings);
        for (int y = startY; y > searchMinY; y--) {
            Measurements.incBlockAccess();
            Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isSolid()) {
                continue;
            }
            Block above = block.getRelative(BlockFace.UP);
            Block twoAbove = above.getRelative(BlockFace.UP);
            if (BlockCompat.isPassable(above) && BlockCompat.isPassable(twoAbove)) {
                if (above.isLiquid()) {
                    Location surface = findSurfaceAboveLiquid(world, x, z, above.getY());
                    if (surface != null) {
                        return surface;
                    }
                    continue;
                }
                return new Location(world, x + 0.5, block.getY() + 1.0, z + 0.5);
            }
        }
        return null;
    }

    private int resolveSurfaceScanDepth(World world, RandomTeleportSettings settings) {
        if (settings == null || settings.getSafetySettings() == null) {
            return DEFAULT_SURFACE_SCAN_DEPTH;
        }
        SafetySettings safetySettings = settings.getSafetySettings();
        int configuredDepth = world != null && world.getEnvironment() == World.Environment.NETHER
                ? safetySettings.getMaxSurfaceScanDepthNether()
                : safetySettings.getMaxSurfaceScanDepth();
        return Math.max(1, Math.min(configuredDepth, MAX_SURFACE_SCAN_DEPTH));
    }

    private int resolveSearchMinY(World world, int startY, int minY, RandomTeleportSettings settings) {
        return Math.max(minY, startY - resolveSurfaceScanDepth(world, settings));
    }

    private Location findSurfaceAboveLiquid(World world, int x, int z, int startY) {
        int maxY = getWorldMaxHeight(world) - 1;
        int y = startY;
        while (y <= maxY) {
            Measurements.incBlockAccess();
            if (!world.getBlockAt(x, y, z).isLiquid()) break;
            y++;
        }
        if (y > maxY) {
            return null;
        }
        Measurements.incBlockAccess();
        Block surface = world.getBlockAt(x, y, z);
        Block above = surface.getRelative(BlockFace.UP);
        if (!BlockCompat.isPassable(surface) || !BlockCompat.isPassable(above)) {
            return null;
        }
        return new Location(world, x + 0.5, surface.getY(), z + 0.5);
    }


    private int surfaceY(World world, int x, int z) {
        if (platformWorldAccess != null) {
            return platformWorldAccess.getSurfaceY(world, x, z);
        }
        return world == null ? 0 : world.getHighestBlockYAt(x, z);
    }

    private Location snapshotValidate(World world, int x, int z, int startY, int minY) {
        if (platformWorldAccess == null) {
            return null;
        }
        return platformWorldAccess.trySnapshotValidate(world, x, z, startY, minY);
    }

    private int resolveMaximumRadius(World world, RandomTeleportSettings settings) {
        if (!settings.useWorldBorderRadius()) {
            return settings.getMaximumRadius();
        }
        java.util.Optional<Double> radius = com.skyblockexp.ezrtp.util.compat.WorldBorderCompat.getBorderRadius(world);
        if (radius.isPresent()) {
            int resolved = (int) Math.floor(radius.get());
            return Math.max(settings.getMinimumRadius(), resolved);
        }
        return settings.getMaximumRadius();
    }

    private Location getFallbackCachedLocation(World world, RandomTeleportSettings settings, boolean requireSafety) {
        if (biomeCache == null || !biomeCache.isEnabled() || world == null) {
            return null;
        }
        int attempts = 8;
        for (int i = 0; i < attempts; i++) {
            Location cachedLocation = biomeCache.getRandomCachedLocation(world);
            if (cachedLocation == null) {
                return null;
            }
            if (requireSafety && !validator.isSafe(cachedLocation, settings)) {
                continue;
            }
            if (validator.isProtectedByClaims(cachedLocation, settings)) {
                continue;
            }
            return cachedLocation;
        }
        return null;
    }

    public CompletableFuture<Location> generateSafeLocationForChunk(World world, int chunkX, int chunkZ, RandomTeleportSettings currentSettings) {
        if (world == null || currentSettings == null) return CompletableFuture.completedFuture(null);

        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        boolean wasChunkLoaded = world.isChunkLoaded(chunkX, chunkZ);

        CompletableFuture<Location> future = new CompletableFuture<>();

        if (!wasChunkLoaded) {
            if (chunkLoadQueue != null && chunkLoadQueue.isEnabled()) {
                statistics.recordChunkLoadQueueUse();
                chunkLoadQueue.requestChunkLoad(world, chunkX, chunkZ).thenAccept(chunk ->
                    scheduler.executeRegion(world, chunkX, chunkZ, () -> handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future))
                ).exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
            } else {
                scheduler.executeRegion(world, chunkX, chunkZ, () -> {
                    world.loadChunk(chunkX, chunkZ);
                    handleCandidateResolution(world, x, z, chunkX, chunkZ, wasChunkLoaded, currentSettings, future);
                });
            }
        } else {
            scheduler.executeRegion(world, chunkX, chunkZ, () -> handleCandidateResolution(world, x, z, chunkX, chunkZ, true, currentSettings, future));
        }

        return future;
    }

    private static int getWorldMinHeight(World world) {
        try {
            return (int) World.class.getMethod("getMinHeight").invoke(world);
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private static int getWorldMaxHeight(World world) {
        try {
            return (int) World.class.getMethod("getMaxHeight").invoke(world);
        } catch (ReflectiveOperationException ignored) {
            return world.getMaxHeight();
        }
    }


    private CompletableFuture<SearchResult> completeDueToLimit(World world,
                                                               RandomTeleportSettings currentSettings,
                                                               SearchContext context,
                                                               boolean cacheChecked) {
        statistics.recordTimeoutFailure();
        SearchLimitType limitType = context.getLimitType();
        boolean noValidBiome = limitType == SearchLimitType.BIOME_REJECTIONS;
        BiomeSearchSettings searchSettings = context.getSettings();
        int effectiveAttemptLimit = resolveAttemptLimit(currentSettings, context);
        long elapsedMillis = context.getElapsedMillis();
        boolean cacheFailoverAttempted = false;
        boolean cacheFailoverProducedLocation = false;

        BiomeSearchSettings.FailoverMode mode = searchSettings.getFailoverMode();
        if (mode == BiomeSearchSettings.FailoverMode.CACHE && biomeCache.isEnabled()) {
            cacheFailoverAttempted = true;
            cacheChecked = true;
            Location cachedLocation = getFallbackCachedLocation(world, currentSettings, false);
            if (cachedLocation != null) {
                cacheFailoverProducedLocation = true;
                logLimitDebug(world, currentSettings, context, limitType, elapsedMillis, effectiveAttemptLimit,
                        searchSettings, cacheFailoverAttempted, cacheFailoverProducedLocation);
                statistics.recordBiomeRejectionCount(context.getBiomeRejections());
                if (limitType == SearchLimitType.BIOME_REJECTIONS) {
                    statistics.recordBiomeFilterTimeout();
                }
                return CompletableFuture.completedFuture(new SearchResult(Optional.of(cachedLocation), false, true, true, true, false, limitType));
            }
        }

        logLimitDebug(world, currentSettings, context, limitType, elapsedMillis, effectiveAttemptLimit,
                searchSettings, cacheFailoverAttempted, cacheFailoverProducedLocation);
        statistics.recordBiomeRejectionCount(context.getBiomeRejections());
        if (limitType == SearchLimitType.BIOME_REJECTIONS) {
            statistics.recordBiomeFilterTimeout();
        }

        return CompletableFuture.completedFuture(new SearchResult(Optional.empty(), noValidBiome, false, cacheChecked, false, false, limitType));
    }

    private void logLimitDebug(World world,
                               RandomTeleportSettings currentSettings,
                               SearchContext context,
                               SearchLimitType limitType,
                               long elapsedMillis,
                               int effectiveAttemptLimit,
                               BiomeSearchSettings searchSettings,
                               boolean cacheFailoverAttempted,
                               boolean cacheFailoverProducedLocation) {
        if (currentSettings == null || !currentSettings.isDebugRejectionLoggingEnabled()) {
            return;
        }
        try {
            String worldName = world != null ? world.getName() : "unknown";
            StringBuilder sb = new StringBuilder();
            sb.append("EzRTP debug: search limit reached for world '").append(worldName).append("'\n");
            sb.append("  activeLimitType=").append(limitType).append('\n');
            sb.append("  elapsedWallClockMs=").append(elapsedMillis).append('\n');
            sb.append("  chunkLoadCount=").append(context.getChunkLoads()).append(" biomeRejectionCount=").append(context.getBiomeRejections()).append('\n');
            sb.append("  effectiveAttemptLimit=").append(effectiveAttemptLimit)
                    .append(" configuredMaxAttempts=").append(currentSettings.getMaxAttempts()).append('\n');
            sb.append("  configuredBiomeSearchLimits=");
            sb.append("{wallClock=").append(searchSettings.getMaxWallClockMillis())
                    .append(", wallClockRare=").append(searchSettings.getMaxWallClockMillisRare())
                    .append(", biomeRejections=").append(searchSettings.getMaxBiomeRejections())
                    .append(", biomeRejectionsRare=").append(searchSettings.getMaxBiomeRejectionsRare())
                    .append(", chunkLoads=").append(searchSettings.getMaxChunkLoads())
                    .append(", chunkLoadsRare=").append(searchSettings.getMaxChunkLoadsRare())
                    .append(", minBiomeAttempts=").append(searchSettings.getMinBiomeAttempts())
                    .append(", failoverMode=").append(searchSettings.getFailoverMode())
                    .append("}\n");
            sb.append("  cacheFailoverAttempted=").append(cacheFailoverAttempted)
                    .append(" cacheFailoverProducedLocation=").append(cacheFailoverProducedLocation);
            debugFileLogger.log(sb.toString());
        } catch (Exception ignored) {
        }
    }

    private void logSearchStart(World world, RandomTeleportSettings settings) {
        if (settings == null || !settings.isDebugRejectionLoggingEnabled()) {
            return;
        }
        try {
            String worldName = world != null ? world.getName() : "unknown";
            String strategyName = searchStrategy != null ? searchStrategy.getClass().getSimpleName() : "UniformSearchStrategy";
            String configuredPattern = settings.getSearchPattern() != null
                    ? settings.getSearchPattern().getConfigKey()
                    : "random";
            BiomeSearchSettings biomeSearchSettings = settings.getBiomeSearchSettings() != null
                    ? settings.getBiomeSearchSettings()
                    : BiomeSearchSettings.defaults();

            StringBuilder sb = new StringBuilder();
            sb.append("EzRTP debug: search start for world '").append(worldName).append("'\n");
            sb.append("  pattern=").append(configuredPattern).append(" strategy=").append(strategyName).append('\n');
            sb.append("  center=(").append(settings.getCenterX()).append(',').append(settings.getCenterZ()).append(")\n");
            sb.append("  radius=[").append(settings.getMinimumRadius()).append(',').append(resolveMaximumRadius(world, settings)).append("] useWorldBorder=").append(settings.useWorldBorderRadius()).append('\n');
            sb.append("  maxAttempts=").append(settings.getMaxAttempts())
                    .append(" minBiomeAttempts=").append(biomeSearchSettings.getMinBiomeAttempts()).append('\n');
            sb.append("  includeBiomes=").append(settings.getBiomeInclude())
                    .append(" excludeBiomes=").append(settings.getBiomeExclude()).append('\n');
            debugFileLogger.log(sb.toString());
        } catch (Exception ignored) {
        }
    }

    private void logCandidateRejection(World world,
                                       RandomTeleportSettings settings,
                                       int attempt,
                                       Location candidate,
                                       String reason) {
        if (settings == null || !settings.isDebugRejectionLoggingEnabled()) {
            return;
        }
        if (attempt > 0 && attempt % 10 != 0) {
            return;
        }
        try {
            String worldName = world != null ? world.getName() : "unknown";
            StringBuilder sb = new StringBuilder();
            sb.append("EzRTP debug: candidate rejected in world '").append(worldName).append("'\n");
            sb.append("  reason=").append(reason).append(" attempt=").append(attempt).append('\n');
            if (candidate != null) {
                sb.append("  candidate=(")
                        .append(candidate.getBlockX()).append(',')
                        .append(candidate.getBlockY()).append(',')
                        .append(candidate.getBlockZ()).append(") biome=")
                        .append(candidate.getBlock().getBiome()).append('\n');
            } else {
                sb.append("  candidate=<null>\n");
            }
            sb.append("  strategy=").append(searchStrategy != null ? searchStrategy.getClass().getSimpleName() : "UniformSearchStrategy");
            debugFileLogger.log(sb.toString());
        } catch (Exception ignored) {
        }
    }

    private void recordUnsafeCause(Location candidate, RandomTeleportSettings settings) {
        UnsafeLocationMonitor monitor = this.unsafeMonitor;
        if (monitor == null || !monitor.isEnabled()) {
            return;
        }
        if (candidate == null) {
            monitor.recordRejection(UnsafeLocationCause.NULL_CANDIDATE);
        } else {
            UnsafeLocationCause cause = validator.checkSafety(candidate, settings).orElse(UnsafeLocationCause.OTHER);
            monitor.recordRejection(cause);
        }
    }

    private Optional<Location> attemptSafetyRecovery(Location unsafeLocation,
                                                     RandomTeleportSettings settings) {
        if (unsafeLocation == null || settings == null) {
            return Optional.empty();
        }
        SafetySettings safety = settings.getSafetySettings();
        if (safety == null || !safety.isRescueEnabled()) {
            return Optional.empty();
        }

        World world = unsafeLocation.getWorld();
        if (world == null) {
            return Optional.empty();
        }

        int maxOffset = Math.max(1, safety.getMaxVerticalRescue());
        Location probe = unsafeLocation.clone();
        for (int offset = 0; offset <= maxOffset; offset++) {
            Location up = probe.clone().add(0, offset, 0);
            if (validator.isSafe(up, settings) && !validator.isProtectedByClaims(up, settings)) {
                return Optional.of(up);
            }
            if (offset == 0) {
                continue;
            }
            Location down = probe.clone().add(0, -offset, 0);
            if (validator.isSafe(down, settings) && !validator.isProtectedByClaims(down, settings)) {
                return Optional.of(down);
            }
        }
        return Optional.empty();
    }

    private static final class SearchContext {
        private final BiomeSearchSettings settings;
        private final long startTime;
        private int biomeRejections;
        private int chunkLoads;
        private SearchLimitType limitType = SearchLimitType.NONE;
        private final boolean rareSearch;

        private SearchContext(BiomeSearchSettings settings, boolean rareSearch) {
            this.settings = settings != null ? settings : BiomeSearchSettings.defaults();
            this.startTime = System.currentTimeMillis();
            this.rareSearch = rareSearch;
        }

        private boolean hasLimitBeenHit() {
            if (limitType != SearchLimitType.NONE) {
                return true;
            }
            int wallClockLimit = rareSearch ? settings.getMaxWallClockMillisRare() : settings.getMaxWallClockMillis();
            if (wallClockLimit > 0
                    && (System.currentTimeMillis() - startTime) >= wallClockLimit) {
                limitType = SearchLimitType.WALL_CLOCK;
                return true;
            }
            return false;
        }

        private boolean recordBiomeRejection() {
            if (limitType != SearchLimitType.NONE) {
                return false;
            }
            biomeRejections++;
            int limit = rareSearch ? settings.getMaxBiomeRejectionsRare() : settings.getMaxBiomeRejections();
            if (limit > 0 && biomeRejections > limit) {
                limitType = SearchLimitType.BIOME_REJECTIONS;
                return false;
            }
            return true;
        }

        private boolean tryIncrementChunkLoad() {
            if (limitType != SearchLimitType.NONE) {
                return false;
            }
            chunkLoads++;
            int limit = rareSearch ? settings.getMaxChunkLoadsRare() : settings.getMaxChunkLoads();
            if (limit > 0 && chunkLoads > limit) {
                limitType = SearchLimitType.CHUNK_LOADS;
                return false;
            }
            return true;
        }

        private SearchLimitType getLimitType() {
            return limitType;
        }

        private BiomeSearchSettings getSettings() {
            return settings;
        }

        private long getElapsedMillis() {
            return System.currentTimeMillis() - startTime;
        }

        private int getBiomeRejections() {
            return biomeRejections;
        }

        private int getChunkLoads() {
            return chunkLoads;
        }
    }

    boolean isRareSearch(RandomTeleportSettings currentSettings) {
        if (currentSettings == null || rareBiomeRegistry == null || !rareBiomeRegistry.isEnabled()) {
            return false;
        }
        if (currentSettings.getRareBiomeOptimizationSettings() == null
                || !currentSettings.getRareBiomeOptimizationSettings().isEnabled()) {
            return false;
        }
        if (currentSettings.getBiomeInclude().isEmpty()) {
            return false;
        }
        return currentSettings.getBiomeInclude().stream()
                .allMatch(rareBiomeRegistry::isRareBiome);
    }
}
