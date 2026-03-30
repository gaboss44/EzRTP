package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class BukkitPlatformGuiBridgeProvider implements PlatformGuiBridgeProvider {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return true;
    }

    @Override
    public PlatformGuiBridge create(Plugin plugin) {
        return new BukkitPlatformGuiBridge();
    }
}
