package com.skyblockexp.ezrtp.platform;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class PaperPlatformWorldAccess implements PlatformWorldAccess {

    @Override
    public int getSurfaceY(World world, int x, int z) {
        return world == null ? 0 : world.getHighestBlockYAt(x, z);
    }

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

    protected Location scanSnapshotForSafeLocation(
            World world,
            ChunkSnapshot snapshot,
            int x,
            int z,
            int localX,
            int localZ,
            int startY,
            int minY
    ) {
        int searchMinY = Math.max(minY, startY - 20);

        for (int y = startY; y > searchMinY; y--) {
            Material base = snapshot.getBlockType(localX, y, localZ);
            if (base == null || !base.isSolid()) {
                continue;
            }

            Material above = snapshot.getBlockType(localX, y + 1, localZ);
            Material twoAbove = snapshot.getBlockType(localX, y + 2, localZ);
            if (above == null || twoAbove == null || above.isSolid() || twoAbove.isSolid()) {
                continue;
            }

            if (isLiquid(above)) {
                int maxY = Math.max(startY, searchMinY + 20);
                int scanY = y + 1;
                while (scanY <= maxY) {
                    Material inColumn = snapshot.getBlockType(localX, scanY, localZ);
                    if (inColumn == null || !isLiquid(inColumn)) {
                        break;
                    }
                    scanY++;
                }

                if (scanY > maxY) {
                    continue;
                }

                Material surface = snapshot.getBlockType(localX, scanY, localZ);
                Material surfaceAbove = snapshot.getBlockType(localX, scanY + 1, localZ);
                if (surface != null && surfaceAbove != null && !surface.isSolid() && !surfaceAbove.isSolid()) {
                    return new Location(world, x + 0.5, scanY, z + 0.5);
                }
                continue;
            }

            return new Location(world, x + 0.5, y + 1.0, z + 0.5);
        }

        return null;
    }

    protected static boolean isLiquid(Material material) {
        if (material == null) {
            return false;
        }
        String name = material.name();
        return name.contains("WATER") || name.contains("LAVA");
    }
}
