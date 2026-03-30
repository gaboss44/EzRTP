package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PaperChunkLoadStrategyProvider implements ChunkLoadStrategyProvider {

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).isStrictPaper();
    }

    @Override
    public ChunkLoadStrategy create(Plugin plugin) {
        return new PaperChunkLoadStrategy();
    }
}
