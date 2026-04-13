package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationCause;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationStatistics;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the /rtp unsafe-stats subcommand, showing a windowed breakdown of unsafe location
 * rejection causes.
 */
public class UnsafeStatsSubcommand extends Subcommand {

    private final EzRtpPlugin plugin;

    public UnsafeStatsSubcommand(EzRtpPlugin plugin) {
        super("unsafe-stats", "ezrtp.stats");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        sender.sendMessage("§6§l═══════════════════════════════════════");
        sender.sendMessage("§e§lEzRTP Unsafe Location Statistics");
        sender.sendMessage("§6§l═══════════════════════════════════════");
        sender.sendMessage("");

        UnsafeLocationMonitor monitor = plugin.getUnsafeLocationMonitor();
        if (monitor == null || !monitor.isEnabled()) {
            sender.sendMessage("§cUnsafe location monitoring is disabled.");
            sender.sendMessage("§7Enable it by setting §funsafe-location-monitoring.monitoring.enabled: true");
            sender.sendMessage("§7in §fplugins/EzRTP/unsafe-location-monitoring.yml§7 and reloading the plugin.");
            sender.sendMessage("");
            sender.sendMessage("§6§l═══════════════════════════════════════");
            return true;
        }

        UnsafeLocationStatistics stats = monitor.getStatistics();
        Map<UnsafeLocationCause, Long> allTime = stats.getAllTimeCounts();
        long allTimeTotal = stats.getAllTimeTotal();

        sender.sendMessage("§6§lAll-Time Unsafe Rejections:");
        sender.sendMessage(String.format("  §7Total: §c%d", allTimeTotal));
        sender.sendMessage("");

        sender.sendMessage("§6§lBreakdown by Cause:");
        allTime.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<UnsafeLocationCause, Long>comparingByValue().reversed())
                .forEach(e -> {
                    double pct = allTimeTotal > 0 ? (e.getValue() * 100.0 / allTimeTotal) : 0.0;
                    sender.sendMessage(String.format(
                            "  §7%s: §c%d §7(§c%.1f%%§7)",
                            e.getKey().getDisplayName(), e.getValue(), pct));
                });

        if (allTimeTotal == 0) {
            sender.sendMessage("  §7No unsafe rejections recorded yet.");
        }

        sender.sendMessage("");
        sender.sendMessage("§6§l═══════════════════════════════════════");
        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
