package com.skyblockexp.ezrtp.paper;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Compatibility entrypoint so Paper can identify this module jar as a plugin artifact.
 */
public final class EzRtpPaperModulePlugin extends JavaPlugin {

    private static final String[][] REGISTRY_PROVIDER_PAIRS = {
            {"com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry", "com.skyblockexp.ezrtp.platform.PaperPlatformRuntimeProvider"},
            {"com.skyblockexp.ezrtp.platform.ChunkLoadStrategyRegistry", "com.skyblockexp.ezrtp.platform.PaperChunkLoadStrategyProvider"},
            {"com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry", "com.skyblockexp.ezrtp.platform.PaperPlatformGuiBridgeProvider"},
            {"com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry", "com.skyblockexp.ezrtp.platform.PaperPlatformMessageServiceProvider"},
            {"com.skyblockexp.ezrtp.platform.PlatformSenderBridgeRegistry", "com.skyblockexp.ezrtp.platform.PaperPlatformSenderBridgeProvider"}
    };

    @Override
    public void onEnable() {
        int registered = 0;
        Logger logger = getLogger();
        for (String[] pair : REGISTRY_PROVIDER_PAIRS) {
            if (registerProvider(logger, pair[0], pair[1])) {
                registered++;
            }
        }
        if (registered > 0) {
            logger.info("Registered " + registered + " Paper platform adapters for EzRTP.");
        } else {
            logger.warning("No EzRTP registries were accessible from EzRTPPaperModule. This is safe, but module providers were not registered.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("EzRTP Paper platform module disabled.");
    }

    static boolean registerProvider(Logger logger, String registryClassName, String providerClassName) {
        try {
            Class<?> registryClass = Class.forName(registryClassName);
            Class<?> providerClass = Class.forName(providerClassName);
            Object provider = providerClass.getDeclaredConstructor().newInstance();

            Method registerMethod = null;
            for (Method method : registryClass.getMethods()) {
                if ("registerProvider".equals(method.getName()) && method.getParameterCount() == 1) {
                    registerMethod = method;
                    break;
                }
            }
            if (registerMethod == null) {
                if (logger != null) {
                    logger.warning("Registry does not expose registerProvider: " + registryClassName);
                }
                return false;
            }

            registerMethod.invoke(null, provider);
            if (logger != null) {
                logger.fine("Registered Paper provider " + providerClassName + " via " + registryClassName);
            }
            return true;
        } catch (Throwable ex) {
            if (logger != null) {
                logger.fine("Skipping Paper provider registration for " + providerClassName + ": " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
            return false;
        }
    }
}
