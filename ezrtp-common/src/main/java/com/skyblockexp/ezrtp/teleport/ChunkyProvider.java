package com.skyblockexp.ezrtp.teleport;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Non-reflective interface for Chunky integration used by the plugin.
 * Implementations may use reflection internally, but callers should depend
 * only on this interface so the rest of the codebase remains clean.
 */
public interface ChunkyProvider {
    boolean isRunning(String worldName);

    boolean startTask(String worldName, String shape, double centerX, double centerZ, int radiusX, int radiusZ, String pattern);

    default void registerListeners(JavaPlugin plugin) {
        // Optional: implementations can register listeners; default no-op
    }
}
