package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.economy.EconomyService;
import com.skyblockexp.ezrtp.economy.VaultEconomyService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Handles Vault detection and economy service provisioning.
 */
public final class EconomyCoordinator {

    private final EzRtpPlugin plugin;

    public EconomyCoordinator(EzRtpPlugin plugin) {
        this.plugin = plugin;
    }

    public EconomyService initializeEconomy() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if (!pluginManager.isPluginEnabled("Vault")) {
            plugin.getLogger().info("Vault not found. Random teleport costs will be disabled.");
            return EconomyService.disabled();
        }

        RegisteredServiceProvider<Economy> registration = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (registration == null || registration.getProvider() == null) {
            plugin.getLogger().warning("Vault is installed but no economy provider is registered. Random teleport costs will be disabled.");
            return EconomyService.disabled();
        }

        VaultEconomyService vaultEconomy = new VaultEconomyService(registration.getProvider(), plugin.getLogger());
        plugin.getLogger().info("Economy: " + vaultEconomy.getProviderName() + " (Vault)");
        return vaultEconomy;
    }
}
