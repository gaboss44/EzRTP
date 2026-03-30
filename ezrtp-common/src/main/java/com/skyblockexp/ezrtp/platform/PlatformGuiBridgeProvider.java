package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

/**
 * SPI entry-point used by platform jars to supply a PlatformGuiBridge.
 */
public interface PlatformGuiBridgeProvider {

    int priority();

    boolean supports(Plugin plugin);

    PlatformGuiBridge create(Plugin plugin);
}
