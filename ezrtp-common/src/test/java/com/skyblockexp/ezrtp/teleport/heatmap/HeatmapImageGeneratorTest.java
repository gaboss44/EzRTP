package com.skyblockexp.ezrtp.teleport.heatmap;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HeatmapImageGenerator.
 */
class HeatmapImageGeneratorTest {

    private World mockWorld;

    @BeforeEach
    void setUp() {
        mockWorld = Mockito.mock(World.class);
    }

    @Test
    void testDefaultMapSize() {
        HeatmapImageGenerator generator = new HeatmapImageGenerator();
        assertEquals(128, generator.getMapSize());
    }

    @Test
    void testCustomMapSize() {
        HeatmapImageGenerator generator = new HeatmapImageGenerator(256);
        assertEquals(256, generator.getMapSize());
    }

    @Test
    void testInvalidMapSizeTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> new HeatmapImageGenerator(0));
    }

    @Test
    void testInvalidMapSizeTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> new HeatmapImageGenerator(1025));
    }

    @Test
    void testGenerateWithNullData() {
        HeatmapImageGenerator generator = new HeatmapImageGenerator();
        BufferedImage image = generator.generate(null, 0, 0, 1000);
        assertNotNull(image);
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
    }

    @Test
    void testGenerateWithEmptyData() {
        HeatmapImageGenerator generator = new HeatmapImageGenerator();
        List<Location> locations = new ArrayList<>();
        HeatmapGenerator heatmapGen = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = heatmapGen.generate(locations);
        BufferedImage image = generator.generate(heatmap, 0, 0, 1000);
        assertNotNull(image);
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
    }

    @Test
    void testGenerateWithSingleLocation() {
        HeatmapImageGenerator generator = new HeatmapImageGenerator();
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(mockWorld, 100, 64, 200));
        HeatmapGenerator heatmapGen = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = heatmapGen.generate(locations);
        BufferedImage image = generator.generate(heatmap, 0, 0, 1000);
        assertNotNull(image);
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
        assertEquals(BufferedImage.TYPE_INT_ARGB, image.getType());
    }

    @Test
    void testGenerateWithMultipleLocations() {
        HeatmapImageGenerator generator = new HeatmapImageGenerator(128);
        List<Location> locations = new ArrayList<>();
        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                locations.add(new Location(mockWorld, x * 100, 64, z * 100));
            }
        }
        HeatmapGenerator heatmapGen = new HeatmapGenerator(512);
        HeatmapGenerator.HeatmapData heatmap = heatmapGen.generate(locations);
        BufferedImage image = generator.generate(heatmap, 0, 0, 1000);
        assertNotNull(image);
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
        boolean hasNonBackgroundPixel = false;
        int backgroundColor = new java.awt.Color(20, 20, 30).getRGB();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) != backgroundColor) {
                    hasNonBackgroundPixel = true;
                    break;
                }
            }
            if (hasNonBackgroundPixel) break;
        }
        assertTrue(hasNonBackgroundPixel, "Image should contain non-background pixels");
    }

    @Test
    void testGenerateWithDifferentMapSizes() {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(mockWorld, 100, 64, 200));
        
        HeatmapGenerator heatmapGen = new HeatmapGenerator();
        HeatmapGenerator.HeatmapData heatmap = heatmapGen.generate(locations);
        
        // Test different map sizes
        int[] mapSizes = {64, 128, 256, 512};
        for (int size : mapSizes) {
            HeatmapImageGenerator generator = new HeatmapImageGenerator(size);
            BufferedImage image = generator.generate(heatmap, 0, 0, 1000);
            assertNotNull(image);
            assertEquals(size, image.getWidth());
            assertEquals(size, image.getHeight());
        }
    }

    @Test
    void testGenerateWithClusteredLocations() {
        HeatmapImageGenerator generator = new HeatmapImageGenerator(128);
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            locations.add(new Location(mockWorld, 100 + i, 64, 200 + i));
        }
        for (int i = 0; i < 10; i++) {
            locations.add(new Location(mockWorld, 1000 + i, 64, 1000 + i));
        }
        HeatmapGenerator heatmapGen = new HeatmapGenerator(512);
        HeatmapGenerator.HeatmapData heatmap = heatmapGen.generate(locations);
        BufferedImage image = generator.generate(heatmap, 0, 0, 1000);
        assertNotNull(image);
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
    }
}
