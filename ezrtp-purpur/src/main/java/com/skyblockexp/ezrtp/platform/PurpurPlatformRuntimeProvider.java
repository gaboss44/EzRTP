package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PurpurPlatformRuntimeProvider extends PaperPlatformRuntimeProvider {

    @Override
    public int priority() {
        return 110;
    }

    @Override
    public PlatformRuntime create(Plugin plugin) {
        return new PurpurPlatformRuntime(PlatformRuntimeCapabilitiesDetector.detect(plugin), plugin);
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).purpurApi();
    }
}
