package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PurpurPlatformSenderBridgeProvider implements PlatformSenderBridgeProvider {

    @Override
    public int priority() {
        return 110;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).purpurApi();
    }

    @Override
    public PlatformSenderBridge create(Plugin plugin) {
        return new PurpurPlatformSenderBridge();
    }
}
