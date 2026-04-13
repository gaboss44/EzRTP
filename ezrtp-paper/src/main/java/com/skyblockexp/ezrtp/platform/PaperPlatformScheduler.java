package com.skyblockexp.ezrtp.platform;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class PaperPlatformScheduler implements PlatformScheduler {

    private final Plugin plugin;
    private final PlatformRuntimeCapabilities capabilities;

    public PaperPlatformScheduler(Plugin plugin, PlatformRuntimeCapabilities capabilities) {
        this.plugin = plugin;
        this.capabilities = capabilities;
    }

    @Override
    public void executeAsync(Runnable task) {
        if (capabilities.regionizedRuntime() && invokeFoliaAsync(task)) {
            return;
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void executeRegion(World world, int chunkX, int chunkZ, Runnable task) {
        if (capabilities.regionizedRuntime() && world != null && invokeRegionTask(world, chunkX, chunkZ, task)) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public PlatformTask scheduleRepeating(Runnable task, long delayTicks, long periodTicks) {
        if (capabilities.regionizedRuntime()) {
            PlatformTask foliaTask = scheduleGlobalRepeating(task, delayTicks, periodTicks);
            if (foliaTask != null) {
                return foliaTask;
            }
        }

        org.bukkit.scheduler.BukkitTask bukkitTask = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, task, delayTicks, periodTicks);
        return bukkitTask::cancel;
    }

    private boolean invokeFoliaAsync(Runnable task) {
        try {
            Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            Method runNow = asyncScheduler.getClass().getMethod("runNow", Plugin.class, java.util.function.Consumer.class);
            runNow.invoke(asyncScheduler, plugin, (java.util.function.Consumer<Object>) scheduledTask -> task.run());
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private boolean invokeRegionTask(World world, int chunkX, int chunkZ, Runnable task) {
        try {
            Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Method run = regionScheduler.getClass().getMethod(
                    "run", Plugin.class, World.class, int.class, int.class,
                    java.util.function.Consumer.class);
            run.invoke(regionScheduler, plugin, world, chunkX, chunkZ,
                    (java.util.function.Consumer<Object>) scheduledTask -> task.run());
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @Override
    public void executeGlobal(Runnable task) {
        if (capabilities.regionizedRuntime() && invokeGlobalRun(task)) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public PlatformTask executeGlobalDelayed(Runnable task, long delayTicks) {
        if (capabilities.regionizedRuntime()) {
            PlatformTask foliaTask = invokeGlobalRunDelayed(task, delayTicks);
            if (foliaTask != null) {
                return foliaTask;
            }
        }
        org.bukkit.scheduler.BukkitTask bukkit =
                plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        return bukkit::cancel;
    }

    @Override
    public void executeRegionDelayed(
            World world, int chunkX, int chunkZ, Runnable task, long delayTicks) {
        if (capabilities.regionizedRuntime()
                && world != null
                && invokeRegionDelayed(world, chunkX, chunkZ, task, delayTicks)) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    private boolean invokeGlobalRun(Runnable task) {
        try {
            Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method run = globalScheduler.getClass().getMethod(
                    "run", Plugin.class, java.util.function.Consumer.class);
            run.invoke(globalScheduler, plugin,
                    (java.util.function.Consumer<Object>) ignored -> task.run());
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private PlatformTask invokeGlobalRunDelayed(Runnable task, long delayTicks) {
        try {
            Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method runDelayed = globalScheduler.getClass().getMethod(
                    "runDelayed", Plugin.class, java.util.function.Consumer.class, long.class);
            Object scheduledTask = runDelayed.invoke(globalScheduler, plugin,
                    (java.util.function.Consumer<Object>) ignored -> task.run(), delayTicks);
            Method cancel = scheduledTask.getClass().getMethod("cancel");
            return () -> {
                try {
                    cancel.invoke(scheduledTask);
                } catch (ReflectiveOperationException ignored) {
                }
            };
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private boolean invokeRegionDelayed(
            World world, int chunkX, int chunkZ, Runnable task, long delayTicks) {
        try {
            Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            Method runDelayed = regionScheduler.getClass().getMethod(
                    "runDelayed", Plugin.class, World.class, int.class, int.class,
                    java.util.function.Consumer.class, long.class);
            runDelayed.invoke(regionScheduler, plugin, world, chunkX, chunkZ,
                    (java.util.function.Consumer<Object>) ignored -> task.run(), delayTicks);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private PlatformTask scheduleGlobalRepeating(Runnable task, long delayTicks, long periodTicks) {
        try {
            Object globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            Method runAtFixedRate = globalRegionScheduler.getClass().getMethod(
                    "runAtFixedRate",
                    Plugin.class,
                    java.util.function.Consumer.class,
                    long.class,
                    long.class
            );
            Object scheduledTask = runAtFixedRate.invoke(globalRegionScheduler, plugin,
                    (java.util.function.Consumer<Object>) ignored -> task.run(),
                    delayTicks,
                    periodTicks);
            Method cancel = scheduledTask.getClass().getMethod("cancel");
            return () -> {
                try {
                    cancel.invoke(scheduledTask);
                } catch (ReflectiveOperationException ignored) {
                }
            };
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
