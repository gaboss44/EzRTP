package com.skyblockexp.ezrtp.teleport;

import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Small reflection wrapper that centralizes all interactions with Chunky API.
 * Keeps reflective code in a single place for clarity and maintainability.
 */
public final class ChunkyAdapter {

    private ChunkyAdapter() {}

    public static boolean isRunning(Object chunkyApi, String worldName) {
        if (chunkyApi == null) return false;
        try {
            Method m = chunkyApi.getClass().getMethod("isRunning", String.class);
            Object r = m.invoke(chunkyApi, worldName);
            return r instanceof Boolean ? (Boolean) r : false;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean startTask(Object chunkyApi, String worldName, String shape, double centerX, double centerZ, int radiusX, int radiusZ, String pattern) {
        if (chunkyApi == null) return false;
        try {
            // Try to find a startTask method with 7 params
            for (Method m : chunkyApi.getClass().getMethods()) {
                if (m.getName().equals("startTask") && m.getParameterCount() == 7) {
                    Object res = m.invoke(chunkyApi, worldName, shape, centerX, centerZ, radiusX, radiusZ, pattern);
                    if (res instanceof Boolean) return (Boolean) res;
                    return true;
                }
            }
        } catch (Throwable t) {
            // swallow and return false on failure
        }
        return false;
    }

    public static void registerListeners(Object chunkyApi, JavaPlugin plugin) {
        if (chunkyApi == null || plugin == null) return;
        try {
            try {
                Method onComplete = chunkyApi.getClass().getMethod("onGenerationComplete", java.util.function.Consumer.class);
                onComplete.invoke(chunkyApi, (Consumer<Object>) event -> plugin.getLogger().info("Chunky generation completed"));
            } catch (Throwable ignored) {}
            try {
                Method onProgress = chunkyApi.getClass().getMethod("onGenerationProgress", java.util.function.Consumer.class);
                onProgress.invoke(chunkyApi, (Consumer<Object>) event -> {});
            } catch (Throwable ignored) {}
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to register Chunky listeners: " + t.getMessage());
        }
    }
}
