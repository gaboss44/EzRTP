package com.skyblockexp.ezrtp.teleport.filter;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import org.bukkit.Location;

/**
 * A single validation step for candidate teleport locations.
 */
@FunctionalInterface
public interface SearchFilter {
    SearchFilterResult apply(Location candidate, RandomTeleportSettings settings);
}
