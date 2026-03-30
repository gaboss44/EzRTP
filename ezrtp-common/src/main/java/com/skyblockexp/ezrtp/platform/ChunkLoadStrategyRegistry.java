package com.skyblockexp.ezrtp.platform;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Registry for the currently active platform chunk loading strategy.
 */
public final class ChunkLoadStrategyRegistry {

    private static final ChunkLoadStrategy DEFAULT_STRATEGY = new SyncFallbackChunkLoadStrategy();
    private static final AtomicReference<ChunkLoadStrategy> STRATEGY = new AtomicReference<>(DEFAULT_STRATEGY);
    private static final List<ChunkLoadStrategyProvider> PROVIDERS = new CopyOnWriteArrayList<>();

    private ChunkLoadStrategyRegistry() {}

    public static void register(ChunkLoadStrategy strategy) {
        STRATEGY.set(strategy != null ? strategy : DEFAULT_STRATEGY);
    }

    public static void unregister() {
        STRATEGY.set(DEFAULT_STRATEGY);
    }

    public static ChunkLoadStrategy get() {
        ChunkLoadStrategy strategy = STRATEGY.get();
        return strategy != null ? strategy : DEFAULT_STRATEGY;
    }

    public static void registerProvider(ChunkLoadStrategyProvider provider) {
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
        for (ChunkLoadStrategyProvider provider : PROVIDERS) {
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
        ChunkLoadStrategyProvider selected = null;
        for (ChunkLoadStrategyProvider provider : PROVIDERS) {
            if (!provider.supports(plugin)) {
                continue;
            }
            if (selected == null || provider.priority() > selected.priority()) {
                selected = provider;
            }
        }

        if (selected == null) {
            register(DEFAULT_STRATEGY);
            return false;
        }

        ChunkLoadStrategy strategy = selected.create(plugin);
        if (strategy == null) {
            register(DEFAULT_STRATEGY);
            return false;
        }

        register(strategy);
        if (logger != null) {
            logger.fine("Registered ChunkLoadStrategy provider: " + selected.getClass().getName());
        }
        return true;
    }

    private static final class SyncFallbackChunkLoadStrategy implements ChunkLoadStrategy {
        @Override
        public CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ) {
            world.loadChunk(chunkX, chunkZ);
            return CompletableFuture.completedFuture(world.getChunkAt(chunkX, chunkZ));
        }
    }
}
