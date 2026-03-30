package com.skyblockexp.ezrtp.economy;

import org.bukkit.entity.Player;

/**
 * Provides a simplified abstraction for optional economy integrations.
 */
public interface EconomyService {

    /**
     * @return {@code true} when an economy provider is available.
     */
    boolean isEnabled();

    /**
     * Checks whether the player can afford the specified amount.
     */
    boolean hasBalance(Player player, double amount);

    /**
     * Withdraws the specified amount from the player.
     */
    boolean withdraw(Player player, double amount);

    /**
     * Returns a disabled, no-op economy service.
     */
    static EconomyService disabled() {
        return DisabledEconomyService.INSTANCE;
    }

    /**
     * Disabled economy implementation that treats all operations as successful.
     */
    enum DisabledEconomyService implements EconomyService {
        INSTANCE;

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean hasBalance(Player player, double amount) {
            return true;
        }

        @Override
        public boolean withdraw(Player player, double amount) {
            return true;
        }
    }
}

