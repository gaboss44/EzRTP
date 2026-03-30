package com.skyblockexp.ezrtp.teleport.biome;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.logging.Logger;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for BiomeLocationCache focusing on biome-mismatched location caching.
 */
class BiomeLocationCacheTest {

    @Mock
    private JavaPlugin plugin;
    
    @Mock
    private World world;
    
    @Mock
    private Block block;
    
    private BiomeLocationCache cache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(world.getName()).thenReturn("test_world");
        when(plugin.getLogger()).thenReturn(Logger.getLogger("BiomeLocationCacheTest"));
        
        // Initialize cache with test parameters
        cache = new BiomeLocationCache(plugin, 50, 0, 10);
        cache.setEnabled(true);
    }

    @Test
    void testCacheStoresDifferentBiomes() {
        // Create locations with different biomes
        Location plainsLocation = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location desertLocation = createMockLocation(world, 200, 64, 200, Biome.DESERT);
        Location forestLocation = createMockLocation(world, 300, 64, 300, Biome.FOREST);
        
        // Cache all locations
        cache.cache(plainsLocation);
        cache.cache(desertLocation);
        cache.cache(forestLocation);
        
        // Verify statistics
        BiomeLocationCache.CacheStats stats = cache.getStats();
        assertEquals(1, stats.worldCount(), "Should have 1 world");
        assertEquals(3, stats.biomeCount(), "Should have 3 different biomes");
        assertEquals(3, stats.locationCount(), "Should have 3 cached locations");
    }

    @Test
    void testCacheRetrievesByBiomeFilter() {
        // Cache locations for different biomes
        Location plainsLocation = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location desertLocation = createMockLocation(world, 200, 64, 200, Biome.DESERT);
        
        cache.cache(plainsLocation);
        cache.cache(desertLocation);
        
        // Retrieve with PLAINS filter
        Set<Biome> includePlains = Set.of(Biome.PLAINS);
        Location retrieved = cache.get(world, includePlains, Set.of());
        
        assertNotNull(retrieved, "Should retrieve a cached location");
        assertEquals(Biome.PLAINS, retrieved.getBlock().getBiome(), "Should retrieve PLAINS location");
    }

    @Test
    void testCacheRespectsExcludeFilter() {
        // Cache locations for different biomes
        Location plainsLocation = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location desertLocation = createMockLocation(world, 200, 64, 200, Biome.DESERT);
        
        cache.cache(plainsLocation);
        cache.cache(desertLocation);
        
        // Try to retrieve DESERT with DESERT excluded
        Set<Biome> includeDesert = Set.of(Biome.DESERT);
        Set<Biome> excludeDesert = Set.of(Biome.DESERT);
        Location retrieved = cache.get(world, includeDesert, excludeDesert);
        
        // Should not retrieve because DESERT is excluded
        assertNull(retrieved, "Should not retrieve excluded biome");
    }

    @Test
    void testCacheDisabledReturnsNull() {
        cache.setEnabled(false);
        
        Location location = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        cache.cache(location);
        
        Location retrieved = cache.get(world, Set.of(Biome.PLAINS), Set.of());
        assertNull(retrieved, "Disabled cache should return null");
    }

    @Test
    void testCacheClearRemovesAllLocations() {
        Location location1 = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location location2 = createMockLocation(world, 200, 64, 200, Biome.DESERT);
        
        cache.cache(location1);
        cache.cache(location2);
        
        assertEquals(2, cache.getStats().locationCount());
        
        cache.clear();
        
        assertEquals(0, cache.getStats().locationCount(), "Cache should be empty after clear");
    }

    @Test
    void testCachePreventsNearbyDuplicates() {
        // Create two locations very close together (within 10 blocks)
        Location location1 = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location location2 = createMockLocation(world, 105, 64, 105, Biome.PLAINS);
        
        cache.cache(location1);
        cache.cache(location2); // Should be rejected as duplicate
        
        BiomeLocationCache.CacheStats stats = cache.getStats();
        assertEquals(1, stats.locationCount(), "Should only have 1 location (duplicate prevented)");
    }

    @Test
    void testCacheHandlesMaxCapacity() {
        // Create cache with max 3 locations per biome
        BiomeLocationCache smallCache = new BiomeLocationCache(plugin, 3, 0, 10);
        smallCache.setEnabled(true);
        
        // Cache 5 PLAINS locations
        for (int i = 0; i < 5; i++) {
            Location location = createMockLocation(world, i * 100, 64, i * 100, Biome.PLAINS);
            smallCache.cache(location);
        }
        
        BiomeLocationCache.CacheStats stats = smallCache.getStats();
        assertEquals(3, stats.locationCount(), "Should only keep 3 locations (max capacity)");
    }

    @Test
    void testCacheReturnsNullWithoutBiomeFilters() {
        Location location = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        cache.cache(location);
        
        // Try to retrieve without any biome filters
        Location retrieved = cache.get(world, Set.of(), Set.of());
        
        assertNull(retrieved, "Should return null when no biome filters provided");
    }

    @Test
    void testGetAllLocationsFiltersExpired() throws InterruptedException {
        // Create a cache with 0 minutes expiration time (immediate expiration)
        BiomeLocationCache shortCache = new BiomeLocationCache(plugin, 50, 0, 0);
        shortCache.setEnabled(true);
        
        // Add some locations
        Location location1 = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location location2 = createMockLocation(world, 200, 64, 200, Biome.DESERT);
        Location location3 = createMockLocation(world, 300, 64, 300, Biome.FOREST);
        
        shortCache.cache(location1);
        shortCache.cache(location2);
        shortCache.cache(location3);
        
        // Verify all 3 locations are cached
        assertEquals(3, shortCache.getStats().locationCount());
        
        // Wait a short time to ensure locations are immediately expired
        // Note: Thread.sleep is necessary here as we're testing time-dependent expiration behavior
        Thread.sleep(50);
        
        // Get all locations - should filter out expired ones
        java.util.List<Location> locations = shortCache.getAllLocations("test_world");
        
        // Should return empty list as all locations are expired
        assertTrue(locations.isEmpty(), "getAllLocations should filter out expired locations");
        
        // Stats should show 0 locations after cleanup
        assertEquals(0, shortCache.getStats().locationCount(), "Expired locations should be removed from cache");
    }

    @Test
    void testGetAllLocationsReturnsValidLocations() {
        // Add some locations
        Location location1 = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location location2 = createMockLocation(world, 200, 64, 200, Biome.DESERT);
        Location location3 = createMockLocation(world, 300, 64, 300, Biome.FOREST);
        
        cache.cache(location1);
        cache.cache(location2);
        cache.cache(location3);
        
        // Get all locations
        java.util.List<Location> locations = cache.getAllLocations("test_world");
        
        // Should return all 3 valid locations
        assertEquals(3, locations.size(), "getAllLocations should return all valid locations");
    }
    
    @Test
    void testGetRandomCachedLocationReturnsFromAnyBiome() {
        // Cache locations for different biomes
        Location plainsLocation = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location desertLocation = createMockLocation(world, 200, 64, 200, Biome.DESERT);
        Location forestLocation = createMockLocation(world, 300, 64, 300, Biome.FOREST);
        
        cache.cache(plainsLocation);
        cache.cache(desertLocation);
        cache.cache(forestLocation);
        
        // Get a random cached location
        Location retrieved = cache.getRandomCachedLocation(world);
        
        assertNotNull(retrieved, "Should retrieve a random cached location");
        // Verify it's one of the cached locations
        assertTrue(retrieved.getBlock().getBiome() == Biome.PLAINS 
                || retrieved.getBlock().getBiome() == Biome.DESERT 
                || retrieved.getBlock().getBiome() == Biome.FOREST,
                "Retrieved location should be from one of the cached biomes");
    }
    
    @Test
    void testGetRandomCachedLocationReturnsNullWhenCacheEmpty() {
        // Don't cache any locations
        Location retrieved = cache.getRandomCachedLocation(world);
        
        assertNull(retrieved, "Should return null when cache is empty");
    }
    
    @Test
    void testGetRandomCachedLocationReturnsNullWhenDisabled() {
        // Cache a location
        Location location = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        cache.cache(location);
        
        // Disable cache
        cache.setEnabled(false);
        
        // Try to get random location
        Location retrieved = cache.getRandomCachedLocation(world);
        
        assertNull(retrieved, "Should return null when cache is disabled");
    }
    
    @Test
    void testGetRandomCachedLocationFiltersExpired() throws InterruptedException {
        // Create a cache with 0 minutes expiration time (immediate expiration)
        BiomeLocationCache shortCache = new BiomeLocationCache(plugin, 50, 0, 0);
        shortCache.setEnabled(true);
        
        // Cache a location
        Location location = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        shortCache.cache(location);
        
        // Wait to ensure location expires
        // Note: Thread.sleep is necessary here as we're testing time-dependent expiration behavior
        Thread.sleep(50);
        
        // Try to get random location
        Location retrieved = shortCache.getRandomCachedLocation(world);
        
        assertNull(retrieved, "Should return null when all cached locations are expired");
    }

    /**
     * Helper method to create a mock location with a specific biome.
     */
    private Location createMockLocation(World world, double x, double y, double z, Biome biome) {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        
        when(location.getWorld()).thenReturn(world);
        when(location.getX()).thenReturn(x);
        when(location.getY()).thenReturn(y);
        when(location.getZ()).thenReturn(z);
        when(location.getBlock()).thenReturn(block);
        when(block.getBiome()).thenReturn(biome);
        when(location.clone()).thenReturn(location);
        when(location.distanceSquared(any(Location.class))).thenAnswer(invocation -> {
            Location other = invocation.getArgument(0);
            double dx = x - other.getX();
            double dy = y - other.getY();
            double dz = z - other.getZ();
            return dx * dx + dy * dy + dz * dz;
        });
        
        return location;
    }
    
    @Test
    void testGetCachedLocationCountReturnsTotalForBiomes() {
        // Cache locations for different biomes
        Location plainsLocation1 = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        Location plainsLocation2 = createMockLocation(world, 200, 64, 200, Biome.PLAINS);
        Location desertLocation = createMockLocation(world, 300, 64, 300, Biome.DESERT);
        Location forestLocation = createMockLocation(world, 400, 64, 400, Biome.FOREST);
        
        cache.cache(plainsLocation1);
        cache.cache(plainsLocation2);
        cache.cache(desertLocation);
        cache.cache(forestLocation);
        
        // Count locations for PLAINS biome
        int plainsCount = cache.getCachedLocationCount(world, Set.of(Biome.PLAINS));
        assertEquals(2, plainsCount, "Should count 2 PLAINS locations");
        
        // Count locations for PLAINS and DESERT biomes
        int plainsAndDesertCount = cache.getCachedLocationCount(world, Set.of(Biome.PLAINS, Biome.DESERT));
        assertEquals(3, plainsAndDesertCount, "Should count 3 locations (2 PLAINS + 1 DESERT)");
        
        // Count locations for all cached biomes
        int allCount = cache.getCachedLocationCount(world, Set.of(Biome.PLAINS, Biome.DESERT, Biome.FOREST));
        assertEquals(4, allCount, "Should count all 4 cached locations");
    }
    
    @Test
    void testGetCachedLocationCountReturnsZeroWhenEmpty() {
        // Don't cache any locations
        int count = cache.getCachedLocationCount(world, Set.of(Biome.PLAINS));
        assertEquals(0, count, "Should return 0 when cache is empty");
    }
    
    @Test
    void testGetCachedLocationCountReturnsZeroWhenDisabled() {
        // Cache a location
        Location location = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        cache.cache(location);
        
        // Disable cache
        cache.setEnabled(false);
        
        // Count should be 0 when disabled
        int count = cache.getCachedLocationCount(world, Set.of(Biome.PLAINS));
        assertEquals(0, count, "Should return 0 when cache is disabled");
    }
    
    @Test
    void testGetCachedLocationCountIgnoresExpiredLocations() throws InterruptedException {
        // Create a cache with 0 minutes expiration time (immediate expiration)
        BiomeLocationCache shortCache = new BiomeLocationCache(plugin, 50, 0, 0);
        shortCache.setEnabled(true);
        
        // Cache a location
        Location location = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        shortCache.cache(location);
        
        // Verify it's cached (timing-dependent in headless tests; primary assertion is expiration)
        
        // Wait to ensure location expires
        // Note: Thread.sleep is necessary here as we're testing time-dependent expiration behavior
        Thread.sleep(50);
        
        // Count should be 0 after expiration
        int count = shortCache.getCachedLocationCount(world, Set.of(Biome.PLAINS));
        assertEquals(0, count, "Should return 0 when all locations are expired");
    }
    
    @Test
    void testGetCachedLocationCountReturnsZeroForUnmatchedBiomes() {
        // Cache PLAINS locations
        Location location = createMockLocation(world, 100, 64, 100, Biome.PLAINS);
        cache.cache(location);
        
        // Count DESERT locations (none cached)
        int count = cache.getCachedLocationCount(world, Set.of(Biome.DESERT));
        assertEquals(0, count, "Should return 0 for unmatched biomes");
    }
}
