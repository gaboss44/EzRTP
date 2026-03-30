package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PaperPlatformGuiBridgeProvider implements PlatformGuiBridgeProvider {

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).isStrictPaper();
    }

    @Override
    public PlatformGuiBridge create(Plugin plugin) {
        return new PaperPlatformGuiBridge();
    }
}
