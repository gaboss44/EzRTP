package com.skyblockexp.ezrtp.platform;

import org.bukkit.World;

/**
 * Scheduler bridge that can route work to async execution and world-region main-thread execution.
 *
 * <p>On Folia, {@code executeGlobal} and {@code executeGlobalDelayed} route through the
 * {@code GlobalRegionScheduler}; {@code executeRegion} and {@code executeRegionDelayed} route
 * through the {@code RegionScheduler} for the supplied chunk. On Bukkit/Spigot/Paper these
 * fall back to the standard {@code BukkitScheduler}.
 */
public interface PlatformScheduler {

    void executeAsync(Runnable task);

    void executeRegion(World world, int chunkX, int chunkZ, Runnable task);

    PlatformTask scheduleRepeating(Runnable task, long delayTicks, long periodTicks);

    /** Run {@code task} on the global main thread (Folia: GlobalRegionScheduler). */
    void executeGlobal(Runnable task);

    /**
     * Run {@code task} on the global main thread after {@code delayTicks} ticks.
     *
     * @return a {@link PlatformTask} that can be used to cancel the pending task
     */
    PlatformTask executeGlobalDelayed(Runnable task, long delayTicks);

    /**
     * Run {@code task} on the region-thread owning the given chunk after {@code delayTicks} ticks.
     * On non-regionised platforms the region context is ignored and a plain delayed task is used.
     */
    void executeRegionDelayed(World world, int chunkX, int chunkZ, Runnable task, long delayTicks);
}
