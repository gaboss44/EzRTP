package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import com.skyblockexp.ezrtp.teleport.queue.ChunkLoadQueue;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the /rtp stats subcommand and its variants.
 */
public class StatsSubcommand extends Subcommand {

    private static final String BIOMES_SUBCOMMAND = "biomes";
    private static final String RARE_BIOMES_SUBCOMMAND = "rare-biomes";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final EzRtpPlugin plugin;
    private final Supplier<RandomTeleportService> teleportServiceSupplier;

    public StatsSubcommand(EzRtpPlugin plugin, Supplier<RandomTeleportService> teleportServiceSupplier) {
        super("stats", "ezrtp.stats");
        this.plugin = plugin;
        this.teleportServiceSupplier = teleportServiceSupplier;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length > 0 && BIOMES_SUBCOMMAND.equalsIgnoreCase(args[0])) {
            int page = parsePageNumber(args, 1, 1);
            displayBiomeStatistics(sender, page);
        } else if (args.length > 0 && RARE_BIOMES_SUBCOMMAND.equalsIgnoreCase(args[0])) {
            int page = parsePageNumber(args, 1, 1);
            displayRareBiomeStatistics(sender, page);
        } else {
            int page = parsePageNumber(args, 0, 1);
            displayStatistics(sender, page);
        }
        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add(BIOMES_SUBCOMMAND);
            suggestions.add(RARE_BIOMES_SUBCOMMAND);
            return suggestions;
        }

        return Collections.emptyList();
    }

    /**
     * Parses a page number from command arguments with a default fallback.
     */
    private int parsePageNumber(String[] args, int index, int defaultPage) {
        if (args.length <= index) {
            return defaultPage;
        }
        try {
            int page = Integer.parseInt(args[index]);
            return Math.max(1, page);
        } catch (NumberFormatException e) {
            return defaultPage;
        }
    }

    /**
     * Displays RTP statistics to the sender with pagination support.
     */
    private void displayStatistics(CommandSender sender, int page) {
        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null) {
            MessageUtil.send(sender, "<red>Teleport service not initialized.</red>");
            return;
        }

        RtpStatistics stats = service.getStatistics();
        BiomeLocationCache cache = service.getBiomeCache();
        BiomeLocationCache.CacheStats cacheStats = cache.getStats();

        MessageUtil.send(sender, "<gold><bold>═══════════════════════════════════════</bold></gold>");
        MessageUtil.send(sender, "<yellow><bold>EzRTP Statistics</bold></yellow>");
        MessageUtil.send(sender, "<gold><bold>═══════════════════════════════════════</bold></gold>");
        sender.sendMessage("");

        // Overall RTP Statistics
        MessageUtil.send(sender, "<gold><bold>Overall RTP Performance:</bold></gold>");
        MessageUtil.send(sender, "  <gray>Total Attempts: <white>" + stats.getTotalAttempts() + "</white></gray>");
        MessageUtil.send(
                sender,
                String.format(
                        "  <gray>Successes: <green>%d</green> <gray>(<green>%.1f%%</green>)</gray>",
                        stats.getTotalSuccesses(), stats.getSuccessRate()));
        MessageUtil.send(sender, "  <gray>Failures: <red>" + stats.getTotalFailures() + "</red></gray>");
        MessageUtil.send(
                sender,
                "  <gray>Average Teleport Time: <white>"
                        + String.format("%.0f", stats.getAverageTeleportTimeMs())
                        + "ms</white></gray>");
        sender.sendMessage("");

        // Cache Statistics
        if (cache.isEnabled()) {
            MessageUtil.send(sender, "<gold><bold>Biome Cache Statistics:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <green>Enabled</green></gray>");
            MessageUtil.send(
                    sender,
                    String.format(
                            "  <gray>Cache Hits: <green>%d</green> <gray>(<green>%.1f%%</green>)</gray>",
                            stats.getTotalCacheHits(), stats.getCacheHitRate()));
            MessageUtil.send(
                    sender, "  <gray>Cache Misses: <yellow>" + stats.getTotalCacheMisses() + "</yellow></gray>");
            MessageUtil.send(sender, "  <gray>Cached Worlds: <white>" + cacheStats.worldCount() + "</white></gray>");
            MessageUtil.send(sender, "  <gray>Cached Biomes: <white>" + cacheStats.biomeCount() + "</white></gray>");
            MessageUtil.send(
                    sender, "  <gray>Cached Locations: <white>" + cacheStats.locationCount() + "</white></gray>");
        } else {
            MessageUtil.send(sender, "<gold><bold>Biome Cache Statistics:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <red>Disabled</red></gray>");
            MessageUtil.send(
                    sender, "  <yellow>Enable pre-caching in config.yml to improve biome RTP success rates!</yellow>");
        }
        sender.sendMessage("");

        // Rare Biome Optimization Statistics
        RareBiomeRegistry registry = service.getRareBiomeRegistry();
        ChunkLoadQueue chunkQueue = service.getChunkLoadQueue();
        if (registry != null && registry.isEnabled()) {
            RareBiomeRegistry.RegistryStats registryStats = registry.getStats();
            MessageUtil.send(sender, "<gold><bold>Rare Biome Optimization:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <green>Enabled</green></gray>");
            MessageUtil.send(
                    sender,
                    "  <gray>Tracked Rare Biomes: <white>" + registryStats.rareBiomeCount() + "</white></gray>");
            MessageUtil.send(
                    sender, "  <gray>Registered Hotspots: <white>" + registryStats.hotspotCount() + "</white></gray>");
            MessageUtil.send(
                    sender,
                    "  <gray>Weighted Search Uses: <white>" + stats.getWeightedSearchUses() + "</white></gray>");
            MessageUtil.send(
                    sender, "  <gray>Uniform Search Uses: <white>" + stats.getUniformSearchUses() + "</white></gray>");
            if (chunkQueue != null && chunkQueue.isEnabled()) {
                MessageUtil.send(sender, "  <gray>Chunk Load Queue: <green>Enabled</green></gray>");
            }
        } else {
            MessageUtil.send(sender, "<gold><bold>Rare Biome Optimization:</bold></gold>");
            MessageUtil.send(sender, "  <gray>Status: <red>Disabled</red></gray>");
            MessageUtil.send(sender, "  <yellow>Enable in config.yml to improve rare biome RTP performance!</yellow>");
        }
        sender.sendMessage("");

        // Per-Biome Statistics with pagination
        var biomeStats = stats.getBiomeStats();
        if (!biomeStats.isEmpty()) {
            var sortedBiomes = biomeStats.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(
                            b.getValue().getAttempts(), a.getValue().getAttempts()))
                    .toList();
            PageWindow pageWindow = buildPageWindow(page, sortedBiomes.size());
            MessageUtil.send(
                    sender,
                    String.format(
                            "<gold><bold>Top Biome Statistics</bold> <gray>(Page %d/%d)</gray>:",
                            pageWindow.currentPage(), pageWindow.totalPages()));
            for (int i = pageWindow.startIndex(); i < pageWindow.endIndex(); i++) {
                var entry = sortedBiomes.get(i);
                var biome = entry.getKey();
                var bStats = entry.getValue();
                String biomeName = formatBiomeName(biome);
                String rank = String.format("#%d", i + 1);
                MessageUtil.send(
                        sender,
                        String.format(
                                "  <gray>%s <white>%s</white> <gray>(<green>%d</green> attempts,"
                                        + " <green>%.1f%%</green> success)</gray>",
                                rank, biomeName, bStats.getAttempts(), bStats.getSuccessRate()));
            }
            if (pageWindow.totalPages() > 1) {
                sender.sendMessage("");
                MessageUtil.send(
                        sender,
                        String.format(
                                "<gray>Page %d/%d - Use /rtp stats %d for next page</gray>",
                                pageWindow.currentPage(), pageWindow.totalPages(), pageWindow.currentPage() + 1));
            }
        } else {
            MessageUtil.send(sender, "<gold><bold>Biome Statistics:</bold></gold>");
            MessageUtil.send(sender, "  <gray>No biome data available yet.</gray>");
        }
        sender.sendMessage("");

        // Failure Breakdown
        RtpStatistics.FailureCauses causes = stats.getFailureCauses();
        if (causes.total() > 0) {
            MessageUtil.send(sender, "<gold><bold>Failure Causes:</bold></gold>");
            if (causes.safety() > 0) {
                MessageUtil.send(
                        sender,
                        String.format(
                                "  <gray>Safety: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>",
                                causes.safety(), (double) causes.safety() / causes.total() * 100));
            }
            if (causes.biome() > 0) {
                MessageUtil.send(
                        sender,
                        String.format(
                                "  <gray>Biome: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>",
                                causes.biome(), (double) causes.biome() / causes.total() * 100));
            }
            if (causes.protection() > 0) {
                MessageUtil.send(
                        sender,
                        String.format(
                                "  <gray>Protection: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>",
                                causes.protection(), (double) causes.protection() / causes.total() * 100));
            }
            if (causes.timeout() > 0) {
                MessageUtil.send(
                        sender,
                        String.format(
                                "  <gray>Timeout: <red>%d</red> <gray>(<red>%.1f%%</red>)</gray>",
                                causes.timeout(), (double) causes.timeout() / causes.total() * 100));
            }
            sender.sendMessage("");
        }

        // Recommendations
        if (cache.isEnabled() && stats.getCacheHitRate() < 30.0 && stats.getTotalCacheMisses() > 10) {
            MessageUtil.send(sender, "<yellow><bold>⚠ Warning:</bold></yellow>");
            MessageUtil.send(sender, "  <gray>Low cache hit rate detected. Consider increasing</gray>");
            MessageUtil.send(sender, "  <gray>'biomes.pre-cache.max-per-biome' or 'warmup-size' in config.yml</gray>");
            sender.sendMessage("");
        }

        // bStats Link
        MessageUtil.send(sender, "<gold><bold>View More Metrics:</bold></gold>");
        MessageUtil.send(sender, "  <gray>bStats: <aqua>https://bstats.org/plugin/bukkit/EzRTP/27735</aqua></gray>");
        MessageUtil.send(sender, "<gold><bold>═══════════════════════════════════════</bold></gold>");
    }

    /**
     * Displays detailed biome statistics to the sender with pagination support.
     */
    private void displayBiomeStatistics(CommandSender sender, int page) {
        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.COMMAND_SERVICE_NOT_INITIALIZED));
            return;
        }

        RtpStatistics stats = service.getStatistics();
        BiomeLocationCache cache = service.getBiomeCache();
        BiomeLocationCache.CacheStats cacheStats = cache.getStats();
        var biomeStats = stats.getBiomeStats();

        sender.sendMessage("§6§l═══════════════════════════════════════");
        sender.sendMessage("§e§lEzRTP Biome Statistics");
        sender.sendMessage("§6§l═══════════════════════════════════════");
        sender.sendMessage("");

        if (biomeStats.isEmpty()) {
            sender.sendMessage("§7No biome data available yet.");
            sender.sendMessage("§7Biome statistics are collected as players use /rtp");
            sender.sendMessage("§7with biome filtering enabled.");
            sender.sendMessage("");
            sender.sendMessage("§6§l═══════════════════════════════════════");
            return;
        }

        // Cache info section
        if (cache.isEnabled()) {
            sender.sendMessage("§6§lCache Information:");
            sender.sendMessage(String.format(
                    "  §7Cached Biomes: §f%d §7| §7Cached Locations: §f%d",
                    cacheStats.biomeCount(), cacheStats.locationCount()));
            sender.sendMessage(String.format(
                    "  §7Cache Hit Rate: §a%.1f%% §7(§a%d §7hits, §e%d §7misses)",
                    stats.getCacheHitRate(), stats.getTotalCacheHits(), stats.getTotalCacheMisses()));
            sender.sendMessage("");
        }

        // Sort by total attempts (most active biomes first)
        var sortedBiomes = biomeStats.entrySet().stream()
                .sorted((a, b) ->
                        Integer.compare(b.getValue().getAttempts(), a.getValue().getAttempts()))
                .toList();

        PageWindow pageWindow = buildPageWindow(page, sortedBiomes.size());

        sender.sendMessage(String.format(
                "§6§lBiomes by Activity §7(Page %d/%d, Total: %d biomes):",
                pageWindow.currentPage(), pageWindow.totalPages(), biomeStats.size()));
        sender.sendMessage("");

        // Display biomes for current page
        for (int i = pageWindow.startIndex(); i < pageWindow.endIndex(); i++) {
            var entry = sortedBiomes.get(i);
            var biome = entry.getKey();
            var bStats = entry.getValue();

            String biomeName = formatBiomeName(biome);
            String rank = String.format("§e#%d", i + 1);

            sender.sendMessage(String.format("  %s §f%s", rank, biomeName));
            sender.sendMessage(String.format(
                    "    §7Attempts: §f%d §7| §7Successes: §a%d §7(§a%.1f%%§7)",
                    bStats.getAttempts(), bStats.getSuccesses(), bStats.getSuccessRate()));
            sender.sendMessage(String.format(
                    "    §7Avg Time: §f%.0fms §7| §7Failures: §c%d", bStats.getAverageTimeMs(), bStats.getFailures()));

            if (i < pageWindow.endIndex() - 1) {
                sender.sendMessage("");
            }
        }

        // Pagination navigation
        if (pageWindow.totalPages() > 1) {
            sender.sendMessage("");
            sender.sendMessage("§6§lNavigation:");
            if (pageWindow.currentPage() < pageWindow.totalPages()) {
                sender.sendMessage(
                        String.format("  §7Next page: §6/rtp stats biomes %d", pageWindow.currentPage() + 1));
            }
            if (pageWindow.currentPage() > 1) {
                sender.sendMessage(
                        String.format("  §7Previous page: §6/rtp stats biomes %d", pageWindow.currentPage() - 1));
            }
        }

        sender.sendMessage("");
        sender.sendMessage("§6§lLegend:");
        sender.sendMessage("  §7Attempts: Total RTP attempts targeting this biome");
        sender.sendMessage("  §7Successes: Successful teleports to this biome");
        sender.sendMessage("  §7Avg Time: Average time to find a safe location");
        sender.sendMessage("§6§l═══════════════════════════════════════");
    }

    /**
     * Displays detailed rare biome statistics to the sender with pagination support.
     */
    private void displayRareBiomeStatistics(CommandSender sender, int page) {
        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null) {
            sender.sendMessage("§cTeleport service not initialized.");
            return;
        }

        RareBiomeRegistry registry = service.getRareBiomeRegistry();
        if (registry == null || !registry.isEnabled()) {
            sender.sendMessage("§cRare biome optimization is not enabled.");
            sender.sendMessage("§7Enable it in config.yml under biomes.rare-biome-optimization");
            return;
        }

        RtpStatistics stats = service.getStatistics();
        RareBiomeRegistry.RegistryStats registryStats = registry.getStats();
        ChunkLoadQueue chunkQueue = service.getChunkLoadQueue();

        sender.sendMessage("§6§l═══════════════════════════════════════");
        sender.sendMessage("§e§lRare Biome Optimization Statistics");
        sender.sendMessage("§6§l═══════════════════════════════════════");
        sender.sendMessage("");

        // System Overview
        sender.sendMessage("§6§lSystem Status:");
        sender.sendMessage(String.format("  §7Optimization: §aEnabled"));
        sender.sendMessage(String.format("  §7Tracked Rare Biomes: §f%d", registryStats.rareBiomeCount()));
        sender.sendMessage(String.format("  §7Registered Hotspots: §f%d", registryStats.hotspotCount()));
        sender.sendMessage(String.format("  §7Worlds with Hotspots: §f%d", registryStats.worldCount()));
        sender.sendMessage("");

        // Search Strategy Statistics
        sender.sendMessage("§6§lSearch Strategy Performance:");
        int totalSearches = stats.getWeightedSearchUses() + stats.getUniformSearchUses();
        if (totalSearches > 0) {
            double weightedPercent = (double) stats.getWeightedSearchUses() / totalSearches * 100.0;
            sender.sendMessage(String.format(
                    "  §7Weighted Searches: §a%d §7(§a%.1f%%§7)", stats.getWeightedSearchUses(), weightedPercent));
            sender.sendMessage(String.format(
                    "  §7Uniform Searches: §e%d §7(§e%.1f%%§7)",
                    stats.getUniformSearchUses(), 100.0 - weightedPercent));
        } else {
            sender.sendMessage("  §7No searches recorded yet");
        }
        sender.sendMessage("");

        // Chunk Load Queue Statistics
        if (chunkQueue != null && chunkQueue.isEnabled()) {
            sender.sendMessage("§6§lChunk Load Queue:");
            sender.sendMessage(String.format("  §7Status: §aEnabled"));
            sender.sendMessage(String.format("  §7Total Uses: §f%d", stats.getChunkLoadQueueHits()));
            sender.sendMessage(String.format("  §7Current Queue Size: §f%d", chunkQueue.getQueueSize()));
            sender.sendMessage("");
        }

        // Hotspot Distribution by Rare Biome with pagination
        var biomeDistribution = registryStats.biomeDistribution();
        if (!biomeDistribution.isEmpty()) {
            // Sort by hotspot count (most to least)
            var sortedBiomes = biomeDistribution.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .toList();

            PageWindow pageWindow = buildPageWindow(page, sortedBiomes.size());

            sender.sendMessage(String.format(
                    "§6§lHotspot Distribution §7(Page %d/%d):", pageWindow.currentPage(), pageWindow.totalPages()));
            sender.sendMessage("");

            for (int i = pageWindow.startIndex(); i < pageWindow.endIndex(); i++) {
                var entry = sortedBiomes.get(i);
                var biome = entry.getKey();
                int hotspotCount = entry.getValue();

                String biomeName = formatBiomeName(biome);
                String rank = String.format("§e#%d", i + 1);

                sender.sendMessage(String.format(
                        "  %s §f%s §7- §a%d §7hotspot%s", rank, biomeName, hotspotCount, hotspotCount == 1 ? "" : "s"));

                if (i < pageWindow.endIndex() - 1) {
                    sender.sendMessage("");
                }
            }

            // Pagination navigation
            if (pageWindow.totalPages() > 1) {
                sender.sendMessage("");
                sender.sendMessage("§6§lNavigation:");
                if (pageWindow.currentPage() < pageWindow.totalPages()) {
                    sender.sendMessage(
                            String.format("  §7Next page: §6/rtp stats rare-biomes %d", pageWindow.currentPage() + 1));
                }
                if (pageWindow.currentPage() > 1) {
                    sender.sendMessage(String.format(
                            "  §7Previous page: §6/rtp stats rare-biomes %d", pageWindow.currentPage() - 1));
                }
            }
        } else {
            sender.sendMessage("§6§lHotspot Distribution:");
            sender.sendMessage("  §7No hotspots registered yet.");
        }

        sender.sendMessage("");

        // Performance Insights
        if (registryStats.hotspotCount() > 0 && totalSearches > 0) {
            sender.sendMessage("§6§lPerformance Insights:");
            double avgHotspotsPerBiome = (double) registryStats.hotspotCount() / registryStats.rareBiomeCount();
            sender.sendMessage(String.format("  §7Average hotspots per rare biome: §f%.1f", avgHotspotsPerBiome));

            if (stats.getWeightedSearchUses() > stats.getUniformSearchUses()) {
                sender.sendMessage("  §7Weighted search is preferred for rare biomes");
                sender.sendMessage("  §7This indicates good hotspot coverage");
            } else {
                sender.sendMessage("  §7Uniform search is being used more often");
                sender.sendMessage("  §7Consider adding more hotspots for better performance");
            }
            sender.sendMessage("");
        }

        // Recommendations
        sender.sendMessage("§6§lRecommendations:");
        if (registryStats.hotspotCount() == 0) {
            sender.sendMessage("  §7Add hotspots using the rare biome registry API");
            sender.sendMessage("  §7Hotspots help EzRTP find rare biomes faster");
        } else {
            sender.sendMessage("  §7Monitor hotspot usage and add more if needed");
            sender.sendMessage("  §7Weighted search should dominate for optimal performance");
        }

        sender.sendMessage("");
        sender.sendMessage("§6§l═══════════════════════════════════════");
    }

    private PageWindow buildPageWindow(int requestedPage, int itemCount) {
        int totalPages = Math.max(1, (int) Math.ceil((double) itemCount / DEFAULT_PAGE_SIZE));
        int currentPage = Math.min(requestedPage, totalPages);
        int startIndex = (currentPage - 1) * DEFAULT_PAGE_SIZE;
        int endIndex = Math.min(startIndex + DEFAULT_PAGE_SIZE, itemCount);
        return new PageWindow(currentPage, totalPages, startIndex, endIndex);
    }

    private record PageWindow(int currentPage, int totalPages, int startIndex, int endIndex) {}

    /**
     * Formats a biome name for display (converts DARK_FOREST to "Dark Forest").
     */
    private String formatBiomeName(org.bukkit.block.Biome biome) {
        String name = biome.name().replace("_", " ");
        String[] words = name.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            formatted.append(words[i].substring(0, 1).toUpperCase(Locale.ROOT)).append(words[i].substring(1));
        }

        return formatted.toString();
    }
}
