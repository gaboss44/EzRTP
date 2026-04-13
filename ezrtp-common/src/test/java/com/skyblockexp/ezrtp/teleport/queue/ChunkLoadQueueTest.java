package com.skyblockexp.ezrtp.teleport.queue;

import com.skyblockexp.ezrtp.platform.ChunkLoadStrategy;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChunkLoadQueue.
 */
class ChunkLoadQueueTest {

    @Mock
    private JavaPlugin mockPlugin;

    @Mock
    private World mockWorld;

    @Mock
    private Chunk mockChunk;

    @Mock
    private ChunkLoadStrategy chunkLoadStrategy;

    @Mock
    private PlatformScheduler platformScheduler;

    private ChunkLoadQueue queue;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queue = new ChunkLoadQueue(mockPlugin, chunkLoadStrategy, platformScheduler);
    }

    @Test
    void testQueueInitiallyEmpty() {
        assertEquals(0, queue.getQueueSize());
    }

    @Test
    void testSetEnabled() {
        assertTrue(queue.isEnabled());

        queue.setEnabled(false);
        assertFalse(queue.isEnabled());

        queue.setEnabled(true);
        assertTrue(queue.isEnabled());
    }

    @Test
    void testClear() {
        queue.clear();
        assertEquals(0, queue.getQueueSize());
    }

    @Test
    void testShutdown() {
        queue.shutdown();
        assertFalse(queue.isEnabled());
        assertEquals(0, queue.getQueueSize());
    }

    // ── async-passthrough ─────────────────────────────────────────────────────

    @Test
    void testAsyncPassthroughInitiallyFalse() {
        assertFalse(queue.isAsyncPassthrough());
    }

    @Test
    void testSetAsyncPassthroughTrue() {
        queue.setAsyncPassthrough(true);
        assertTrue(queue.isAsyncPassthrough());
    }

    @Test
    void testSetAsyncPassthroughFalseRestoresFalse() {
        queue.setAsyncPassthrough(true);
        queue.setAsyncPassthrough(false);
        assertFalse(queue.isAsyncPassthrough());
    }

    /**
     * When async-passthrough is active, {@code requestChunkLoad} must NOT enqueue the request —
     * it delegates directly to the strategy via {@code executeRegion}.
     */
    @Test
    void testAsyncPassthroughSkipsQueue() {
        // Arrange: chunk is not loaded; strategy returns a completed future immediately.
        when(mockWorld.isChunkLoaded(anyInt(), anyInt())).thenReturn(false);
        when(chunkLoadStrategy.loadChunk(any(), anyInt(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(mockChunk));

        // Capture the Runnable passed to executeRegion and run it synchronously.
        AtomicReference<Runnable> capturedRunnable = new AtomicReference<>();
        doAnswer(inv -> {
            capturedRunnable.set(inv.getArgument(3));
            return null;
        }).when(platformScheduler).executeRegion(any(), anyInt(), anyInt(), any(Runnable.class));

        queue.setAsyncPassthrough(true);
        CompletableFuture<Chunk> future = queue.requestChunkLoad(mockWorld, 0, 0);

        // Queue must remain empty (nothing enqueued)
        assertEquals(0, queue.getQueueSize(), "async-passthrough must not enqueue the request");

        // executeRegion must have been called once
        verify(platformScheduler, times(1)).executeRegion(any(), anyInt(), anyInt(), any(Runnable.class));

        // Run captured callback to drive the future to completion
        assertNotNull(capturedRunnable.get(), "executeRegion callback should have been captured");
        capturedRunnable.get().run();

        assertTrue(future.isDone(), "future should be completed after executeRegion callback runs");
        assertFalse(future.isCompletedExceptionally(), "future should complete normally");
    }

    /**
     * When async-passthrough is active and the chunk is already loaded, the queue must complete
     * the future immediately with the existing chunk — without calling the strategy.
     */
    @Test
    void testAsyncPassthroughUsesAlreadyLoadedChunk() {
        when(mockWorld.isChunkLoaded(anyInt(), anyInt())).thenReturn(true);
        when(mockWorld.getChunkAt(anyInt(), anyInt())).thenReturn(mockChunk);

        AtomicReference<Runnable> capturedRunnable = new AtomicReference<>();
        doAnswer(inv -> {
            capturedRunnable.set(inv.getArgument(3));
            return null;
        }).when(platformScheduler).executeRegion(any(), anyInt(), anyInt(), any(Runnable.class));

        queue.setAsyncPassthrough(true);
        CompletableFuture<Chunk> future = queue.requestChunkLoad(mockWorld, 0, 0);

        capturedRunnable.get().run();

        assertTrue(future.isDone());
        // Strategy must NOT have been called — chunk was already loaded
        verify(chunkLoadStrategy, never()).loadChunk(any(), anyInt(), anyInt());
    }
}
