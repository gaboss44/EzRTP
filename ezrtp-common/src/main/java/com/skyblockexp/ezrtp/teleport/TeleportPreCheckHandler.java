package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Handles pre-teleport validation checks including memory safety and payment verification.
 */
public final class TeleportPreCheckHandler {

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final MessageProvider messageProvider;
    private final TeleportCostCalculator costCalculator;

    public TeleportPreCheckHandler(org.bukkit.plugin.java.JavaPlugin plugin,
                                   MessageProvider messageProvider,
                                   TeleportCostCalculator costCalculator) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        this.messageProvider = messageProvider;
        this.costCalculator = costCalculator;
    }

    /**
     * Performs critical memory check before teleport.
     * Cancels teleport if memory is too low.
     */
    public boolean checkMemorySafety(Player player, Consumer<Boolean> callback) {
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
        long totalMemoryMb = runtime.totalMemory() / (1024L * 1024L);
        if (freeMemoryMb < 128) { // Less than 128MB free memory
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORTING, player));
            scheduler.runTaskLater(plugin, () -> {
                com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.WORLD_MISSING,
                    Map.of("world", "Server memory too low for teleport"), player));
                if (callback != null) callback.accept(false);
            }, 20L); // Delay message to avoid spam
            plugin.getLogger().warning(String.format("[EzRTP] Teleport cancelled due to critically low memory: %dMB free / %dMB total",
                freeMemoryMb, totalMemoryMb));
            return false;
        }
        return true;
    }

    /**
     * Checks if player has sufficient funds for teleport.
     */
    public boolean checkPayment(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
        double resolvedCost = costCalculator.calculateCost(player, teleportSettings);
        if (costCalculator.requiresPayment(reason, resolvedCost)
            && !costCalculator.hasBalance(player, resolvedCost)) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.INSUFFICIENT_FUNDS,
                Map.of("cost", String.format(java.util.Locale.US, "%.2f", resolvedCost)), player));
            return false;
        }
        return true;
    }

    /**
     * Withdraws payment if required.
     */
    public boolean processPayment(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
        double resolvedCost = costCalculator.calculateCost(player, teleportSettings);
        if (costCalculator.requiresPayment(reason, resolvedCost)
            && !costCalculator.withdraw(player, resolvedCost)) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.INSUFFICIENT_FUNDS,
                Map.of("cost", String.format(java.util.Locale.US, "%.2f", resolvedCost)), player));
            return false;
        }
        return true;
    }
}