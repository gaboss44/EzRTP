package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.protection.ProtectionRegistry;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationCause;

import java.util.Optional;
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
     *
     * @return {@code true} when the location passes all safety checks
     */
    public boolean isSafe(Location location, RandomTeleportSettings currentSettings) {
        return checkSafety(location, currentSettings).isEmpty();
    }

    /**
     * Checks if a location is safe for teleportation and returns the cause of any rejection.
     *
     * <p>All debug-rejection logging is preserved. When the location passes every check,
     * {@link Optional#empty()} is returned. Otherwise an {@link Optional} containing the
     * {@link UnsafeLocationCause} that triggered the first failing check is returned.
     *
     * @param location        the candidate location, may be {@code null}
     * @param currentSettings the RTP settings in effect, may be {@code null}
     * @return empty when safe; the cause when unsafe
     */
    public Optional<UnsafeLocationCause> checkSafety(
            Location location, RandomTeleportSettings currentSettings) {
        if (location == null || currentSettings == null) {
            return Optional.of(UnsafeLocationCause.OTHER);
        }

        Block blockBelow = location.clone().subtract(0, 1, 0).getBlock();
        Block destination = location.getBlock();
        Material typeBelow = blockBelow.getType();
        int y = location.getBlockY();
        int minY =
                currentSettings.getMinY() != null
                        ? currentSettings.getMinY()
                        : getWorldMinHeight(location.getWorld());
        int maxY =
                currentSettings.getMaxY() != null
                        ? currentSettings.getMaxY()
                        : getWorldMaxHeight(location.getWorld());

        if (y < minY || y > maxY) {
            debugReject(
                    currentSettings,
                    location,
                    "Y out of range: " + y + " not in [" + minY + ", " + maxY + "]");
            return Optional.of(UnsafeLocationCause.OUT_OF_BOUNDS);
        }

        if (destination.isLiquid()) {
            debugReject(
                    currentSettings, location, "Destination is inside liquid: " + destination.getType());
            return Optional.of(
                    destination.getType() == Material.LAVA
                            ? UnsafeLocationCause.LAVA
                            : UnsafeLocationCause.LIQUID);
        }

        // Optionally reject locations with liquid directly above (lava in Nether, water in Overworld)
        Block blockAbove = location.clone().add(0, 1, 0).getBlock();
        if (blockAbove.isLiquid()) {
            org.bukkit.World.Environment env = location.getWorld().getEnvironment();
            Material aboveType = blockAbove.getType();
            com.skyblockexp.ezrtp.config.SafetySettings safety = currentSettings.getSafetySettings();
            if (env == World.Environment.NETHER
                    && aboveType == Material.LAVA
                    && safety.isRejectLavaAboveInNether()) {
                debugReject(
                        currentSettings, location, "Lava above destination in Nether: " + aboveType);
                return Optional.of(UnsafeLocationCause.LAVA);
            }
            if (env == World.Environment.NORMAL
                    && aboveType == Material.WATER
                    && safety.isRejectWaterAboveInOverworld()) {
                debugReject(
                        currentSettings,
                        location,
                        "Water above destination in Overworld: " + aboveType);
                return Optional.of(UnsafeLocationCause.LIQUID_SURFACE);
            }
        }

        boolean waterSurface = typeBelow == Material.WATER && !destination.isLiquid();
        if (currentSettings.getUnsafeBlocks().contains(typeBelow) && !waterSurface) {
            debugReject(currentSettings, location, "Unsafe block below: " + typeBelow);
            UnsafeLocationCause cause =
                    (typeBelow == Material.LAVA || typeBelow == Material.MAGMA_BLOCK)
                            ? UnsafeLocationCause.LAVA
                            : UnsafeLocationCause.UNSAFE_BLOCK;
            return Optional.of(cause);
        }

        if (!blockBelow.getType().isSolid() && !waterSurface) {
            debugReject(currentSettings, location, "Block below not solid: " + typeBelow);
            return Optional.of(UnsafeLocationCause.VOID);
        }

        if (!com.skyblockexp.ezrtp.util.compat.WorldBorderCompat.isInside(location)) {
            debugReject(currentSettings, location, "Outside world border");
            return Optional.of(UnsafeLocationCause.OUT_OF_BOUNDS);
        }

        return Optional.empty();
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