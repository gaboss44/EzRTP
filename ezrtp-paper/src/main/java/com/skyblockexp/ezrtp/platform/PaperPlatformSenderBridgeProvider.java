package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PaperPlatformSenderBridgeProvider implements PlatformSenderBridgeProvider {

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).paperApi();
    }

    @Override
    public PlatformSenderBridge create(Plugin plugin) {
        return new PaperPlatformSenderBridge();
    }
}
