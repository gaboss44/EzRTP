package com.skyblockexp.ezrtp.platform;

import org.bukkit.Location;
import org.bukkit.World;

public class BukkitPlatformWorldAccess implements PlatformWorldAccess {

    @Override
    public int getSurfaceY(World world, int x, int z) {
        return world == null ? 0 : world.getHighestBlockYAt(x, z);
    }

    @Override
    public Location trySnapshotValidate(World world, int x, int z, int startY, int minY) {
        return null;
    }
}
