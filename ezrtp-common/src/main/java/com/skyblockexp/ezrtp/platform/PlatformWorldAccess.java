package com.skyblockexp.ezrtp.platform;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * World-level platform hooks used by teleport search.
 */
public interface PlatformWorldAccess {

    int getSurfaceY(World world, int x, int z);

    Location trySnapshotValidate(World world, int x, int z, int startY, int minY);
}
