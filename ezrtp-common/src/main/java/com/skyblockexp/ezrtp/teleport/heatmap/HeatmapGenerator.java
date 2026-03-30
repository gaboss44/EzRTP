package com.skyblockexp.ezrtp.teleport.heatmap;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates heatmap data from a list of RTP locations.
 * Groups locations into grid cells and counts the frequency of locations in each cell.
 */
public final class HeatmapGenerator {
    
    private static final int DEFAULT_GRID_SIZE = 512; // sensible default grid size (blocks per cell)
    private static final int MIN_GRID_SIZE = 16;
    private static final int MAX_GRID_SIZE = 4096;
    
    private final int gridSize;
    
    /**
     * Creates a heatmap generator with the default grid size.
     */
    public HeatmapGenerator() {
        this(DEFAULT_GRID_SIZE);
    }
    
    /**
     * Creates a heatmap generator with a custom grid size.
     * 
     * @param gridSize the size of each grid cell in blocks (must be between 16 and 4096)
     */
    public HeatmapGenerator(int gridSize) {
        if (gridSize < MIN_GRID_SIZE || gridSize > MAX_GRID_SIZE) {
            throw new IllegalArgumentException(
                "Grid size must be between " + MIN_GRID_SIZE + " and " + MAX_GRID_SIZE);
        }
        this.gridSize = gridSize;
    }
    
    /**
     * Generates heatmap data from a list of locations.
     * 
     * @param locations the list of locations to process
     * @return a HeatmapData object containing the heatmap grid and statistics
     */
    public HeatmapData generate(List<Location> locations) {
        // Normal operation; no debug logging.

        if (locations == null || locations.isEmpty()) {
            return new HeatmapData(gridSize, new HashMap<>());
        }
        
        Map<GridCell, Integer> heatmap = new HashMap<>();
        
        int __idx = 0;
        for (Location loc : locations) {
            if (loc == null) {
                continue;
            }
            
            GridCell cell = new GridCell(
                Math.floorDiv(loc.getBlockX(), gridSize),
                Math.floorDiv(loc.getBlockZ(), gridSize)
            );
            // keep iteration lightweight; no per-location debug logging
            if (__idx < 0) {
                // intentionally no-op to keep structure similar when debugging was present
            }
            __idx++;
            
            heatmap.merge(cell, 1, Integer::sum);
        }
        // no diagnostic recalculation logging in production
        
        // no diagnostic occupied-cells logging in production
        return new HeatmapData(gridSize, heatmap);
    }
    
    /**
     * Represents a grid cell in the heatmap.
     */
    public static final class GridCell {
        private final int x;
        private final int z;

        public GridCell(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int x() {
            return x;
        }

        public int z() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GridCell gridCell = (GridCell) o;
            return x == gridCell.x && z == gridCell.z;
        }

        @Override
        public int hashCode() {
            return 31 * x + z;
        }
    }
    
    /**
     * Represents the result of heatmap generation.
     */
    public static class HeatmapData {
        private final int gridSize;
        private final Map<GridCell, Integer> data;
        
        public HeatmapData(int gridSize, Map<GridCell, Integer> data) {
            this.gridSize = gridSize;
            this.data = data;
        }
        
        /**
         * Returns the grid size used for this heatmap.
         */
        public int getGridSize() {
            return gridSize;
        }
        
        /**
         * Returns the heatmap data as a map of grid cells to location counts.
         */
        public Map<GridCell, Integer> getData() {
            return data;
        }
        
        /**
         * Returns the total number of locations in the heatmap.
         */
        public int getTotalLocations() {
            return data.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        /**
         * Returns the number of grid cells with at least one location.
         */
        public int getOccupiedCells() {
            return data.size();
        }
        
        /**
         * Returns the maximum count in any single cell.
         */
        public int getMaxCount() {
            return data.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        }
        
        /**
         * Returns the minimum count in any occupied cell.
         */
        public int getMinCount() {
            return data.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        }
        
        /**
         * Returns the average count per occupied cell.
         */
        public double getAverageCount() {
            if (data.isEmpty()) {
                return 0.0;
            }
            return (double) getTotalLocations() / data.size();
        }
        
        /**
         * Returns the minimum X coordinate of a cell in blocks.
         */
        public int getCellMinX(GridCell cell) {
            return cell.x() * gridSize;
        }
        
        /**
         * Returns the minimum Z coordinate of a cell in blocks.
         */
        public int getCellMinZ(GridCell cell) {
            return cell.z() * gridSize;
        }
        
        /**
         * Returns the maximum X coordinate of a cell in blocks.
         */
        public int getCellMaxX(GridCell cell) {
            return (cell.x() + 1) * gridSize - 1;
        }
        
        /**
         * Returns the maximum Z coordinate of a cell in blocks.
         */
        public int getCellMaxZ(GridCell cell) {
            return (cell.z() + 1) * gridSize - 1;
        }
    }
}
