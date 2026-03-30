package com.skyblockexp.ezrtp.teleport.heatmap;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HeatmapMapService.
 */
class HeatmapMapServiceTest {

    private World mockWorld;
    private Logger mockLogger;
    private HeatmapMapService mapService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockWorld = Mockito.mock(World.class);
        mockLogger = Mockito.mock(Logger.class);
        mapService = new HeatmapMapService(mockLogger);
    }

    @Test
    void testHasEnoughDataWithNull() {
        assertFalse(mapService.hasEnoughData(null));
    }

    @Test
    void testHasEnoughDataWithEmptyData() {
        List<Location> locations = new ArrayList<>();
        HeatmapGenerator generator = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertFalse(mapService.hasEnoughData(heatmap));
    }

    @Test
    void testHasEnoughDataWithInsufficientData() {
        List<Location> locations = new ArrayList<>();
        // Add only 3 locations (below threshold of 5)
        locations.add(new Location(mockWorld, 100, 64, 200));
        locations.add(new Location(mockWorld, 200, 64, 300));
        locations.add(new Location(mockWorld, 300, 64, 400));
        
        HeatmapGenerator generator = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertFalse(mapService.hasEnoughData(heatmap));
    }

    @Test
    void testHasEnoughDataWithSufficientData() {
        List<Location> locations = new ArrayList<>();
        // Add 10 locations (above threshold of 5)
        for (int i = 0; i < 10; i++) {
            locations.add(new Location(mockWorld, i * 100, 64, i * 100));
        }
        
        HeatmapGenerator generator = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        assertTrue(mapService.hasEnoughData(heatmap));
    }

    @Test
    void testGetMinDataThreshold() {
        assertEquals(5, mapService.getMinDataThreshold());
    }

    @Test
    void testSaveHeatmapAsPngWithNullData() {
        File outputFile = new File(tempDir.toFile(), "test.png");
        assertFalse(mapService.saveHeatmapAsPng(null, outputFile, 0, 0, 1000));
    }

    @Test
    void testSaveHeatmapAsPngWithInsufficientData() {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(mockWorld, 100, 64, 200));
        
        HeatmapGenerator generator = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        File outputFile = new File(tempDir.toFile(), "test.png");
        assertFalse(mapService.saveHeatmapAsPng(heatmap, outputFile, 0, 0, 1000));
    }

    @Test
    void testSaveHeatmapAsPngWithSufficientData() {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            locations.add(new Location(mockWorld, i * 100, 64, i * 100));
        }
        
        HeatmapGenerator generator = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        File outputFile = new File(tempDir.toFile(), "test.png");
        assertTrue(mapService.saveHeatmapAsPng(heatmap, outputFile, 0, 0, 1000));
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    @Test
    void testSaveHeatmapAsPngCreatesDirectory() {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            locations.add(new Location(mockWorld, i * 100, 64, i * 100));
        }
        
        HeatmapGenerator generator = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        File subDir = new File(tempDir.toFile(), "subdir");
        File outputFile = new File(subDir, "test.png");
        
        assertFalse(subDir.exists());
        assertTrue(mapService.saveHeatmapAsPng(heatmap, outputFile, 0, 0, 1000));
        assertTrue(subDir.exists());
        assertTrue(outputFile.exists());
    }

    @Test
    void testSaveMultipleHeatmaps() {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            locations.add(new Location(mockWorld, i * 100, 64, i * 100));
        }
        
        HeatmapGenerator generator = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = generator.generate(locations);
        
        // Save multiple heatmaps
        File outputFile1 = new File(tempDir.toFile(), "test1.png");
        File outputFile2 = new File(tempDir.toFile(), "test2.png");
        
        assertTrue(mapService.saveHeatmapAsPng(heatmap, outputFile1, 0, 0, 1000));
        assertTrue(mapService.saveHeatmapAsPng(heatmap, outputFile2, 0, 0, 1000));
        
        assertTrue(outputFile1.exists());
        assertTrue(outputFile2.exists());
        assertTrue(outputFile1.length() > 0);
        assertTrue(outputFile2.length() > 0);
    }
}
