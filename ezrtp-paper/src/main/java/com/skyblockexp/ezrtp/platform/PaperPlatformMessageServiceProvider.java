package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PaperPlatformMessageServiceProvider implements PlatformMessageServiceProvider {

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).isStrictPaper();
    }

    @Override
    public PlatformMessageService create(Plugin plugin) {
        return new PaperPlatformMessageService(plugin);
    }
}
