package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

/**
 * SPI entry-point used by platform jars to supply a {@link ChunkLoadStrategy}.
 */
public interface ChunkLoadStrategyProvider {

    /**
     * Higher priority wins when multiple providers support the current server.
     */
    int priority();

    /**
     * Returns true when this provider should be used on the current runtime.
     */
    boolean supports(Plugin plugin);

    /**
     * Creates the strategy instance for the current platform.
     */
    ChunkLoadStrategy create(Plugin plugin);
}
