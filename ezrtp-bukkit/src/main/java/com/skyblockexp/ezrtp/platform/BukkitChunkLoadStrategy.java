package com.skyblockexp.ezrtp.platform;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public final class BukkitChunkLoadStrategy implements ChunkLoadStrategy {

    @Override
    public CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ) {
        world.loadChunk(chunkX, chunkZ);
        return CompletableFuture.completedFuture(world.getChunkAt(chunkX, chunkZ));
    }
}
