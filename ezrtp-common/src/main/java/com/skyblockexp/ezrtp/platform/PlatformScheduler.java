package com.skyblockexp.ezrtp.platform;

import org.bukkit.World;

/**
 * Scheduler bridge that can route work to async execution and world-region main-thread execution.
 */
public interface PlatformScheduler {

    void executeAsync(Runnable task);

    void executeRegion(World world, int chunkX, int chunkZ, Runnable task);

    PlatformTask scheduleRepeating(Runnable task, long delayTicks, long periodTicks);
}
