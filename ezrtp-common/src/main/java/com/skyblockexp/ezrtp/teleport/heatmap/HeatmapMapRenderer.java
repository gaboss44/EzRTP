package com.skyblockexp.ezrtp.teleport.heatmap;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

/**
 * Custom MapRenderer that renders a heatmap image onto a Minecraft map.
 */
public final class HeatmapMapRenderer extends MapRenderer {
    
    private final BufferedImage heatmapImage;
    private boolean hasRendered = false;
    
    /**
     * Creates a new heatmap map renderer.
     * 
     * @param heatmapImage the heatmap image to render onto the map
     */
    public HeatmapMapRenderer(@NotNull BufferedImage heatmapImage) {
        super(false); // contextual = false, we want to render the same for everyone
        this.heatmapImage = heatmapImage;
    }
    
    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        // Only render once to avoid unnecessary computation
        if (hasRendered) {
            return;
        }
        
        // Draw the heatmap image onto the canvas
        canvas.drawImage(0, 0, heatmapImage);
        
        hasRendered = true;
    }
}
