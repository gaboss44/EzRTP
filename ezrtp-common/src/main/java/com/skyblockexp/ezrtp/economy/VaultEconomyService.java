package com.skyblockexp.ezrtp.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Economy integration backed by Vault.
 */
public final class VaultEconomyService implements EconomyService {

    private final Economy economy;
    private final Logger logger;

    public VaultEconomyService(Economy economy, Logger logger) {
        this.economy = economy;
        this.logger = logger;
    }

    @Override
    public boolean isEnabled() {
        return economy != null;
    }

    @Override
    public boolean hasBalance(Player player, double amount) {
        if (economy == null || amount <= 0) {
            return true;
        }
        return economy.has(player, amount);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (economy == null || amount <= 0) {
            return true;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (response.transactionSuccess()) {
            return true;
        }
        if (logger != null) {
            logger.warning(String.format(Locale.US, "Failed to withdraw %.2f from %s: %s", amount, player.getName(),
                    response.errorMessage == null ? "unknown error" : response.errorMessage));
        }
        return false;
    }

    public String getProviderName() {
        return economy != null ? economy.getName() : "Unknown";
    }
}

