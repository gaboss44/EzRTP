package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PurpurPlatformGuiBridgeProvider implements PlatformGuiBridgeProvider {

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).purpurApi();
    }

    @Override
    public PlatformGuiBridge create(Plugin plugin) {
        return new PurpurPlatformGuiBridge();
    }
}
