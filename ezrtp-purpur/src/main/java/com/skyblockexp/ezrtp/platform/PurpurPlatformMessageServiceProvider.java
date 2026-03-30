package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PurpurPlatformMessageServiceProvider implements PlatformMessageServiceProvider {

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public boolean supports(Plugin plugin) {
        return PlatformRuntimeCapabilitiesDetector.detect(plugin).purpurApi();
    }

    @Override
    public PlatformMessageService create(Plugin plugin) {
        return new PurpurPlatformMessageService(plugin);
    }
}
