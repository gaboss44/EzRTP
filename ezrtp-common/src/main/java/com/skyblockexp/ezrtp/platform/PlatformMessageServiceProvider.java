package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

/**
 * SPI entry-point used by platform jars to supply a PlatformMessageService.
 */
public interface PlatformMessageServiceProvider {

    /**
     * Higher priority wins when multiple providers support the current server.
     */
    int priority();

    /**
     * Returns true when this provider should be used on the current runtime.
     */
    boolean supports(Plugin plugin);

    /**
     * Creates the service instance for the current platform.
     */
    PlatformMessageService create(Plugin plugin);
}
