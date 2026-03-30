package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class BukkitPlatformRuntimeProvider implements PlatformRuntimeProvider {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return true;
    }

    @Override
    public PlatformRuntime create(Plugin plugin) {
        return new BukkitPlatformRuntime(PlatformRuntimeCapabilitiesDetector.detect(plugin), plugin);
    }
}
