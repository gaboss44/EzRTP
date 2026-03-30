package com.skyblockexp.ezrtp.platform;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

/**
 * Platform-specific strategy for loading chunks.
 */
public interface ChunkLoadStrategy {

    /**
     * Loads or retrieves a chunk for the supplied world and coordinates.
     */
    CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ);
}
