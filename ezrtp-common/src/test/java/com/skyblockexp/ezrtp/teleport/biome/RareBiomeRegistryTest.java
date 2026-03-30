package com.skyblockexp.ezrtp.teleport.biome;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RareBiomeRegistry.
 */
class RareBiomeRegistryTest {
    
    @Mock
    private JavaPlugin mockPlugin;
    
    @Mock
    private World mockWorld;
    
    @Mock
    private Location mockLocation;
    
    @Mock
    private org.bukkit.block.Block mockBlock;
    
    private RareBiomeRegistry registry;
    private Set<Biome> rareBiomes;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        rareBiomes = new HashSet<>();
        try {
            rareBiomes.add(Biome.valueOf("MUSHROOM_FIELDS"));
            rareBiomes.add(Biome.valueOf("JUNGLE"));
        } catch (IllegalArgumentException e) {
            // Biomes might not exist in all versions
        }
        
        registry = new RareBiomeRegistry(mockPlugin, rareBiomes);
        registry.setBackgroundScanningEnabled(false); // Disable for testing
    }
    
    @Test
    void testIsRareBiome() {
        if (rareBiomes.isEmpty()) {
            return; // Skip test if biomes aren't available
        }
        
        assertTrue(registry.isRareBiome(Biome.valueOf("MUSHROOM_FIELDS")));
        assertTrue(registry.isRareBiome(Biome.valueOf("JUNGLE")));
        assertFalse(registry.isRareBiome(Biome.valueOf("PLAINS")));
    }
    
    @Test
    void testRegisterHotspot() {
        if (rareBiomes.isEmpty()) {
            return; // Skip test if biomes aren't available
        }
        
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getName()).thenReturn("world");
        when(mockLocation.getBlock()).thenReturn(mockBlock);
        when(mockBlock.getBiome()).thenReturn(Biome.valueOf("MUSHROOM_FIELDS"));
        when(mockLocation.clone()).thenReturn(mockLocation);
        
        registry.registerHotspot(mockLocation);
        
        RareBiomeRegistry.RegistryStats stats = registry.getStats();
        assertEquals(1, stats.hotspotCount());
        assertTrue(stats.hotspotCount() > 0);
    }
    
    @Test
    void testGetHotspots() {
        if (rareBiomes.isEmpty()) {
            return; // Skip test if biomes aren't available
        }
        
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getName()).thenReturn("world");
        when(mockLocation.getBlock()).thenReturn(mockBlock);
        when(mockBlock.getBiome()).thenReturn(Biome.valueOf("MUSHROOM_FIELDS"));
        when(mockLocation.clone()).thenReturn(mockLocation);
        when(mockLocation.distanceSquared(any())).thenReturn(100000.0); // Far enough apart
        
        registry.registerHotspot(mockLocation);
        
        List<Location> hotspots = registry.getHotspots(mockWorld, Biome.valueOf("MUSHROOM_FIELDS"));
        assertFalse(hotspots.isEmpty());
    }
    
    @Test
    void testDisabled() {
        registry.setEnabled(false);
        
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getName()).thenReturn("world");
        when(mockLocation.getBlock()).thenReturn(mockBlock);
        when(mockBlock.getBiome()).thenReturn(Biome.valueOf("MUSHROOM_FIELDS"));
        
        registry.registerHotspot(mockLocation);
        
        RareBiomeRegistry.RegistryStats stats = registry.getStats();
        assertEquals(0, stats.hotspotCount());
    }
    
    @Test
    void testClear() {
        if (rareBiomes.isEmpty()) {
            return; // Skip test if biomes aren't available
        }
        
        when(mockLocation.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getName()).thenReturn("world");
        when(mockLocation.getBlock()).thenReturn(mockBlock);
        when(mockBlock.getBiome()).thenReturn(Biome.valueOf("MUSHROOM_FIELDS"));
        when(mockLocation.clone()).thenReturn(mockLocation);
        
        registry.registerHotspot(mockLocation);
        assertEquals(1, registry.getStats().hotspotCount());
        
        registry.clear();
        assertEquals(0, registry.getStats().hotspotCount());
    }
}
