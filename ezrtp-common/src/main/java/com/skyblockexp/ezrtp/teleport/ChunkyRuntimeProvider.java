package com.skyblockexp.ezrtp.teleport;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Runtime loader/adapter that wraps the Chunky API implementation (if present)
 * and exposes the `ChunkyProvider` interface. Reflection is localized here.
 */
public final class ChunkyRuntimeProvider implements ChunkyProvider {
    private final Object impl;

    private ChunkyRuntimeProvider(Object impl) {
        this.impl = impl;
    }

    public static ChunkyProvider loadProvider() {
        try {
            Class<?> chunkyClass = Class.forName("org.popcraft.chunky.api.ChunkyAPI");
            Object svc = Bukkit.getServicesManager().load(chunkyClass);
            if (svc == null) return null;
            return new ChunkyRuntimeProvider(svc);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public boolean isRunning(String worldName) {
        if (impl == null) return false;
        try {
            var m = impl.getClass().getMethod("isRunning", String.class);
            Object r = m.invoke(impl, worldName);
            return r instanceof Boolean ? (Boolean) r : false;
        } catch (Throwable ignored) { return false; }
    }

    @Override
    public boolean startTask(String worldName, String shape, double centerX, double centerZ, int radiusX, int radiusZ, String pattern) {
        if (impl == null) return false;
        try {
            for (var m : impl.getClass().getMethods()) {
                if (m.getName().equals("startTask") && m.getParameterCount() == 7) {
                    Object res = m.invoke(impl, worldName, shape, centerX, centerZ, radiusX, radiusZ, pattern);
                    if (res instanceof Boolean) return (Boolean) res;
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public void registerListeners(JavaPlugin plugin) {
        if (impl == null || plugin == null) return;
        try {
            try {
                var m = impl.getClass().getMethod("onGenerationComplete", java.util.function.Consumer.class);
                m.invoke(impl, (java.util.function.Consumer<Object>) (event -> plugin.getLogger().info("[EzRTP] Chunky generation completed")));
            } catch (Throwable ignored) {}
            try {
                var m2 = impl.getClass().getMethod("onGenerationProgress", java.util.function.Consumer.class);
                m2.invoke(impl, (java.util.function.Consumer<Object>) (event -> {}));
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }
}
