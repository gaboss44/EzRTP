package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

/**
 * SPI entry-point used by platform jars to supply a PlatformSenderBridge.
 */
public interface PlatformSenderBridgeProvider {

    /**
     * Higher priority wins when multiple providers support the current server.
     */
    int priority();

    /**
     * Returns true when this provider should be used on the current runtime.
     */
    boolean supports(Plugin plugin);

    /**
     * Creates the bridge instance for the current platform.
     */
    PlatformSenderBridge create(Plugin plugin);
}
