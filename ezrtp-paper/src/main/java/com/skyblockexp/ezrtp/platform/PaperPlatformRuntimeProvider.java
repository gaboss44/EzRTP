package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public class PaperPlatformRuntimeProvider implements PlatformRuntimeProvider {

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).paperApi();
    }

    @Override
    public PlatformRuntime create(Plugin plugin) {
        return new PaperPlatformRuntime(PlatformRuntimeCapabilitiesDetector.detect(plugin), plugin);
    }
}
