package com.skyblockexp.ezrtp.platform;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public final class BukkitPlatformScheduler implements PlatformScheduler {

    private final Plugin plugin;

    public BukkitPlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void executeAsync(Runnable task) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void executeRegion(World world, int chunkX, int chunkZ, Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public PlatformTask scheduleRepeating(Runnable task, long delayTicks, long periodTicks) {
        org.bukkit.scheduler.BukkitTask bukkitTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }
}
