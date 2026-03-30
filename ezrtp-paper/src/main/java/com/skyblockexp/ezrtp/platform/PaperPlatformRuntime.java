package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

public final class PaperPlatformRuntime implements PlatformRuntime {

    private static final PlatformWorldAccess WORLD_ACCESS = new PaperPlatformWorldAccess();
    private final PlatformRuntimeCapabilities capabilities;
    private final PlatformScheduler scheduler;

    public PaperPlatformRuntime(PlatformRuntimeCapabilities capabilities, Plugin plugin) {
        this.capabilities = capabilities;
        this.scheduler = new PaperPlatformScheduler(plugin, capabilities);
    }

    @Override
    public PlatformRuntimeCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public PlatformWorldAccess worldAccess() {
        return WORLD_ACCESS;
    }

    @Override
    public PlatformScheduler scheduler() {
        return scheduler;
    }
}
