package com.skyblockexp.ezrtp.config.safety;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.bukkit.Bukkit;

/**
 * Encapsulates optional safety enhancements applied after a teleport location is selected.
 */
public final class SafetySettings {
    private static final int DEFAULT_MAX_SURFACE_SCAN_DEPTH = 20;
    private static final int DEFAULT_MAX_SURFACE_SCAN_DEPTH_NETHER = 128;
    private static final int MAX_SURFACE_SCAN_DEPTH_LIMIT = 128;

    private final boolean placeDirtOnWater;
    private final Material placeBlockMaterial;
    private final boolean rescueEnabled;
    private final int maxVerticalRescue;
    private final int maxSurfaceScanDepth;
    private final int maxSurfaceScanDepthNether;
    private final boolean rejectLavaAboveInNether;
    private final boolean rejectWaterAboveInOverworld;

    public SafetySettings(boolean placeDirtOnWater,
                          boolean rescueEnabled,
                          int maxVerticalRescue,
                          int maxSurfaceScanDepth,
                          Material placeBlockMaterial) {
        this(placeDirtOnWater, rescueEnabled, maxVerticalRescue, maxSurfaceScanDepth, DEFAULT_MAX_SURFACE_SCAN_DEPTH_NETHER, placeBlockMaterial, true, true);
    }

    public SafetySettings(boolean placeDirtOnWater,
                          boolean rescueEnabled,
                          int maxVerticalRescue,
                          int maxSurfaceScanDepth,
                          int maxSurfaceScanDepthNether,
                          Material placeBlockMaterial) {
        this(placeDirtOnWater, rescueEnabled, maxVerticalRescue, maxSurfaceScanDepth, maxSurfaceScanDepthNether, placeBlockMaterial, true, true);
    }

    public SafetySettings(boolean placeDirtOnWater,
                          boolean rescueEnabled,
                          int maxVerticalRescue,
                          int maxSurfaceScanDepth,
                          int maxSurfaceScanDepthNether,
                          Material placeBlockMaterial,
                          boolean rejectLavaAboveInNether,
                          boolean rejectWaterAboveInOverworld) {
        this.placeDirtOnWater = placeDirtOnWater;
        this.rescueEnabled = rescueEnabled;
        this.maxVerticalRescue = Math.max(1, maxVerticalRescue);
        this.maxSurfaceScanDepth = Math.max(1, Math.min(maxSurfaceScanDepth, MAX_SURFACE_SCAN_DEPTH_LIMIT));
        this.maxSurfaceScanDepthNether = Math.max(1, Math.min(maxSurfaceScanDepthNether, MAX_SURFACE_SCAN_DEPTH_LIMIT));
        if (placeBlockMaterial != null) {
            this.placeBlockMaterial = placeBlockMaterial;
        } else {
            Material ice = Material.matchMaterial("ICE");
            this.placeBlockMaterial = ice != null ? ice : Material.DIRT;
        }
        this.rejectLavaAboveInNether = rejectLavaAboveInNether;
        this.rejectWaterAboveInOverworld = rejectWaterAboveInOverworld;
    }

    public boolean isPlaceDirtOnWater() {
        return placeDirtOnWater;
    }

    public Material getPlaceBlockMaterial() {
        return placeBlockMaterial;
    }

    public boolean isRescueEnabled() {
        return rescueEnabled;
    }

    public int getMaxVerticalRescue() {
        return maxVerticalRescue;
    }

    public int getMaxSurfaceScanDepth() {
        return maxSurfaceScanDepth;
    }

    public int getMaxSurfaceScanDepthNether() {
        return maxSurfaceScanDepthNether;
    }

    public boolean isRejectLavaAboveInNether() {
        return rejectLavaAboveInNether;
    }

    public boolean isRejectWaterAboveInOverworld() {
        return rejectWaterAboveInOverworld;
    }

    public static SafetySettings defaults() {
        return new SafetySettings(false, true, 6, DEFAULT_MAX_SURFACE_SCAN_DEPTH, DEFAULT_MAX_SURFACE_SCAN_DEPTH_NETHER, null, true, true);
    }

    public static SafetySettings fromConfiguration(ConfigurationSection section, SafetySettings fallback) {
        SafetySettings defaults = fallback != null ? fallback : defaults();
        if (section == null) {
            return defaults;
        }

        ConfigurationSection water = section.getConfigurationSection("water");
        boolean placeDirt;
        if (water != null) {
            if (water.contains("place-block-on-surface")) {
                placeDirt = water.getBoolean("place-block-on-surface", defaults.isPlaceDirtOnWater());
            } else if (water.contains("place-dirt-on-surface")) {
                placeDirt = water.getBoolean("place-dirt-on-surface", defaults.isPlaceDirtOnWater());
                Bukkit.getLogger().warning("[EzRTP] 'safety.water.place-dirt-on-surface' is deprecated; use 'safety.water.place-block-on-surface' instead.");
            } else {
                placeDirt = defaults.isPlaceDirtOnWater();
            }
        } else {
            placeDirt = defaults.isPlaceDirtOnWater();
        }

        String materialName = water != null
            ? water.getString("material", defaults.getPlaceBlockMaterial().name())
            : defaults.getPlaceBlockMaterial().name();
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = defaults.getPlaceBlockMaterial();
        }

        ConfigurationSection recovery = section.getConfigurationSection("recovery");
        boolean rescue = recovery != null
                ? recovery.getBoolean("enabled", defaults.isRescueEnabled())
                : defaults.isRescueEnabled();
        int maxVertical = recovery != null
                ? recovery.getInt("max-vertical-adjust", defaults.getMaxVerticalRescue())
                : defaults.getMaxVerticalRescue();
        int maxSurfaceScanDepth = recovery != null
                ? recovery.getInt("max-surface-scan-depth", defaults.getMaxSurfaceScanDepth())
                : defaults.getMaxSurfaceScanDepth();
        int maxSurfaceScanDepthNether = recovery != null
                ? recovery.getInt("max-surface-scan-depth-nether", defaults.getMaxSurfaceScanDepthNether())
                : defaults.getMaxSurfaceScanDepthNether();

        boolean rejectLava = section.getBoolean("reject-liquid-above-nether", defaults.isRejectLavaAboveInNether());
        boolean rejectWater = section.getBoolean("reject-liquid-above-overworld", defaults.isRejectWaterAboveInOverworld());

        return new SafetySettings(placeDirt, rescue, maxVertical, maxSurfaceScanDepth, maxSurfaceScanDepthNether, material, rejectLava, rejectWater);
    }
}
