package com.skyblockexp.ezrtp.log;

import java.util.logging.Logger;

/**
 * Centralised log-message catalogue for EzRTP.
 *
 * <p>Every user-visible console message is defined here as a named method. Callers obtain an
 * instance via {@link #EzRtpLogger(Logger)} and invoke semantic methods instead of building
 * inline message strings. This keeps all wording and log levels in one place.
 */
public final class EzRtpLogger {

  private final Logger logger;

  public EzRtpLogger(Logger logger) {
    this.logger = logger;
  }

  // ---------------------------------------------------------------------------
  // Bootstrap / lifecycle
  // ---------------------------------------------------------------------------

  public void logReady() {
    logger.info("Ready.");
  }

  public void logDisabled() {
    logger.info("EzRTP plugin disabled.");
  }

  public void logRuntimeModule(String modules) {
    logger.info("Runtime module: " + modules);
  }

  public void warnNoRuntimeModule() {
    logger.warning("No runtime module installed; using built-in Bukkit adapters.");
  }

  public void severeUnsupportedModuleCombination(String unsupported) {
    logger.severe("Unsupported EzRTP runtime module combination detected: " + unsupported + ".");
    logger.severe(
        "Likely symptoms include duplicate chat messages and duplicate/random double teleports.");
    logger.severe(
        "Remove unsupported EzRTP runtime module jar(s) and keep only 'EzRTP.jar' and optionally"
            + " one supported module plugin such as 'EzRTPPaperModule'.");
  }

  public void logPlatform(String runtime, String scheduler, String strategy) {
    logger.info("Platform: " + runtime + " | " + scheduler + " | " + strategy);
  }

  public void logAdapters(String gui, String messages, String sender) {
    logger.info("Adapters: GUI=" + gui + " | Messages=" + messages + " | Sender=" + sender);
  }

  // ---------------------------------------------------------------------------
  // Chunky integration
  // ---------------------------------------------------------------------------

  public void logChunkyNotFound() {
    logger.info("Chunky plugin not found - Chunky integration disabled.");
  }

  public void warnChunkyInitFailed(String message) {
    logger.warning("Failed to initialize Chunky integration: " + message);
  }

  public void logChunkyDisabledByConfig() {
    logger.info("Chunky integration disabled in configuration.");
  }

  public void logChunkyMemorySafety(long freeMb, long thresholdMb) {
    logger.info(
        String.format("Chunky: memory safety enabled (%dMB free, threshold %dMB)", freeMb, thresholdMb));
  }

  public void logChunkyGenerationCompleted() {
    logger.info("Chunky generation completed");
  }

  public void warnChunkyListenerFailed(String message) {
    logger.warning("Failed to register Chunky listeners: " + message);
  }

  public void warnChunkyCleanupScheduleFailed(String message) {
    logger.warning("Failed to schedule ChunkyWarmupCoordinator cleanup: " + message);
  }

  // ---------------------------------------------------------------------------
  // Configuration service
  // ---------------------------------------------------------------------------

  public void warnCostsNoVault() {
    logger.warning(
        "Teleport costs are configured but Vault is unavailable."
            + " Teleports will not deduct currency.");
  }

  public void warnSaveLanguageFileFailed(String message) {
    logger.warning("Could not save default language file: " + message);
  }

  public void logLegacyMessagesMigrated(String filename) {
    logger.info("Detected legacy messages.yml - migrated to messages/" + filename);
  }

  public void warnLegacyMessagesMigrationFailed(String message) {
    logger.warning("Failed to migrate legacy messages.yml: " + message);
  }

  public void warnSaveDefaultResourceFailed(String resource, String message) {
    logger.warning("Unable to save default resource '" + resource + "': " + message);
  }

  // ---------------------------------------------------------------------------
  // Economy
  // ---------------------------------------------------------------------------

  public void logVaultNotFound() {
    logger.info("Vault not found. Random teleport costs will be disabled.");
  }

  public void warnVaultNoProvider() {
    logger.warning(
        "Vault is installed but no economy provider is registered."
            + " Random teleport costs will be disabled.");
  }

  public void logEconomy(String providerName) {
    logger.info("Economy: " + providerName + " (Vault)");
  }

  public void warnWithdrawFailed(double amount, String playerName, String response) {
    logger.warning(
        String.format(
            java.util.Locale.US, "Failed to withdraw %.2f from %s: %s", amount, playerName, response));
  }

  // ---------------------------------------------------------------------------
  // Message provider / locale
  // ---------------------------------------------------------------------------

  public void warnMessagesDirectoryMissing() {
    logger.warning("Messages directory does not exist, using defaults");
  }

  public void warnLanguageFileMissing(String language) {
    logger.warning("Language file for '" + language + "' not found, using defaults");
  }

  public void warnMissingMessageKey(String key, String language) {
    logger.warning("Missing message key: " + key + " in language file: " + language);
  }

  public void logMigratedLegacyMessageKey() {
    logger.info(
        "Migrated legacy message key 'forcertp-target-notify' to 'forcertp-target-notification'");
  }

  public void logLocale(String language, int count) {
    logger.info("Locale: " + language + " (" + count + " messages)");
  }

  public void severeLangFileFailed(String language, String message) {
    logger.severe("Failed to load language file for '" + language + "': " + message);
  }

  // ---------------------------------------------------------------------------
  // Usage reset
  // ---------------------------------------------------------------------------

  public void logDailyUsageReset() {
    logger.info("Daily RTP usage counts reset.");
  }

  public void logWeeklyUsageReset() {
    logger.info("Weekly RTP usage counts reset.");
  }

  // ---------------------------------------------------------------------------
  // Metrics (bStats)
  // ---------------------------------------------------------------------------

  public void logMetricsEnabled(String url) {
    logger.info("Metrics: bStats (" + url + ")");
  }

  public void logMetricsDisabled() {
    logger.info("bStats metrics disabled in config.");
  }

  public void warnMetricsFailed(String message) {
    logger.warning("Failed to start bStats metrics: " + message);
  }

  // ---------------------------------------------------------------------------
  // Platform adapter registrar
  // ---------------------------------------------------------------------------

  public void fineRegisteredOptionalProvider(String className) {
    logger.fine("Registered optional provider: " + className);
  }

  public void warnOptionalProviderFailed(String className, String message) {
    logger.warning("Failed to register optional provider " + className + ": " + message);
  }

  // ---------------------------------------------------------------------------
  // Platform registries
  // ---------------------------------------------------------------------------

  public void fineRegisteredChunkLoadStrategy(String className) {
    logger.fine("Registered ChunkLoadStrategy provider: " + className);
  }

  public void fineRegisteredGuiBridge(String className) {
    logger.fine("Registered PlatformGuiBridge provider: " + className);
  }

  public void fineRegisteredMessageService(String className) {
    logger.fine("Registered PlatformMessageService provider: " + className);
  }

  public void fineRegisteredPlatformRuntime(String className) {
    logger.fine("Registered PlatformRuntime provider: " + className);
  }

  public void fineRegisteredSenderBridge(String className) {
    logger.fine("Registered PlatformSenderBridge provider: " + className);
  }

  // ---------------------------------------------------------------------------
  // Chunk loading
  // ---------------------------------------------------------------------------

  public void logChunkLoadingPaperAsync() {
    logger.info("Chunk loading: Paper async API");
  }

  public void warnPaperAsyncAlwaysForcedOnNonPaper() {
    logger.warning(
        "chunk-loading.use-paper-async-api is set to 'always' but Paper API was not detected."
            + " The async fast-path will still run but chunk loading will not be genuinely"
            + " non-blocking.");
  }

  public void warnChunkLoadFailed(String message) {
    logger.warning("Failed to load chunk: " + message);
  }

  public void warnChunkLoadQueueFallback(String reason) {
    logger.warning(
        "Chunk load queue fallback engaged; using immediate synchronous loading. Reason: " + reason);
  }

  public void warnChunkLoadSyncFailed(String message) {
    logger.warning("Failed to synchronously load chunk after queue fallback: " + message);
  }

  // ---------------------------------------------------------------------------
  // Teleport / location search
  // ---------------------------------------------------------------------------

  public void warnCriticallyLowMemory(long freeMb, long totalMb) {
    logger.warning(
        String.format(
            "Teleport cancelled due to critically low memory: %dMB free / %dMB total",
            freeMb, totalMb));
  }

  public void warnLowMemoryCacheOnly(long freeMb) {
    logger.warning(
        String.format("Low memory detected (%dMB free), using cache-only mode", freeMb));
  }

  public void logReducedAttemptsMemory(int from, int to, long freeMb) {
    logger.info(
        String.format(
            "Reducing max attempts from %d to %d due to low memory (%dMB free)", from, to, freeMb));
  }

  public void fineReducedCacheGenMemory(int from, int to, long freeMb) {
    logger.fine(
        String.format(
            "Reducing cache generation attempts from %d to %d due to low memory (%dMB free)",
            from, to, freeMb));
  }

  public void fineSkipChunkyLowMemory() {
    logger.fine("Skipping Chunky pregeneration due to low memory");
  }

  public void logRtpRejected(String location, String reason) {
    logger.info("RTP rejected " + location + ": " + reason);
  }

  public void warnDebugLocationSearchFailed(
      String playerName, String worldName, long durationMs, String exClass, String exMsg) {
    logger.warning(
        "EzRTP debug: location search failed for player '"
            + playerName
            + "' in world '"
            + worldName
            + "' after "
            + durationMs
            + "ms: "
            + exClass
            + " - "
            + exMsg);
  }

  public void warnDebugFailureBreadcrumb(
      String playerName,
      String worldName,
      String reasonClass,
      String limitType,
      boolean noValidBiome,
      boolean fallbackUsed,
      boolean noCacheAvailable,
      long durationMs,
      boolean cacheHit) {
    logger.warning(
        "EzRTP debug: failure breadcrumb"
            + " player="
            + playerName
            + " world="
            + worldName
            + " reasonClass="
            + reasonClass
            + " limitType="
            + limitType
            + " noValidBiome="
            + noValidBiome
            + " fallbackUsed="
            + fallbackUsed
            + " noCacheAvailable="
            + noCacheAvailable
            + " durationMs="
            + durationMs
            + " cacheHit="
            + cacheHit);
  }

  // ---------------------------------------------------------------------------
  // Biome pre-cache
  // ---------------------------------------------------------------------------

  public void warnBiomePreCacheDisableMemory(long freeMb, long totalMb) {
    logger.warning(
        String.format(
            "Disabling biome pre-cache due to low memory: %dMB free / %dMB total",
            freeMb, totalMb));
  }

  public void logBiomePreCacheAutoEnabled(long freeMb, long totalMb) {
    logger.info(
        String.format(
            "Biome filters detected; auto-enabling pre-cache as configured"
                + " (Memory: %dMB free / %dMB total)",
            freeMb, totalMb));
  }

  public void warnBiomePreCacheMemoryLow(long freeMb, long totalMb) {
    logger.warning(
        String.format(
            "Biome filters detected but pre-cache not enabled due to low memory:"
                + " %dMB free / %dMB total",
            freeMb, totalMb));
  }

  public void warnBiomeNoCacheNoFilter() {
    logger.warning("Biome filtering is configured but pre-caching is disabled.");
  }

  public void warnBiomeNoCacheRecommendation() {
    logger.warning(
        "RECOMMENDATION: Enable 'biomes.pre-cache.enabled: true' in config.yml"
            + " to improve RTP success rates.");
  }

  public void logSkipChunkyBiomeCacheLowMemory() {
    logger.info("Skipping Chunky pregeneration for biome pre-caching due to low memory");
  }

  public void logStartedChunkyPregen(String worldName) {
    logger.info(
        "Started Chunky pregeneration for world '"
            + worldName
            + "' to support biome pre-caching.");
  }

  public void warnChunkyApiFailed(String message) {
    logger.warning("Failed to interact with Chunky API: " + message);
  }

  public void logBiomeCacheWarmupStarted(String worldName, long freeMb, long totalMb) {
    logger.info(
        String.format(
            "Starting biome cache warmup for world '%s' (Memory: %dMB free / %dMB total)",
            worldName, freeMb, totalMb));
  }

  public void logBiomeFiltersRareOptimizations() {
    logger.info("Biome filters detected; auto-enabling rare biome optimizations as configured.");
  }

  // ---------------------------------------------------------------------------
  // Biome cache warmup (BiomeLocationCache)
  // ---------------------------------------------------------------------------

  public void warnBiomeCacheSkipWarmupLowMemory(String worldName, long freeMb, long totalMb) {
    logger.warning(
        String.format(
            "[BiomeCache] Skipping warmup for world '%s' due to low memory:"
                + " %dMB free / %dMB total",
            worldName, freeMb, totalMb));
  }

  public void logBiomeCacheWarmedUp(int count, String worldName, long freeMb, long totalMb) {
    logger.info(
        String.format(
            "[BiomeCache] Warmed up %d locations for world '%s'"
                + " (Memory: %dMB free / %dMB total)",
            count, worldName, freeMb, totalMb));
  }

  public void fineBiomeCacheWarmupBatch(
      String worldName, int completed, int total, long freeMb, long totalMb) {
    logger.fine(
        String.format(
            "[BiomeCache] Starting warmup batch for world '%s'"
                + " (completed: %d/%d, Memory: %dMB free / %dMB total)",
            worldName, completed, total, freeMb, totalMb));
  }

  public void logBiomeCacheReduceBatchSize(int size, long freeKb) {
    logger.info(
        String.format(
            "[BiomeCache] Reducing warmup batch size to %d due to memory constraints"
                + " (%dKB free)",
            size, freeKb));
  }

  public void fineBiomeCacheChunkBatch(int size, String worldName, long freeMb) {
    logger.fine(
        String.format(
            "[BiomeCache] Processing %d chunk-based warmup locations for world '%s'"
                + " (Memory: %dMB free)",
            size, worldName, freeMb));
  }

  public void logBiomeCacheWarmedUpFinal(int count, String worldName) {
    logger.info(
        String.format("[BiomeCache] Warmed up %d locations for world '%s'", count, worldName));
  }

  public void warnBiomeCacheAbortWarmupLowMemory(String worldName, long freeMb) {
    logger.warning(
        String.format(
            "[BiomeCache] Aborting warmup attempt for world '%s' due to critically low memory:"
                + " %dMB free",
            worldName, freeMb));
  }

  public void fineBiomeCacheWarmupAttempt(String worldName, long freeMb) {
    logger.fine(
        String.format(
            "[BiomeCache] Attempting warmup location for world '%s' (Memory: %dMB free)",
            worldName, freeMb));
  }

  public void fineBiomeCacheCachedLocation(String location, String worldName) {
    logger.fine(
        String.format(
            "[BiomeCache] Successfully cached location at %s in world '%s'", location, worldName));
  }

  public void warnBiomeCacheWarmupInterrupted() {
    logger.warning("[BiomeCache] Warmup interrupted - plugin may be shutting down");
  }

  public void fineBiomeCacheFailedGenerate(String worldName) {
    logger.fine(
        String.format(
            "[BiomeCache] Failed to generate warmup location for world '%s'", worldName));
  }

  public void fineBiomeCachedLocation(String biome, String worldName, int total, long freeMb) {
    logger.fine(
        String.format(
            "[BiomeCache] Cached location for %s in world '%s' (total: %d, Memory: %dMB free)",
            biome, worldName, total, freeMb));
  }

  // ---------------------------------------------------------------------------
  // Hotspot / MySQL storage
  // ---------------------------------------------------------------------------

  public void warnHotspotTableCreateFailed(String message) {
    logger.warning("Failed to create hotspot table: " + message);
  }

  public void warnHotspotSaveFailed(String message) {
    logger.warning("Failed to save hotspot: " + message);
  }

  public void warnHotspotLoadFailed(String message) {
    logger.warning("Failed to load hotspots: " + message);
  }

  public void warnHotspotCloseFailed(String message) {
    logger.warning("Failed to close hotspot storage: " + message);
  }

  public void warnMySqlHotspotStorageInitFailed(String message) {
    logger.warning(
        "Failed to initialise MySQL hotspot storage, falling back to in-memory only: " + message);
  }

  // ---------------------------------------------------------------------------
  // Performance monitor
  // ---------------------------------------------------------------------------

  public void warnSlowRtp(long durationMs, String worldName, long thresholdMs) {
    logger.warning(
        String.format(
            "Slow RTP: %dms in world '%s' (threshold %dms)", durationMs, worldName, thresholdMs));
  }

  public void warnPerformanceMetricsWriteFailed(String filePath, String message) {
    logger.warning("Failed to write performance metrics to '" + filePath + "': " + message);
  }

  public void warnSlowRtpWriteFailed(String filePath, String message) {
    logger.warning("Failed to write slow-RTP warning to '" + filePath + "': " + message);
  }

  // ---------------------------------------------------------------------------
  // Unsafe location monitor
  // ---------------------------------------------------------------------------

  public void warnHighUnsafeRate(double rate, int unsafe, int total, int minutes) {
    logger.warning(
        String.format(
            "High unsafe location rejection rate: %.1f%% (%d/%d RTPs) in the last %d minutes",
            rate, unsafe, total, minutes));
  }

  public void warnUnsafeMetricsWriteFailed(String filePath, String message) {
    logger.warning("Failed to write unsafe-location metrics to '" + filePath + "': " + message);
  }

  public void warnUnsafeLocationWriteFailed(String filePath, String message) {
    logger.warning("Failed to write unsafe-location warning to '" + filePath + "': " + message);
  }

  // ---------------------------------------------------------------------------
  // Heatmap
  // ---------------------------------------------------------------------------

  public void warnHeatmapDisabled(String playerName) {
    logger.warning(
        "EzRTP: Heatmap command invoked by "
            + playerName
            + " but heatmap.enabled is false in rtp.yml. Enable it to use heatmap features.");
  }

  public void warnHeatmapCreateFailed(String message) {
    logger.warning("Failed to create heatmap map: " + message);
  }

  public void logHeatmapSaved(String path) {
    logger.info("Heatmap saved to: " + path);
  }

  public void warnHeatmapPngSaveFailed(String message) {
    logger.warning("Failed to save heatmap PNG: " + message);
  }

  // ---------------------------------------------------------------------------
  // Update checker
  // ---------------------------------------------------------------------------

  public void warnUpdateCheckEmptyResponse() {
    logger.warning("SpigotMC update check returned an empty response.");
  }

  public void logUpToDate() {
    logger.info("EzRTP is up to date.");
  }

  public void warnUpdateCheckFailed(String message) {
    logger.warning("Failed to check for EzRTP updates: " + message);
  }

  // ---------------------------------------------------------------------------
  // Debug file logger
  // ---------------------------------------------------------------------------

  public void fineDebugLogWriteFailed(String message) {
    logger.fine("Failed to write debug log file: " + message);
  }

  // ---------------------------------------------------------------------------
  // Protection providers
  // ---------------------------------------------------------------------------

  public void warnGriefPreventionNoGetClaimAt() {
    logger.warning(
        "GriefPrevention getClaimAt method not found;"
            + " RTP protection will be disabled for this provider.");
  }

  public void warnGriefPreventionDataStoreNull() {
    logger.warning(
        "GriefPrevention dataStore field is null;"
            + " RTP protection will be disabled for this provider.");
  }

  public void warnProtectionNoProviders(String missing) {
    logger.warning(
        "EzRTP: avoid-claims is enabled but none of the configured protection providers "
            + missing
            + " are available. Protected regions will NOT be avoided."
            + " Install WorldGuard or GriefPrevention, or set avoid-claims: false in rtp.yml.");
  }

  public void warnProtectionProviderUnavailable(String providerId) {
    logger.warning(
        "EzRTP protection provider '"
            + providerId
            + "' is configured but not available;"
            + " protected regions will not be avoided for this provider.");
  }

  // ---------------------------------------------------------------------------
  // Network
  // ---------------------------------------------------------------------------

  public void warnNetworkPollingFailed(String message) {
    logger.warning("Unable to start network status polling: " + message);
  }

  public void warnNetworkTransferFailed(String server, String message) {
    logger.warning(
        String.format("Failed to prepare network transfer to '%s': %s", server, message));
  }

  // ---------------------------------------------------------------------------
  // Configuration validation warnings
  // ---------------------------------------------------------------------------

  public void warnNamedCenterMissingWorld(String name) {
    logger.warning("Named center '" + name + "' is missing a world and was skipped.");
  }

  public void warnInvalidBossBarColor(String name) {
    logger.warning("Invalid bossbar color: " + name);
  }

  public void warnInvalidBossBarStyle(String name) {
    logger.warning("Invalid bossbar style: " + name);
  }

  public void warnInvalidParticle(String name) {
    logger.warning("Invalid particle type: " + name);
  }

  public void warnInvalidGuiSlot(String worldKey, int slot) {
    logger.warning(
        String.format(
            "GUI world option '%s' uses an invalid slot '%d'. Skipping entry.", worldKey, slot));
  }

  public void warnDuplicateGuiSlot(int slot, String worldKey) {
    logger.warning(
        String.format(
            "Duplicate GUI slot '%d' detected for world option '%s'. Skipping entry.",
            slot, worldKey));
  }

  public void warnNetworkServerMissingIdentifier(String key) {
    logger.warning(
        String.format("Network server '%s' is missing a valid 'bungee-server' identifier.", key));
  }

  public void warnNetworkServerInvalidPort(String key, int port) {
    logger.warning(
        String.format("Network server '%s' specifies an invalid port '%d'.", key, port));
  }

  public void warnNetworkServerInvalidIcon(String key) {
    logger.warning(
        String.format("Network server '%s' has an invalid icon configuration.", key));
  }

  public void warnGuiNetworkServerInvalidSlot(String serverId, int slot) {
    logger.warning(
        String.format(
            "Network server '%s' uses an invalid slot '%d'. Skipping entry.", serverId, slot));
  }

  public void warnGuiNetworkServerSlotOccupied(String serverId, int slot) {
    logger.warning(
        String.format(
            "Network server '%s' cannot use GUI slot '%d' because it is already occupied."
                + " Skipping entry.",
            serverId, slot));
  }

  // ---------------------------------------------------------------------------
  // PlaceholderAPI
  // ---------------------------------------------------------------------------

  public void logPapiFound() {
    logger.info("PlaceholderAPI found and enabled. Placeholder support is available.");
  }

  public void logPapiNotFound() {
    logger.info("PlaceholderAPI not found. Placeholder resolution will use fallback behavior.");
  }

  public void logPapiNotAvailable() {
    logger.info(
        "PlaceholderAPI not available. Placeholder resolution will use fallback behavior.");
  }

  public void warnPlaceholderResolveFailed(String message) {
    logger.warning("Failed to resolve placeholders: " + message);
  }

  public void warnComponentPlaceholderFailed(String message) {
    logger.warning("Failed to resolve component placeholders: " + message);
  }

  // ---------------------------------------------------------------------------
  // LoreUtil debug
  // ---------------------------------------------------------------------------

  public void logLoreUtilSkipped() {
    logger.info("LoreUtil.setLore: rawLore is empty, skipping");
  }

  public void logLoreUtilRawLore(String rawLore) {
    logger.info("LoreUtil.setLore: rawLore = " + rawLore);
  }

  public void logLoreUtilHasMethod(boolean hasMethod) {
    logger.info("LoreUtil.setLore: hasMethod = " + hasMethod);
  }

  public void logLoreUtilPaperApi() {
    logger.info("LoreUtil.setLore: Using Paper API");
  }

  public void logLoreUtilSetPaperLore(String lore) {
    logger.info("LoreUtil.setLore: Set Paper lore: " + lore);
  }

  public void logLoreUtilSpigotApi() {
    logger.info("LoreUtil.setLore: Using Spigot API");
  }

  public void logLoreUtilResolvedLine(String line) {
    logger.info("LoreUtil.setLore: resolvedLine = " + line);
  }

  public void logLoreUtilLegacy(String legacy) {
    logger.info("LoreUtil.setLore: legacy = " + legacy);
  }

  public void logLoreUtilFinalLore(String lore) {
    logger.info("LoreUtil.setLore: final lore = " + lore);
  }

  public void logLoreUtilMetaHasLore(boolean hasLore) {
    logger.info("LoreUtil.setLore: meta.hasLore() = " + hasLore);
  }

  public void logLoreUtilGetLore(String lore) {
    logger.info("LoreUtil.setLore: meta.getLore() = " + lore);
  }

  public void warnInvalidMiniMessage(String context, String miniMessageString, String message) {
    logger.warning(
        "Invalid MiniMessage in " + context + ": '" + miniMessageString + "' - " + message);
  }

  // ---------------------------------------------------------------------------
  // Paper module
  // ---------------------------------------------------------------------------

  public void logPaperAdaptersRegistered(int count) {
    logger.info("Registered " + count + " Paper platform adapters for EzRTP.");
  }

  public void warnNoPaperRegistriesAccessible() {
    logger.warning(
        "No EzRTP registries were accessible from EzRTPPaperModule."
            + " This is safe, but module providers were not registered.");
  }

  public void logPaperModuleDisabled() {
    logger.info("EzRTP Paper platform module disabled.");
  }

  public void warnPaperRegistryNoRegisterProvider(String className) {
    logger.warning("Registry does not expose registerProvider: " + className);
  }

  public void fineRegisteredPaperProvider(String providerClass, String registryClass) {
    logger.fine("Registered Paper provider " + providerClass + " via " + registryClass);
  }

  public void fineSkipPaperProvider(String providerClass, String exceptionClass, String message) {
    logger.fine(
        "Skipping Paper provider registration for "
            + providerClass
            + ": "
            + exceptionClass
            + " - "
            + message);
  }

  public void finePlaceholderApiResolutionFailed(String message) {
    logger.fine("PlaceholderAPI resolution failed: " + message);
  }
}
