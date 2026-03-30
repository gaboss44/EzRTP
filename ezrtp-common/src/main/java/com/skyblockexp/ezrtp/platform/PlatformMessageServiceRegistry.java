package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Simple registry for a single PlatformMessageService implementation.
 * Platform modules register provider implementations using Java SPI.
 */
public final class PlatformMessageServiceRegistry {

    private static final AtomicReference<PlatformMessageService> SERVICE = new AtomicReference<>();
    private static final List<PlatformMessageServiceProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private PlatformMessageServiceRegistry() {}

    public static void register(PlatformMessageService service) {
        SERVICE.set(service);
    }

    public static void unregister() {
        SERVICE.set(null);
    }

    public static PlatformMessageService get() {
        return SERVICE.get();
    }

    public static void registerProvider(PlatformMessageServiceProvider provider) {
        if (provider == null) {
            return;
        }
        if (hasProviderClass(provider.getClass())) {
            return;
        }
        PROVIDERS.add(provider);
    }

    static boolean hasProviderClass(Class<?> providerClass) {
        if (providerClass == null) {
            return false;
        }
        for (PlatformMessageServiceProvider provider : PROVIDERS) {
            if (provider.getClass().equals(providerClass)) {
                return true;
            }
        }
        return false;
    }

    static int providerCount() {
        return PROVIDERS.size();
    }

    public static void clearProviders() {
        PROVIDERS.clear();
    }

    public static boolean loadAndRegister(Plugin plugin, Logger logger) {
        PlatformMessageServiceProvider selected = null;
        for (PlatformMessageServiceProvider provider : PROVIDERS) {
            if (!provider.supports(plugin)) {
                continue;
            }
            if (selected == null || provider.priority() > selected.priority()) {
                selected = provider;
            }
        }

        if (selected == null) {
            return false;
        }

        PlatformMessageService service = selected.create(plugin);
        if (service == null) {
            return false;
        }

        register(service);
        if (logger != null) {
            logger.fine("Registered PlatformMessageService provider: " + selected.getClass().getName());
        }
        return true;
    }

    public static void closeAndUnregister() {
        PlatformMessageService service = SERVICE.getAndSet(null);
        if (service != null) {
            service.close();
        }
    }
}
