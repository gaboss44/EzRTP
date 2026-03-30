package com.skyblockexp.ezrtp.teleport.queue;

import com.skyblockexp.ezrtp.platform.ChunkLoadStrategy;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChunkLoadQueue.
 */
class ChunkLoadQueueTest {
    
    @Mock
    private JavaPlugin mockPlugin;
    
    @Mock
    private World mockWorld;

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
        // Note: We can't fully test chunk loading without a real server
        // but we can verify the queue clears properly
        queue.clear();
        assertEquals(0, queue.getQueueSize());
    }
    
    @Test
    void testShutdown() {
        queue.shutdown();
        assertFalse(queue.isEnabled());
        assertEquals(0, queue.getQueueSize());
    }
}
