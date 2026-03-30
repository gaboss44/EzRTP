package com.skyblockexp.ezrtp.teleport.biome;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for search strategies.
 */
class BiomeSearchStrategyTest {
    
    @Mock
    private World mockWorld;
    
    @Mock
    private RareBiomeRegistry mockRegistry;
    
    private UniformSearchStrategy uniformStrategy;
    private WeightedRareBiomeStrategy weightedStrategy;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        uniformStrategy = new UniformSearchStrategy();
        weightedStrategy = new WeightedRareBiomeStrategy();
    }
    
    @Test
    void testUniformSearchGeneratesValidCoordinates() {
        int centerX = 0;
        int centerZ = 0;
        int minRadius = 100;
        int maxRadius = 1000;
        Set<Biome> targetBiomes = new HashSet<>();
        
        int[] coords = uniformStrategy.generateCandidateCoordinates(
            mockWorld, centerX, centerZ, minRadius, maxRadius,
            targetBiomes, mockRegistry
        );
        
        assertNotNull(coords);
        assertEquals(2, coords.length);
        
        int x = coords[0];
        int z = coords[1];
        double distance = Math.sqrt(x * x + z * z);
        
        assertTrue(distance >= minRadius, "Distance should be >= minRadius");
        assertTrue(distance <= maxRadius, "Distance should be <= maxRadius");
    }
    
    @Test
    void testUniformSearchIsAlwaysApplicable() {
        assertTrue(uniformStrategy.isApplicableFor(new HashSet<>(), mockRegistry));
        assertTrue(uniformStrategy.isApplicableFor(null, mockRegistry));
    }
    
    @Test
    void testWeightedSearchWithNoRareBiomesFallsBackToUniform() {
        Set<Biome> commonBiomes = new HashSet<>();
        try {
            commonBiomes.add(Biome.valueOf("PLAINS"));
        } catch (IllegalArgumentException e) {
            return; // Skip if biome not available
        }
        
        when(mockRegistry.isRareBiome(any())).thenReturn(false);
        when(mockRegistry.getHotspots(any(), any())).thenReturn(java.util.Collections.emptyList());
        
        int[] coords = weightedStrategy.generateCandidateCoordinates(
            mockWorld, 0, 0, 100, 1000, commonBiomes, mockRegistry
        );
        
        assertNotNull(coords);
        assertEquals(2, coords.length);
    }
    
    @Test
    void testWeightedSearchIsApplicableForRareBiomes() {
        Set<Biome> rareBiomes = new HashSet<>();
        try {
            rareBiomes.add(Biome.valueOf("MUSHROOM_FIELDS"));
        } catch (IllegalArgumentException e) {
            return; // Skip if biome not available
        }
        
        when(mockRegistry.isRareBiome(any())).thenReturn(true);
        
        assertTrue(weightedStrategy.isApplicableFor(rareBiomes, mockRegistry));
    }
    
    @Test
    void testWeightedSearchNotApplicableForCommonBiomes() {
        Set<Biome> commonBiomes = new HashSet<>();
        try {
            commonBiomes.add(Biome.valueOf("PLAINS"));
        } catch (IllegalArgumentException e) {
            return; // Skip if biome not available
        }
        
        when(mockRegistry.isRareBiome(any())).thenReturn(false);
        
        assertFalse(weightedStrategy.isApplicableFor(commonBiomes, mockRegistry));
    }
}
