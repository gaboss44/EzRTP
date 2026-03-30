package com.skyblockexp.ezrtp.protection;

import com.skyblockexp.ezrtp.config.ProtectionSettings;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public final class ProtectionRegistry {
    private final Map<String, ProtectionProvider> providers = new HashMap<>();
    private final Logger logger;

    public ProtectionRegistry(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        registerProvider(new WorldGuardProtectionProvider(pluginManager, logger));
        registerProvider(new GriefPreventionProtectionProvider(pluginManager, logger));
    }

    public Optional<String> findProtectionProvider(Location location, ProtectionSettings settings) {
        if (settings == null || !settings.isAvoidClaims()) {
            return Optional.empty();
        }
        for (String providerId : settings.getProviders()) {
            ProtectionProvider provider = providers.get(providerId);
            if (provider == null || !provider.isAvailable()) {
                continue;
            }
            if (provider.isLocationProtected(location)) {
                return Optional.of(providerId);
            }
        }
        return Optional.empty();
    }

    public void warnMissingProviders(ProtectionSettings settings) {
        if (settings == null || !settings.isAvoidClaims()) {
            return;
        }
        for (String providerId : settings.getProviders()) {
            ProtectionProvider provider = providers.get(providerId);
            if (provider == null || !provider.isAvailable()) {
                logger.warning("EzRTP protection provider '" + providerId
                        + "' is configured but not available; protected regions will not be avoided for this provider.");
            }
        }
    }

    public WorldGuardProtectionProvider getWorldGuardProvider() {
        ProtectionProvider provider = providers.get("worldguard");
        if (provider instanceof WorldGuardProtectionProvider worldGuardProtectionProvider) {
            return worldGuardProtectionProvider;
        }
        return null;
    }

    private void registerProvider(ProtectionProvider provider) {
        providers.put(provider.getId(), provider);
    }
}
