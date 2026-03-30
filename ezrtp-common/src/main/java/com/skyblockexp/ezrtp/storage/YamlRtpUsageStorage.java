package com.skyblockexp.ezrtp.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlRtpUsageStorage implements RtpUsageStorage {
    private final File file;
    private YamlConfiguration config;

    public YamlRtpUsageStorage(File file) {
        this.file = file;
        reload();
    }

    @Override
    public long getLastRtpTime(UUID player, String world) {
        return config.getLong("players." + player + "." + world + ".lastRtp", 0);
    }

    @Override
    public void setLastRtpTime(UUID player, String world, long time) {
        config.set("players." + player + "." + world + ".lastRtp", time);
    }

    @Override
    public int getUsageCount(UUID player, String world, String period) {
        return config.getInt("players." + player + "." + world + ".usage." + period, 0);
    }

    @Override
    public void incrementUsage(UUID player, String world, String period) {
        String path = "players." + player + "." + world + ".usage." + period;
        int count = config.getInt(path, 0);
        config.set(path, count + 1);
    }

    @Override
    public void resetUsage(UUID player, String world, String period) {
        if (player == null && world == null) {
            // Reset all
            if (config.isConfigurationSection("players")) {
                for (String p : config.getConfigurationSection("players").getKeys(false)) {
                    for (String w : config.getConfigurationSection("players." + p).getKeys(false)) {
                        config.set("players." + p + "." + w + ".usage." + period, 0);
                    }
                }
            }
        } else if (player != null && world == null) {
            // Reset all worlds for player
            if (config.isConfigurationSection("players." + player)) {
                for (String w : config.getConfigurationSection("players." + player).getKeys(false)) {
                    config.set("players." + player + "." + w + ".usage." + period, 0);
                }
            }
        } else if (player == null && world != null) {
            // Reset all players for world
            if (config.isConfigurationSection("players")) {
                for (String p : config.getConfigurationSection("players").getKeys(false)) {
                    config.set("players." + p + "." + world + ".usage." + period, 0);
                }
            }
        } else {
            // Reset specific
            config.set("players." + player + "." + world + ".usage." + period, 0);
        }
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[EzRTP] Failed to save RTP usage YAML: " + e.getMessage());
        }
    }

    @Override
    public void reload() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[EzRTP] Failed to create RTP usage YAML: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }
}