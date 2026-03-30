package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

/**
 * SPI entry-point used by platform jars to supply a {@link PlatformRuntime}.
 */
public interface PlatformRuntimeProvider {

    int priority();

    boolean supports(Plugin plugin);

    PlatformRuntime create(Plugin plugin);
}
