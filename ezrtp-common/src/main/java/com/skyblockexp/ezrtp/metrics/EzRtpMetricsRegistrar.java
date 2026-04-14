package com.skyblockexp.ezrtp.metrics;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationCause;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bstats.charts.SimplePie;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles bStats metrics registration and chart wiring using suppliers to always reflect
 * the latest teleport service statistics.
 */
public final class EzRtpMetricsRegistrar {

    private static final int BSTATS_PLUGIN_ID = 27735;
    private static final double[] TELEPORT_TIME_BUCKET_THRESHOLDS = {500, 2000, 5000, 10000, 30000};
    private static final String[] TELEPORT_TIME_BUCKET_LABELS = {
            "<500ms",
            "500-1999ms",
            "2000-4999ms",
            "5000-9999ms",
            "10000-29999ms",
            "30000ms+"
    };

    private final EzRtpPlugin plugin;
    private final Supplier<RandomTeleportService> teleportServiceSupplier;
    private Metrics metrics;

    public EzRtpMetricsRegistrar(EzRtpPlugin plugin, Supplier<RandomTeleportService> teleportServiceSupplier) {
        this.plugin = plugin;
        this.teleportServiceSupplier = teleportServiceSupplier;
    }

    public void register() {
        if (metrics != null) {
            return;
        }
        boolean enableBstats = plugin.getConfig().getBoolean("enable-bstats", true);
        if (!enableBstats) {
            plugin.getLogger().info("bStats metrics disabled in config.");
            return;
        }
        try {
            metrics = new Metrics(plugin, BSTATS_PLUGIN_ID);
            metrics.addCustomChart(new SingleLineChart("cached_locations", () -> {
                RandomTeleportService service = teleportServiceSupplier.get();
                if (service != null && service.getBiomeCache() != null) {
                    return service.getBiomeCache().getStats().locationCount();
                }
                return 0;
            }));

            metrics.addCustomChart(new SingleLineChart("cache_hit_rate", () -> {
                RandomTeleportService service = teleportServiceSupplier.get();
                if (service != null && service.getStatistics() != null) {
                    return (int) service.getStatistics().getCacheHitRate();
                }
                return 0;
            }));

            metrics.addCustomChart(new SimplePie("rtp_success_distribution", this::describeSuccessRateBucket));
            metrics.addCustomChart(new SimplePie("teleport_time_distribution", this::describeAverageTeleportTimeBucket));
            metrics.addCustomChart(new SimplePie("failure_cause", this::describeTopFailureCause));
            metrics.addCustomChart(new SimplePie("unsafe_location_cause", this::describeTopUnsafeLocationCause));

            metrics.addCustomChart(new SimplePie("cache_enabled", () -> {
                RandomTeleportService service = teleportServiceSupplier.get();
                if (service != null && service.getBiomeCache() != null) {
                    return service.getBiomeCache().isEnabled() ? "Enabled" : "Disabled";
                }
                return "Unknown";
            }));

            metrics.addCustomChart(new SingleLineChart("total_rtps", () -> {
                RandomTeleportService service = teleportServiceSupplier.get();
                if (service != null && service.getStatistics() != null) {
                    return service.getStatistics().getTotalAttempts();
                }
                return 0;
            }));

            metrics.addCustomChart(new SingleLineChart("total_wait_time_seconds", () -> {
                RandomTeleportService service = teleportServiceSupplier.get();
                if (service != null && service.getStatistics() != null) {
                    return (int) (service.getStatistics().getTotalTeleportTimeMs() / 1000);
                }
                return 0;
            }));

            plugin.getLogger().info("Metrics: bStats (https://bstats.org/plugin/bukkit/EzRTP/27735)");
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Failed to start bStats metrics: " + throwable.getMessage());
        }
    }

    private String describeTopFailureCause() {
        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null || service.getStatistics() == null) {
            return "No Data";
        }
        var causes = service.getStatistics().getFailureCauses();
        if (causes.total() == 0) {
            return "No Failures";
        }

        Map<String, Integer> buckets = new LinkedHashMap<>();
        buckets.put("Safety", causes.safety());
        buckets.put("Biome", causes.biome());
        buckets.put("Protection", causes.protection());
        buckets.put("Timeout", causes.timeout());
        buckets.put("Economy", causes.economy());
        buckets.put("Offline/Cancelled", causes.playerOfflineOrCancelled());
        buckets.put("Teleport API", causes.teleportApi());
        buckets.put("Search Error", causes.genericSearchError());

        return buckets.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private String describeTopUnsafeLocationCause() {
        UnsafeLocationMonitor monitor = plugin.getUnsafeLocationMonitor();
        if (monitor == null || !monitor.isEnabled()) {
            return "No Data";
        }
        var allTime = monitor.getStatistics().getAllTimeCounts();
        return allTime.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().getDisplayName())
                .orElse("No Data");
    }

    private String describeSuccessRateBucket() {
        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null || service.getStatistics() == null) {
            return "No Data";
        }
        double rate = service.getStatistics().getSuccessRate();
        if (Double.isNaN(rate)) {
            return "No Data";
        }
        if (rate >= 95) return "95-100%";
        if (rate >= 90) return "90-94%";
        if (rate >= 80) return "80-89%";
        if (rate >= 70) return "70-79%";
        if (rate >= 60) return "60-69%";
        if (rate >= 50) return "50-59%";
        return "<50%";
    }

    private String describeAverageTeleportTimeBucket() {
        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null || service.getStatistics() == null) {
            return "No Data";
        }
        double avgTime = service.getStatistics().getAverageTeleportTimeMs();
        if (Double.isNaN(avgTime) || avgTime < 0) {
            return "No Data";
        }
        for (int i = 0; i < TELEPORT_TIME_BUCKET_THRESHOLDS.length; i++) {
            if (avgTime < TELEPORT_TIME_BUCKET_THRESHOLDS[i]) {
                return TELEPORT_TIME_BUCKET_LABELS[i];
            }
        }
        return TELEPORT_TIME_BUCKET_LABELS[TELEPORT_TIME_BUCKET_LABELS.length - 1];
    }
}
