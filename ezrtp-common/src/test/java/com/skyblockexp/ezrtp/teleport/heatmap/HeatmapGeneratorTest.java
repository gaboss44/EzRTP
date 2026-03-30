package com.skyblockexp.ezrtp.teleport.heatmap;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HeatmapGenerator.
 */
class HeatmapGeneratorTest {

    private World mockWorld;

    @BeforeEach
    void setUp() {
        mockWorld = Mockito.mock(World.class);
    }

    @Test
    void testDefaultGridSize() {
        HeatmapGenerator generator = new HeatmapGenerator();
        assertNotNull(generator);
    }

    @Test
    void testCustomGridSize() {
        HeatmapGenerator generator = new HeatmapGenerator(256);
        assertNotNull(generator);
    }

    @Test
    void testInvalidGridSizeTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> new HeatmapGenerator(15));
    }

    @Test
    void testInvalidGridSizeTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> new HeatmapGenerator(4097));
    }

    @Test
    void testEmptyLocationList() {
        HeatmapGenerator generator = new HeatmapGenerator();
        List<Location> locations = new ArrayList<>();
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(0, heatmap.getTotalLocations());
        assertEquals(0, heatmap.getOccupiedCells());
        assertEquals(0, heatmap.getMaxCount());
        assertEquals(0, heatmap.getMinCount());
        assertEquals(0.0, heatmap.getAverageCount());
    }

    @Test
    void testNullLocationList() {
        HeatmapGenerator generator = new HeatmapGenerator();
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(null);
        
        assertNotNull(heatmap);
        assertEquals(0, heatmap.getTotalLocations());
        assertEquals(0, heatmap.getOccupiedCells());
    }

    @Test
    void testSingleLocation() {
        HeatmapGenerator generator = new HeatmapGenerator(512);
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(mockWorld, 100, 64, 200));
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(512, heatmap.getGridSize());
        assertEquals(1, heatmap.getTotalLocations());
        assertEquals(1, heatmap.getOccupiedCells());
        assertEquals(1, heatmap.getMaxCount());
        assertEquals(1, heatmap.getMinCount());
        assertEquals(1.0, heatmap.getAverageCount());
    }

    @Test
    void testMultipleLocationsInSameCell() {
        HeatmapGenerator generator = new HeatmapGenerator(512);
        List<Location> locations = new ArrayList<>();
        
        // All locations in the same grid cell (0, 0)
        locations.add(new Location(mockWorld, 100, 64, 200));
        locations.add(new Location(mockWorld, 150, 64, 250));
        locations.add(new Location(mockWorld, 200, 64, 300));
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(3, heatmap.getTotalLocations());
        assertEquals(1, heatmap.getOccupiedCells());
        assertEquals(3, heatmap.getMaxCount());
        assertEquals(3, heatmap.getMinCount());
        assertEquals(3.0, heatmap.getAverageCount());
    }

    @Test
    void testMultipleLocationsInDifferentCells() {
        HeatmapGenerator generator = new HeatmapGenerator(512);
        List<Location> locations = new ArrayList<>();
        
        // Locations in different grid cells
        locations.add(new Location(mockWorld, 100, 64, 200));    // Cell (0, 0)
        locations.add(new Location(mockWorld, 600, 64, 200));    // Cell (1, 0)
        locations.add(new Location(mockWorld, 100, 64, 600));    // Cell (0, 1)
        locations.add(new Location(mockWorld, 600, 64, 600));    // Cell (1, 1)
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(4, heatmap.getTotalLocations());
        assertEquals(4, heatmap.getOccupiedCells());
        assertEquals(1, heatmap.getMaxCount());
        assertEquals(1, heatmap.getMinCount());
        assertEquals(1.0, heatmap.getAverageCount());
    }

    @Test
    void testNegativeCoordinates() {
        HeatmapGenerator generator = new HeatmapGenerator(512);
        List<Location> locations = new ArrayList<>();
        
        // Locations with negative coordinates
        locations.add(new Location(mockWorld, -100, 64, -200));  // Cell (-1, -1)
        locations.add(new Location(mockWorld, -600, 64, -200));  // Cell (-2, -1)
        locations.add(new Location(mockWorld, -100, 64, -600));  // Cell (-1, -2)
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(3, heatmap.getTotalLocations());
        assertEquals(3, heatmap.getOccupiedCells());
    }

    @Test
    void testMixedPositiveAndNegativeCoordinates() {
        HeatmapGenerator generator = new HeatmapGenerator(512);
        List<Location> locations = new ArrayList<>();
        
        locations.add(new Location(mockWorld, 100, 64, 200));
        locations.add(new Location(mockWorld, -100, 64, -200));
        locations.add(new Location(mockWorld, 100, 64, -200));
        locations.add(new Location(mockWorld, -100, 64, 200));
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(4, heatmap.getTotalLocations());
        assertEquals(4, heatmap.getOccupiedCells());
    }

    @Test
    void testNullLocationsInList() {
        HeatmapGenerator generator = new HeatmapGenerator(512);
        List<Location> locations = new ArrayList<>();
        
        locations.add(new Location(mockWorld, 100, 64, 200));
        locations.add(null);  // Null location should be skipped
        locations.add(new Location(mockWorld, 600, 64, 200));
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(2, heatmap.getTotalLocations());
        assertEquals(2, heatmap.getOccupiedCells());
    }

    @Test
    void testDifferentGridSizes() {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(mockWorld, 100, 64, 200));
        locations.add(new Location(mockWorld, 600, 64, 200));
        
        // With grid size 512, these should be in different cells
        HeatmapGenerator generator512 = new HeatmapGenerator(512);
        HeatmapGenerator.HeatmapData heatmap512 = generator512.generate(locations);
        assertEquals(2, heatmap512.getOccupiedCells());
        
        // With grid size 1024, these should be in the same cell
        HeatmapGenerator generator1024 = new HeatmapGenerator(1024);
        HeatmapGenerator.HeatmapData heatmap1024 = generator1024.generate(locations);
        assertEquals(1, heatmap1024.getOccupiedCells());
    }

    @Test
    void testLargeNumberOfLocations() {
        HeatmapGenerator generator = new HeatmapGenerator(512);
        List<Location> locations = new ArrayList<>();
        
        // Generate 1000 locations spread across a grid
        for (int i = 0; i < 1000; i++) {
            int x = (i % 10) * 512;  // 10 cells wide
            int z = ((i / 10) % 10) * 512;  // 10 rows (wrap every 10 to create 10x10)
            locations.add(new Location(mockWorld, x, 64, z));
        }
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertNotNull(heatmap);
        assertEquals(1000, heatmap.getTotalLocations());
        assertEquals(100, heatmap.getOccupiedCells()); // 10x10 grid
        assertEquals(10.0, heatmap.getAverageCount());
    }

    @Test
    void testHeatmapDataGetters() {
        HeatmapGenerator generator = new HeatmapGenerator(256);
        List<Location> locations = new ArrayList<>();
        
        // Create a distribution with varying counts
        // Cell (0, 0): 5 locations
        for (int i = 0; i < 5; i++) {
            locations.add(new Location(mockWorld, i * 10, 64, i * 10));
        }
        // Cell (1, 0): 3 locations
        for (int i = 0; i < 3; i++) {
            locations.add(new Location(mockWorld, 256 + i * 10, 64, i * 10));
        }
        // Cell (0, 1): 1 location
        locations.add(new Location(mockWorld, 10, 64, 260));
        
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertEquals(256, heatmap.getGridSize());
        assertEquals(9, heatmap.getTotalLocations());
        assertEquals(3, heatmap.getOccupiedCells());
        assertEquals(5, heatmap.getMaxCount());
        assertEquals(1, heatmap.getMinCount());
        assertEquals(3.0, heatmap.getAverageCount());
    }
}
