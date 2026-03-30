package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class BukkitPlatformSenderBridgeProvider implements PlatformSenderBridgeProvider {

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return true;
    }

    @Override
    public PlatformSenderBridge create(Plugin plugin) {
        return new BukkitPlatformSenderBridge();
    }
}
