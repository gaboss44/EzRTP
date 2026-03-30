package com.skyblockexp.ezrtp.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Writes debug diagnostics to a dedicated file under the plugin data folder.
 */
public final class DebugFileLogger {

    private static final String LOG_DIRECTORY = "logs";
    private static final String LOG_FILE = "search-debug.log";
    private final JavaPlugin plugin;
    private final Object lock = new Object();

    public DebugFileLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void log(String message) {
        if (plugin == null || message == null || message.isBlank()) {
            return;
        }
        synchronized (lock) {
            try {
                Path folder = plugin.getDataFolder().toPath().resolve(LOG_DIRECTORY);
                Files.createDirectories(folder);
                Path file = folder.resolve(LOG_FILE);
                String payload = "[" + Instant.now() + "] " + message + System.lineSeparator();
                Files.writeString(file, payload, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            } catch (IOException exception) {
                plugin.getLogger().fine("Failed to write debug log file: " + exception.getMessage());
            }
        }
    }
}
