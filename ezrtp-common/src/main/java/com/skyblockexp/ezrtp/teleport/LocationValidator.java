package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.protection.ProtectionRegistry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Validates locations for safety, biome requirements, and protection status.
 */
public final class LocationValidator {

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private volatile ProtectionRegistry protectionRegistry;

    public LocationValidator(org.bukkit.plugin.java.JavaPlugin plugin, ProtectionRegistry protectionRegistry) {
        this.plugin = plugin;
        this.protectionRegistry = protectionRegistry;
    }

    public void setProtectionRegistry(ProtectionRegistry protectionRegistry) {
        this.protectionRegistry = protectionRegistry;
    }

    /**
     * Checks if a location is safe for teleportation.
     */
    public boolean isSafe(Location location, RandomTeleportSettings currentSettings) {
        if (location == null || currentSettings == null) {
            return false;
        }

        Block blockBelow = location.clone().subtract(0, 1, 0).getBlock();
        Block destination = location.getBlock();
        Material typeBelow = blockBelow.getType();
        int y = location.getBlockY();
        int minY = currentSettings.getMinY() != null ? currentSettings.getMinY() : getWorldMinHeight(location.getWorld());
        int maxY = currentSettings.getMaxY() != null ? currentSettings.getMaxY() : getWorldMaxHeight(location.getWorld());

        if (y < minY || y > maxY) {
            debugReject(currentSettings, location, "Y out of range: " + y + " not in [" + minY + ", " + maxY + "]");
            return false;
        }

        if (destination.isLiquid()) {
            debugReject(currentSettings, location, "Destination is inside liquid: " + destination.getType());
            return false;
        }

        // Optionally reject locations with liquid directly above (lava in Nether, water in Overworld)
        Block blockAbove = location.clone().add(0, 1, 0).getBlock();
        if (blockAbove.isLiquid()) {
            org.bukkit.World.Environment env = location.getWorld().getEnvironment();
            Material aboveType = blockAbove.getType();
            com.skyblockexp.ezrtp.config.SafetySettings safety = currentSettings.getSafetySettings();
            if (env == World.Environment.NETHER && aboveType == Material.LAVA && safety.isRejectLavaAboveInNether()) {
                debugReject(currentSettings, location, "Lava above destination in Nether: " + aboveType);
                return false;
            }
            if (env == World.Environment.NORMAL && aboveType == Material.WATER && safety.isRejectWaterAboveInOverworld()) {
                debugReject(currentSettings, location, "Water above destination in Overworld: " + aboveType);
                return false;
            }
        }

        boolean waterSurface = typeBelow == Material.WATER && !destination.isLiquid();
        if (currentSettings.getUnsafeBlocks().contains(typeBelow) && !waterSurface) {
            debugReject(currentSettings, location, "Unsafe block below: " + typeBelow);
            return false;
        }

        if (!blockBelow.getType().isSolid() && !waterSurface) {
            debugReject(currentSettings, location, "Block below not solid: " + typeBelow);
            return false;
        }

        if (!com.skyblockexp.ezrtp.util.compat.WorldBorderCompat.isInside(location)) {
            debugReject(currentSettings, location, "Outside world border");
            return false;
        }

        return true;
    }

    /**
     * Checks if a location's biome is allowed by the current settings.
     */
    public boolean isBiomeAllowed(Location location, RandomTeleportSettings currentSettings) {
        if (location == null || currentSettings == null) {
            return false;
        }

        if (currentSettings.getBiomeInclude().isEmpty() && currentSettings.getBiomeExclude().isEmpty()) {
            return true;
        }

        org.bukkit.block.Biome biome = location.getBlock().getBiome();

        if (!currentSettings.getBiomeExclude().isEmpty()
                && currentSettings.getBiomeExclude().contains(biome)) {
            return false;
        }

        if (!currentSettings.getBiomeInclude().isEmpty()
                && !currentSettings.getBiomeInclude().contains(biome)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a location is protected by claims.
     */
    public boolean isProtectedByClaims(Location location, RandomTeleportSettings currentSettings) {
        ProtectionRegistry registry = this.protectionRegistry;
        if (registry == null || location == null || currentSettings == null) {
            return false;
        }
        return registry.findProtectionProvider(location, currentSettings.getProtectionSettings()).isPresent();
    }

    /**
     * Checks if the given settings have biome filters enabled.
     * Returns {@code false} when {@code biome-filtering.enabled} is set to {@code false},
     * even if include/exclude lists are non-empty.
     */
    public static boolean hasBiomeFilters(RandomTeleportSettings settings) {
        return settings != null
            && settings.isBiomeFilteringEnabled()
            && (!settings.getBiomeInclude().isEmpty() || !settings.getBiomeExclude().isEmpty());
    }

    private void debugReject(RandomTeleportSettings settings, Location loc, String reason) {
        if (settings != null && settings.isDebugRejectionLoggingEnabled()) {
            plugin.getLogger().info("[EzRTP] RTP rejected " + loc + ": " + reason);
        }
    }

    private static int getWorldMinHeight(World world) {
        try {
            return (int) World.class.getMethod("getMinHeight").invoke(world);
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    private static int getWorldMaxHeight(World world) {
        try {
            return (int) World.class.getMethod("getMaxHeight").invoke(world);
        } catch (ReflectiveOperationException ignored) {
            return world.getMaxHeight();
        }
    }
}