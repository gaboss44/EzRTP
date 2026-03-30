package com.skyblockexp.ezrtp.platform;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Registry for the currently active platform runtime.
 */
public final class PlatformRuntimeRegistry {

    private static final PlatformRuntime DEFAULT_RUNTIME = new DefaultPlatformRuntime();
    private static final AtomicReference<PlatformRuntime> RUNTIME = new AtomicReference<>(DEFAULT_RUNTIME);
    private static final List<PlatformRuntimeProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private PlatformRuntimeRegistry() {}

    public static void register(PlatformRuntime runtime) {
        RUNTIME.set(runtime != null ? runtime : DEFAULT_RUNTIME);
    }

    public static void unregister() {
        RUNTIME.set(DEFAULT_RUNTIME);
    }

    public static PlatformRuntime get() {
        PlatformRuntime runtime = RUNTIME.get();
        return runtime != null ? runtime : DEFAULT_RUNTIME;
    }

    public static void registerProvider(PlatformRuntimeProvider provider) {
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
        for (PlatformRuntimeProvider provider : PROVIDERS) {
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
        PlatformRuntimeProvider selected = null;
        for (PlatformRuntimeProvider provider : PROVIDERS) {
            if (!provider.supports(plugin)) {
                continue;
            }
            if (selected == null || provider.priority() > selected.priority()) {
                selected = provider;
            }
        }

        if (selected == null) {
            register(DEFAULT_RUNTIME);
            return false;
        }

        PlatformRuntime runtime = selected.create(plugin);
        if (runtime == null) {
            register(DEFAULT_RUNTIME);
            return false;
        }

        register(runtime);
        if (logger != null) {
            logger.fine("Registered PlatformRuntime provider: " + selected.getClass().getName());
        }
        return true;
    }

    private static final class DefaultPlatformRuntime implements PlatformRuntime {
        private static final PlatformWorldAccess WORLD_ACCESS = new DefaultPlatformWorldAccess();
        private static final PlatformRuntimeCapabilities CAPABILITIES = PlatformRuntimeCapabilities.BUKKIT;
        private static final PlatformScheduler SCHEDULER = new DefaultPlatformScheduler();

        @Override
        public PlatformRuntimeCapabilities capabilities() {
            return CAPABILITIES;
        }

        @Override
        public PlatformWorldAccess worldAccess() {
            return WORLD_ACCESS;
        }

        @Override
        public PlatformScheduler scheduler() {
            return SCHEDULER;
        }
    }

    private static final class DefaultPlatformScheduler implements PlatformScheduler {

        @Override
        public void executeAsync(Runnable task) {
            CompletableFuture.runAsync(task);
        }

        @Override
        public void executeRegion(World world, int chunkX, int chunkZ, Runnable task) {
            task.run();
        }

        @Override
        public PlatformTask scheduleRepeating(Runnable task, long delayTicks, long periodTicks) {
            return () -> { };
        }
    }

    private static final class DefaultPlatformWorldAccess implements PlatformWorldAccess {
        @Override
        public int getSurfaceY(World world, int x, int z) {
            return world == null ? 0 : world.getHighestBlockYAt(x, z);
        }

        @Override
        public org.bukkit.Location trySnapshotValidate(World world, int x, int z, int startY, int minY) {
            return null;
        }
    }
}
