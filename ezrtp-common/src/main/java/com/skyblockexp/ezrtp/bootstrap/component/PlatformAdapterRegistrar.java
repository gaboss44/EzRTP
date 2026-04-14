package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.platform.ChunkLoadStrategyProvider;
import com.skyblockexp.ezrtp.platform.ChunkLoadStrategyRegistry;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeProvider;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceProvider;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeProvider;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.platform.PlatformSenderBridgeProvider;
import com.skyblockexp.ezrtp.platform.PlatformSenderBridgeRegistry;

import java.util.logging.Logger;

/**
 * Registers platform adapter providers without relying on Java SPI ServiceLoader lookups.
 */
public final class PlatformAdapterRegistrar {

    private static final String[] BUKKIT_RUNTIME_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.BukkitPlatformRuntimeProvider"
    };
    private static final String[] BUKKIT_CHUNK_STRATEGY_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.BukkitChunkLoadStrategyProvider"
    };
    private static final String[] BUKKIT_MESSAGE_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.BukkitPlatformMessageServiceProvider"
    };
    private static final String[] BUKKIT_SENDER_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.BukkitPlatformSenderBridgeProvider"
    };
    private static final String[] BUKKIT_GUI_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.BukkitPlatformGuiBridgeProvider"
    };
    private static final String[] OPTIONAL_RUNTIME_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.PaperPlatformRuntimeProvider",
            "com.skyblockexp.ezrtp.platform.PurpurPlatformRuntimeProvider"
    };
    private static final String[] OPTIONAL_CHUNK_STRATEGY_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.PaperChunkLoadStrategyProvider",
            "com.skyblockexp.ezrtp.platform.PurpurChunkLoadStrategyProvider"
    };
    private static final String[] OPTIONAL_MESSAGE_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.PaperPlatformMessageServiceProvider",
            "com.skyblockexp.ezrtp.platform.PurpurPlatformMessageServiceProvider"
    };
    private static final String[] OPTIONAL_SENDER_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.PaperPlatformSenderBridgeProvider",
            "com.skyblockexp.ezrtp.platform.PurpurPlatformSenderBridgeProvider"
    };
    private static final String[] OPTIONAL_GUI_PROVIDERS = {
            "com.skyblockexp.ezrtp.platform.PaperPlatformGuiBridgeProvider",
            "com.skyblockexp.ezrtp.platform.PurpurPlatformGuiBridgeProvider"
    };

    private PlatformAdapterRegistrar() {}

    public static void registerKnownProviders(Logger logger) {
        registerKnownProviders(logger, true);
    }

    public static void registerKnownProviders(Logger logger, boolean registerOptionalProviders) {
        ChunkLoadStrategyRegistry.clearProviders();
        PlatformRuntimeRegistry.clearProviders();
        PlatformGuiBridgeRegistry.clearProviders();
        PlatformMessageServiceRegistry.clearProviders();
        PlatformSenderBridgeRegistry.clearProviders();

        registerOptionalProviders(BUKKIT_RUNTIME_PROVIDERS, PlatformRuntimeProvider.class, PlatformRuntimeRegistry::registerProvider, logger);
        registerOptionalProviders(BUKKIT_CHUNK_STRATEGY_PROVIDERS, ChunkLoadStrategyProvider.class, ChunkLoadStrategyRegistry::registerProvider, logger);
        registerOptionalProviders(BUKKIT_MESSAGE_PROVIDERS, PlatformMessageServiceProvider.class, PlatformMessageServiceRegistry::registerProvider, logger);
        registerOptionalProviders(BUKKIT_SENDER_PROVIDERS, PlatformSenderBridgeProvider.class, PlatformSenderBridgeRegistry::registerProvider, logger);
        registerOptionalProviders(BUKKIT_GUI_PROVIDERS, PlatformGuiBridgeProvider.class, PlatformGuiBridgeRegistry::registerProvider, logger);

        if (!registerOptionalProviders) {
            return;
        }

        registerOptionalProviders(OPTIONAL_RUNTIME_PROVIDERS, PlatformRuntimeProvider.class, PlatformRuntimeRegistry::registerProvider, logger);
        registerOptionalProviders(OPTIONAL_CHUNK_STRATEGY_PROVIDERS, ChunkLoadStrategyProvider.class, ChunkLoadStrategyRegistry::registerProvider, logger);
        registerOptionalProviders(OPTIONAL_MESSAGE_PROVIDERS, PlatformMessageServiceProvider.class, PlatformMessageServiceRegistry::registerProvider, logger);
        registerOptionalProviders(OPTIONAL_SENDER_PROVIDERS, PlatformSenderBridgeProvider.class, PlatformSenderBridgeRegistry::registerProvider, logger);
        registerOptionalProviders(OPTIONAL_GUI_PROVIDERS, PlatformGuiBridgeProvider.class, PlatformGuiBridgeRegistry::registerProvider, logger);
    }

    private static <T> void registerOptionalProviders(
            String[] classNames, Class<T> type, java.util.function.Consumer<T> register, Logger logger) {
        for (String className : classNames) {
            try {
                Class<?> providerClass = Class.forName(className);
                Object instance = providerClass.getDeclaredConstructor().newInstance();
                register.accept(type.cast(instance));
                if (logger != null) {
                    logger.fine("Registered optional provider: " + className);
                }
            } catch (ClassNotFoundException ignored) {
                // Optional provider absent in this artifact/classpath.
            } catch (Exception ex) {
                if (logger != null) {
                    logger.warning("Failed to register optional provider " + className + ": " + ex.getMessage());
                }
            }
        }
    }
}
