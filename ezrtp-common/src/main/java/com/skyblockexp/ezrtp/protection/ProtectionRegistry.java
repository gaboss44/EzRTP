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
        this(plugin.getServer() != null ? plugin.getServer().getPluginManager() : null, plugin.getLogger());
    }

    /**
     * Test-friendly constructor that accepts a PluginManager and Logger directly.
     */
    public ProtectionRegistry(PluginManager pluginManager, Logger logger) {
        this.logger = logger != null ? logger : java.util.logging.Logger.getAnonymousLogger();
        registerProvider(new WorldGuardProtectionProvider(pluginManager, this.logger));
        registerProvider(new GriefPreventionProtectionProvider(pluginManager, this.logger));
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
        java.util.List<String> missing = new java.util.ArrayList<>();
        for (String providerId : settings.getProviders()) {
            ProtectionProvider provider = providers.get(providerId);
            if (provider == null || !provider.isAvailable()) {
                missing.add(providerId);
            }
        }
        if (missing.isEmpty()) {
            return;
        }
        if (missing.size() == settings.getProviders().size()) {
            // ALL providers are unavailable — emit a single high-visibility warning
            logger.warning("EzRTP: avoid-claims is enabled but none of the configured protection providers "
                    + missing + " are available. Protected regions will NOT be avoided. "
                    + "Install WorldGuard or GriefPrevention, or set avoid-claims: false in rtp.yml.");
        } else {
            for (String providerId : missing) {
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
