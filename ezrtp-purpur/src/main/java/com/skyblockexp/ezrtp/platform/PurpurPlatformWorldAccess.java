package com.skyblockexp.ezrtp.platform;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

public final class PurpurPlatformWorldAccess extends PaperPlatformWorldAccess {

    @Override
    public Location trySnapshotValidate(World world, int x, int z, int startY, int minY) {
        if (world == null) {
            return null;
        }

        try {
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            // Snapshot validation is loaded-chunk only; unloaded chunks are handled by normal load strategies.
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                return null;
            }
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            if (chunk == null) {
                return null;
            }

            ChunkSnapshot snapshot = chunk.getChunkSnapshot();
            if (snapshot == null) {
                return null;
            }

            int localX = x & 15;
            int localZ = z & 15;
            return scanSnapshotForSafeLocation(world, snapshot, x, z, localX, localZ, startY, minY);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
