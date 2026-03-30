package com.skyblockexp.ezrtp.teleport.queue;

import com.skyblockexp.ezrtp.platform.ChunkLoadStrategy;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformTask;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages a queue of chunk loading operations to spread the load across multiple ticks.
 * This helps reduce server strain from RTP-related chunk loading by processing
 * a limited number of chunk loads per tick.
 */
public final class ChunkLoadQueue {

    private static final long DEFAULT_PROCESSING_INTERVAL_TICKS = 1L; // Process every tick
    private static final int DEFAULT_MAX_CHUNKS_PER_TICK = 1; // Maximum chunks to load per tick

    private final JavaPlugin plugin;
    private final ChunkLoadStrategy chunkLoadStrategy;
    private final PlatformScheduler scheduler;
    private final Queue<ChunkLoadRequest> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private PlatformTask processingTask;
    private final AtomicBoolean processingTaskCancelled = new AtomicBoolean(false);
    private volatile long processingIntervalTicks = DEFAULT_PROCESSING_INTERVAL_TICKS;
    private volatile int maxChunksPerTick = DEFAULT_MAX_CHUNKS_PER_TICK;
    private volatile boolean enabled;
    private final AtomicBoolean schedulerFallbackLogged = new AtomicBoolean(false);
    private volatile long minFreeMemoryMb = 256L; // Default 256MB minimum free memory

    public ChunkLoadQueue(JavaPlugin plugin, ChunkLoadStrategy chunkLoadStrategy, PlatformScheduler scheduler) {
        this.plugin = plugin;
        this.chunkLoadStrategy = chunkLoadStrategy;
        this.scheduler = scheduler;
        this.enabled = true;
    }

    /**
     * Enables or disables the chunk load queue.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stop();
            queue.clear();
        } else if (processing.get()) {
            // Restart processing if it was running
            startProcessing();
        }
    }

    /**
     * Updates throughput controls for the queue.
     */
    public void configure(long intervalTicks, int maxChunksPerTick) {
        this.processingIntervalTicks = Math.max(1L, intervalTicks);
        this.maxChunksPerTick = Math.max(1, maxChunksPerTick);
        if (processing.get()) {
            startProcessing();
        }
    }

    /**
     * Configures memory-aware chunk loading limits.
     * Reduces chunk loading rate when memory is low to prevent OOM.
     */
    public void configureMemorySafety(long minFreeMemoryMb) {
        // Store the minimum free memory threshold for memory checks
        this.minFreeMemoryMb = minFreeMemoryMb;
    }

    /**
     * Returns whether the queue is currently enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Requests a chunk to be loaded asynchronously through the queue.
     * Returns a CompletableFuture that completes when the chunk is loaded.
     *
     * @param world The world containing the chunk
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     * @return Future that completes with the loaded chunk
     */
    public CompletableFuture<Chunk> requestChunkLoad(World world, int chunkX, int chunkZ) {
        if (!enabled) {
            // If disabled, load immediately without queueing
            CompletableFuture<Chunk> future = new CompletableFuture<>();
            scheduler.executeRegion(world, chunkX, chunkZ, () -> {
                world.loadChunk(chunkX, chunkZ);
                future.complete(world.getChunkAt(chunkX, chunkZ));
            });
            return future;
        }

        CompletableFuture<Chunk> future = new CompletableFuture<>();
        ChunkLoadRequest request = new ChunkLoadRequest(world, chunkX, chunkZ, future);
        queue.offer(request);

        // Start processing if not already running
        if (!processing.getAndSet(true)) {
            startProcessing();
        }

        return future;
    }

    /**
     * Returns the current queue size.
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Starts processing the chunk load queue.
     */
    private void startProcessing() {
        synchronized (this) {
            // If a running task exists and is not marked cancelled, leave it running
            if (processingTask != null && !processingTaskCancelled.get()) {
                return;
            }

            // Cancel any existing scheduled task (best-effort) and mark it cancelled locally
            if (processingTask != null) {
                processingTaskCancelled.set(true);
                try {
                    processingTask.cancel();
                } catch (Exception ignored) {
                    // ignore
                }
                processingTask = null;
            }

            // Reset cancelled flag for the new task
            processingTaskCancelled.set(false);
            try {
                processingTask = scheduler.scheduleRepeating(this::processQueue,
                        processingIntervalTicks,
                        processingIntervalTicks);
            } catch (Exception ex) {
                activateSynchronousFallback("scheduler threw while scheduling repeating task: " + ex.getMessage());
                return;
            }

            if (processingTask == null) {
                activateSynchronousFallback("scheduler returned null for repeating task");
            }
        }
    }

    /**
     * Processes queued chunk loads.
     */
    private void processQueue() {
        // Bail out early if stop() requested or task was cancelled externally
        if (processingTaskCancelled.get()) {
            processing.set(false);
            stop();
            return;
        }

        if (!enabled || queue.isEmpty()) {
            if (queue.isEmpty()) {
                processing.set(false);
                stop();
            }
            return;
        }

        // Check memory and adjust processing rate
        int effectiveMaxChunks = maxChunksPerTick;
        if (minFreeMemoryMb > 0) {
            Runtime runtime = Runtime.getRuntime();
            long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
            if (freeMemoryMb < minFreeMemoryMb) {
                // Reduce chunk loading rate when memory is low (minimum 1 chunk per tick)
                effectiveMaxChunks = Math.max(1, maxChunksPerTick / 2);
            }
        }

        int processed = 0;
        while (processed < effectiveMaxChunks && !queue.isEmpty()) {
            ChunkLoadRequest request = queue.poll();
            if (request != null) {
                processChunkLoad(request);
                processed++;
            }
        }
    }

    /**
     * Processes a single chunk load request.
     */
    private void processChunkLoad(ChunkLoadRequest request) {
        try {
            World world = request.world();
            int chunkX = request.chunkX();
            int chunkZ = request.chunkZ();

            scheduler.executeRegion(world, chunkX, chunkZ, () -> {
                try {
                    // Check if chunk is already loaded
                    if (world.isChunkLoaded(chunkX, chunkZ)) {
                        request.future().complete(world.getChunkAt(chunkX, chunkZ));
                        return;
                    }
                    chunkLoadStrategy.loadChunk(world, chunkX, chunkZ)
                            .whenComplete((chunk, ex) -> {
                                if (ex != null) {
                                    request.future().completeExceptionally(ex);
                                    return;
                                }
                                request.future().complete(chunk);
                            });
                } catch (Exception e) {
                    request.future().completeExceptionally(e);
                    plugin.getLogger().warning("Failed to load chunk: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            request.future().completeExceptionally(e);
            plugin.getLogger().warning("Failed to load chunk: " + e.getMessage());
        }
    }

    /**
     * Stops processing the queue.
     */
    public void stop() {
        processing.set(false);
        if (processingTask != null) {
            processingTaskCancelled.set(true);
            try {
                processingTask.cancel();
            } catch (Exception ignored) {
                // ignore
            }
            processingTask = null;
        }
    }

    /**
     * Clears all pending chunk load requests.
     * Completes all pending futures exceptionally with a cancellation exception.
     */
    public void clear() {
        ChunkLoadRequest request;
        while ((request = queue.poll()) != null) {
            request.future().completeExceptionally(
                    new java.util.concurrent.CancellationException("Chunk load request cancelled")
            );
        }
    }

    /**
     * Shuts down the queue and clears all requests.
     */
    public void shutdown() {
        stop();
        clear();
        enabled = false;
    }

    private void activateSynchronousFallback(String reason) {
        enabled = false;
        processing.set(false);
        if (schedulerFallbackLogged.compareAndSet(false, true)) {
            plugin.getLogger().warning("[EzRTP] Chunk load queue fallback engaged; using immediate synchronous loading. Reason: " + reason);
        }
        ChunkLoadRequest request;
        while ((request = queue.poll()) != null) {
            processChunkLoadImmediate(request);
        }
    }

    private void processChunkLoadImmediate(ChunkLoadRequest request) {
        try {
            World world = request.world();
            int chunkX = request.chunkX();
            int chunkZ = request.chunkZ();
            world.loadChunk(chunkX, chunkZ);
            request.future().complete(world.getChunkAt(chunkX, chunkZ));
        } catch (Exception e) {
            request.future().completeExceptionally(e);
            plugin.getLogger().warning("Failed to synchronously load chunk after queue fallback: " + e.getMessage());
        }
    }

    /**
     * Represents a chunk load request.
     */
    private record ChunkLoadRequest(
            World world,
            int chunkX,
            int chunkZ,
            CompletableFuture<Chunk> future
    ) {}
}
