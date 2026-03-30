package com.skyblockexp.ezrtp.protection;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GriefPreventionProtectionProvider implements ProtectionProvider {
    private final Logger logger;
    private final boolean available;
    private final Object dataStore;
    private final Method getClaimAtMethod;

    public GriefPreventionProtectionProvider(PluginManager pluginManager, Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        Plugin plugin = pluginManager != null ? pluginManager.getPlugin("GriefPrevention") : null;
        if (plugin == null || !plugin.isEnabled()) {
            this.available = false;
            this.dataStore = null;
            this.getClaimAtMethod = null;
            return;
        }
        Object resolvedDataStore = null;
        Method resolvedGetClaimAt = null;
        boolean resolvedAvailable = false;
        try {
            ClassLoader classLoader = plugin.getClass().getClassLoader();
            Class<?> pluginClass = classLoader.loadClass("me.ryanhamshire.GriefPrevention.GriefPrevention");
            Field dataStoreField = pluginClass.getField("dataStore");
            resolvedDataStore = dataStoreField.get(plugin);
            if (resolvedDataStore != null) {
                Class<?> dataStoreInterface = classLoader.loadClass("me.ryanhamshire.GriefPrevention.DataStore");
                try {
                    Class<?> claimClass = classLoader.loadClass("me.ryanhamshire.GriefPrevention.Claim");
                    resolvedGetClaimAt = dataStoreInterface.getMethod("getClaimAt", Location.class, boolean.class, boolean.class, claimClass);
                } catch (NoSuchMethodException e) {
                    try {
                        Class<?> claimClass = classLoader.loadClass("me.ryanhamshire.GriefPrevention.Claim");
                        resolvedGetClaimAt = resolvedDataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, boolean.class, claimClass);
                    } catch (NoSuchMethodException ex2) {
                        logger.log(Level.WARNING, "GriefPrevention DataStore does not have the required getClaimAt method.", ex2);
                    }
                }

                if (resolvedGetClaimAt != null) {
                    resolvedAvailable = true;
                } else {
                    logger.warning("GriefPrevention getClaimAt method not found; RTP protection will be disabled for this provider.");
                }
            } else {
                logger.warning("GriefPrevention dataStore field is null; RTP protection will be disabled for this provider.");
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            logger.log(Level.WARNING, "Failed to initialize GriefPrevention RTP protection hook.", ex);
        }
        this.available = resolvedAvailable;
        this.dataStore = resolvedDataStore;
        this.getClaimAtMethod = resolvedGetClaimAt;
    }

    @Override
    public String getId() {
        return "griefprevention";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isLocationProtected(Location location) {
        if (!available || location == null || dataStore == null || getClaimAtMethod == null) {
            return false;
        }
        try {
            Object claim = getClaimAtMethod.invoke(dataStore, location, false, false, null);
            
            return claim != null;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            logger.log(Level.WARNING, "Failed to query GriefPrevention claims for RTP protection check.", ex);
            return false;
        }
    }
}
