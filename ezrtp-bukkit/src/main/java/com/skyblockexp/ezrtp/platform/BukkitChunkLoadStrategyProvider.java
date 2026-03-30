package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class BukkitChunkLoadStrategyProvider implements ChunkLoadStrategyProvider {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return true;
    }

    @Override
    public ChunkLoadStrategy create(Plugin plugin) {
        return new BukkitChunkLoadStrategy();
    }
}
