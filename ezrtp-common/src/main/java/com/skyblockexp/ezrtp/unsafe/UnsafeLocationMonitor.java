package com.skyblockexp.ezrtp.unsafe;

import com.skyblockexp.ezrtp.config.safety.LoggingSettings;
import com.skyblockexp.ezrtp.config.safety.MetricsSettings;
import com.skyblockexp.ezrtp.config.safety.UnsafeLocationSettings;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformTask;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;

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
 * Coordinates per-cause unsafe location tracking, console/file rate warnings,
 * and periodic JSON metrics export.
 *
 * <p>All file I/O is performed on a dedicated daemon thread to avoid blocking
 * server ticks.
 *
 * <p>The monitor is modelled on the existing {@link com.skyblockexp.ezrtp.performance.PerformanceMonitor}
 * to keep lifecycle and wiring patterns consistent.
 */
public final class UnsafeLocationMonitor {

    /** 20 ticks/second × 60 seconds = 1 minute in ticks. */
    private static final long TICKS_PER_MINUTE = 1_200L;

    private final UnsafeLocationSettings settings;
    private final UnsafeLocationStatistics unsafeStats;
    private final RtpStatistics rtpStats;
    private final Logger logger;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ezrtp-unsafe-io");
        t.setDaemon(true);
        return t;
    });

    private volatile PlatformTask periodicTask;

    public UnsafeLocationMonitor(
            UnsafeLocationSettings settings,
            UnsafeLocationStatistics unsafeStats,
            RtpStatistics rtpStats,
            Logger logger) {
        this.settings = settings;
        this.unsafeStats = unsafeStats;
        this.rtpStats = rtpStats;
        this.logger = logger;
    }

    /** Returns the current settings for this monitor instance. */
    public UnsafeLocationSettings getSettings() {
        return settings;
    }

    /** Returns {@code true} when the master monitoring switch is on. */
    public boolean isEnabled() {
        return settings.getMonitoring().isEnabled();
    }

    /**
     * Records an unsafe location rejection.
     *
     * <p>No-op when monitoring is disabled or cause tracking is disabled.
     *
     * @param cause the reason the candidate was rejected
     */
    public void recordRejection(UnsafeLocationCause cause) {
        if (!isEnabled() || !settings.getCauses().isTrack()) {
            return;
        }
        unsafeStats.recordRejection(cause);
    }

    /**
     * Returns the underlying {@link UnsafeLocationStatistics} for external queries
     * (e.g. bStats chart supplier).
     */
    public UnsafeLocationStatistics getStatistics() {
        return unsafeStats;
    }

    /**
     * Schedules a repeating export task using the provided scheduler.
     * Cancels any previously scheduled task first.
     */
    public void schedulePeriodicExport(PlatformScheduler scheduler) {
        cancelPeriodicTask();
        MetricsSettings metricsSettings = settings.getMetrics();
        if (!isEnabled() || !metricsSettings.isEnabled()) {
            return;
        }
        long periodTicks = TICKS_PER_MINUTE * metricsSettings.getExportIntervalMinutes();
        periodicTask = scheduler.scheduleRepeating(this::exportNow, periodTicks, periodTicks);
    }

    /**
     * Snapshots and resets the current window, then writes the JSON export and (if the
     * unsafe rate crosses the configured threshold) emits console/file warnings.
     */
    public void exportNow() {
        if (!isEnabled()) {
            return;
        }

        long currentAttempts = rtpStats.getTotalAttempts();
        boolean metricsEnabled = settings.getMetrics().isEnabled();
        UnsafeLocationStatistics.WindowSnapshot windowSnapshot =
                unsafeStats.snapshotWindow(currentAttempts, /* reset= */ true);

        UnsafeLocationSnapshot snapshot = new UnsafeLocationSnapshot(
                Instant.now().toString(),
                settings.getMetrics().getExportIntervalMinutes(),
                windowSnapshot.windowAttempts(),
                windowSnapshot.totalUnsafe(),
                windowSnapshot.unsafeRate(),
                windowSnapshot.counts());

        if (metricsEnabled) {
            String json = snapshot.toJson();
            String exportPath = settings.getMetrics().getExportPath();
            ioExecutor.submit(() -> writeFile(exportPath, json));
        }

        LoggingSettings logging = settings.getLogging();
        if (logging.isWarnOnUnsafe() && snapshot.getUnsafeRate() * 100 >= logging.getWarnThresholdPercent()) {
            String message = String.format(
                    "[EzRTP] High unsafe location rejection rate: %.1f%% (%d/%d RTPs) in the last %d minutes",
                    snapshot.getUnsafeRate() * 100,
                    snapshot.getUnsafeAttempts(),
                    snapshot.getTotalAttempts(),
                    settings.getMetrics().getExportIntervalMinutes());
            if (logging.isLogToConsole()) {
                logger.warning(message);
            }
            if (logging.isLogToFile()) {
                String line = Instant.now() + " " + message + "\n";
                String filePath = logging.getFilePath();
                ioExecutor.submit(() -> appendToFile(filePath, line));
            }
        }
    }

    /**
     * Cancels the periodic export task and shuts down the I/O executor gracefully.
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

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

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
                    "[EzRTP] Failed to write unsafe-location metrics to '"
                            + filePath
                            + "': "
                            + e.getMessage());
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
                    "[EzRTP] Failed to write unsafe-location warning to '"
                            + filePath
                            + "': "
                            + e.getMessage());
        }
    }
}
