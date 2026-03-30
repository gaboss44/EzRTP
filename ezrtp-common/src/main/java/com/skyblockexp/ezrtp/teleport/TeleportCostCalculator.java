package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.economy.EconomyService;

import org.bukkit.entity.Player;

/**
 * Handles teleport cost calculation and payment processing.
 */
public final class TeleportCostCalculator {

    private EconomyService economyService;
    private java.util.function.BiFunction<Player, RandomTeleportSettings, Double> costResolver;

    public TeleportCostCalculator(EconomyService economyService,
                                  java.util.function.BiFunction<Player, RandomTeleportSettings, Double> costResolver) {
        this.economyService = economyService != null ? economyService : EconomyService.disabled();
        this.costResolver = costResolver;
    }

    /**
     * Calculates the teleport cost for a player and settings.
     */
    public double calculateCost(Player player, RandomTeleportSettings teleportSettings) {
        if (teleportSettings == null) {
            return 0.0D;
        }
        if (costResolver == null) {
            return teleportSettings.getTeleportCost();
        }
        Double resolved = costResolver.apply(player, teleportSettings);
        return resolved != null ? resolved : teleportSettings.getTeleportCost();
    }

    /**
     * Checks if payment is required for the given reason and cost.
     */
    public boolean requiresPayment(TeleportReason reason, double cost) {
        return reason == TeleportReason.COMMAND && cost > 0.0D;
    }

    /**
     * Checks if the player has sufficient balance for the cost.
     */
    public boolean hasBalance(Player player, double cost) {
        return economyService.hasBalance(player, cost);
    }

    /**
     * Withdraws the cost from the player's balance.
     */
    public boolean withdraw(Player player, double cost) {
        return economyService.withdraw(player, cost);
    }

    /**
     * Sets the economy service.
     */
    public void setEconomyService(EconomyService economyService) {
        this.economyService = economyService != null ? economyService : EconomyService.disabled();
    }

    /**
     * Sets the cost resolver function.
     */
    public void setCostResolver(java.util.function.BiFunction<Player, RandomTeleportSettings, Double> costResolver) {
        this.costResolver = costResolver;
    }
}