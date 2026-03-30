package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class BukkitPlatformMessageServiceProvider implements PlatformMessageServiceProvider {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return true;
    }

    @Override
    public PlatformMessageService create(Plugin plugin) {
        return new BukkitPlatformMessageService(plugin);
    }
}
