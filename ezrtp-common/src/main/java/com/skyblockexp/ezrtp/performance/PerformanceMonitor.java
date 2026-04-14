package com.skyblockexp.ezrtp.performance;

import com.skyblockexp.ezrtp.config.performance.MetricsSettings;
import com.skyblockexp.ezrtp.config.performance.PerformanceSettings;
import com.skyblockexp.ezrtp.config.performance.WarningsSettings;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformTask;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Coordinates slow-RTP warnings and periodic JSON metrics export.
 * File I/O is performed on a dedicated daemon thread to avoid blocking
 * server ticks.
 */
public final class PerformanceMonitor {

    /** 20 ticks/second × 60 seconds = 1 minute in ticks. */
    private static final long TICKS_PER_MINUTE = 1_200L;

    private final PerformanceSettings settings;
    private final RtpStatistics statistics;
    private final BiomeLocationCache biomeCache;
    private final Logger logger;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ezrtp-perf-io");
        t.setDaemon(true);
        return t;
    });

    private volatile PlatformTask periodicTask;

    public PerformanceMonitor(
            PerformanceSettings settings,
            RtpStatistics statistics,
            BiomeLocationCache biomeCache,
            Logger logger) {
        this.settings = settings;
        this.statistics = statistics;
        this.biomeCache = biomeCache;
        this.logger = logger;
    }

    /**
     * Returns the settings this monitor was created with.
     */
    public PerformanceSettings getSettings() {
        return settings;
    }

    /**
     * Returns {@code true} when performance monitoring is enabled in config.
     */
    public boolean isEnabled() {
        return settings.getMonitoring().isEnabled();
    }

    /**
     * Called after each RTP completes. Logs a slow-RTP warning when
     * {@code durationMs} exceeds the configured threshold.
     *
     * @param durationMs wall-clock time the operation took
     * @param worldName  name of the destination world (for context)
     */
    public void afterOperation(long durationMs, String worldName) {
        if (!isEnabled()) {
            return;
        }
        WarningsSettings warnings = settings.getWarnings();
        if (durationMs >= warnings.getSlowRtpThresholdMs()) {
            String message = String.format(
                    "Slow RTP: %dms in world '%s' (threshold %dms)",
                    durationMs, worldName, warnings.getSlowRtpThresholdMs());
            if (warnings.isLogToConsole()) {
                logger.warning(message);
            }
            if (warnings.isLogToFile()) {
                String filePath = warnings.getFilePath();
                String line = Instant.now() + " " + message + "\n";
                ioExecutor.submit(() -> appendToFile(filePath, line));
            }
        }
    }

    /**
     * Cancels any existing periodic export task and, if both monitoring
     * and metrics export are enabled, schedules a new one.
     *
     * @param scheduler platform scheduler used to register the repeating task
     */
    public void schedulePeriodicExport(PlatformScheduler scheduler) {
        cancelPeriodicTask();
        MetricsSettings metrics = settings.getMetrics();
        if (!isEnabled() || !metrics.isEnabled()) {
            return;
        }
        long periodTicks = TICKS_PER_MINUTE * metrics.getExportIntervalMinutes();
        periodicTask = scheduler.scheduleRepeating(this::exportNow, periodTicks, periodTicks);
    }

    /**
     * Captures a snapshot of current metrics and writes it to the configured
     * JSON export file. Can be called from any thread; file I/O is dispatched
     * to the I/O executor.
     */
    public void exportNow() {
        if (!isEnabled()) {
            return;
        }
        String json = snapshot().toJson();
        String filePath = settings.getMetrics().getExportPath();
        ioExecutor.submit(() -> writeFile(filePath, json));
    }

    /**
     * Returns an immutable point-in-time snapshot of all performance metrics.
     */
    public PerformanceSnapshot snapshot() {
        long slowOpThreshold = settings.getWarnings().getSlowRtpThresholdMs();
        BiomeLocationCache.CacheStats cacheStats = biomeCache.getStats();
        return new PerformanceSnapshot(
                Instant.now().toString(),
                statistics.getTotalAttempts(),
                statistics.getTotalSuccesses(),
                statistics.getTotalFailures(),
                statistics.getSuccessRate(),
                statistics.getTimingPercentile(50),
                statistics.getTimingPercentile(75),
                statistics.getTimingPercentile(90),
                statistics.getTimingPercentile(95),
                statistics.getTimingPercentile(99),
                statistics.getMinTimeMs(),
                statistics.getMaxTimeMs(),
                statistics.getSlowOperationCount(slowOpThreshold),
                statistics.getCacheHitRate(),
                statistics.getTotalCacheHits(),
                statistics.getTotalCacheMisses(),
                cacheStats.evictions(),
                statistics.getTotalChunkLoads(),
                statistics.getAsyncChunkLoads(),
                statistics.getSyncChunkLoads(),
                statistics.getTotalBiomeRejections(),
                statistics.getMaxBiomeRejectionsPerRtp(),
                statistics.getBiomeFilterTimeouts());
    }

    /**
     * Cancels the periodic export task and shuts down the I/O executor,
     * waiting up to 2 seconds for any in-progress writes to finish.
     */
    public void shutdown() {
        cancelPeriodicTask();
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void cancelPeriodicTask() {
        PlatformTask task = periodicTask;
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
                // best-effort cancel
            }
            periodicTask = null;
        }
    }

    private void writeFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    path,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.warning(
                    "Failed to write performance metrics to '" + filePath + "': " + e.getMessage());
        }
    }

    private void appendToFile(String filePath, String line) {
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    path,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.warning(
                    "Failed to write slow-RTP warning to '" + filePath + "': " + e.getMessage());
        }
    }
}
