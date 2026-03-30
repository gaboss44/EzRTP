package com.skyblockexp.ezrtp.util.compat;

import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection-based compatibility shim for WorldBorder behavior across server versions.
 * Exposes a safe API to obtain a border radius (half the size) when available.
 */
public final class WorldBorderCompat {

    private WorldBorderCompat() {
    }

    /**
     * Returns the border radius (half the size) for the given world, if available.
     * Uses reflection to support older/newer server APIs where WorldBorder methods differ.
     */
    public static Optional<Double> getBorderRadius(World world) {
        if (world == null) return Optional.empty();
        try {
            // Try to call World#getWorldBorder()
            Method getWorldBorder = World.class.getMethod("getWorldBorder");
            Object border = getWorldBorder.invoke(world);
            if (border == null) return Optional.empty();

            // Try border.getSize()
            try {
                Method getSize = border.getClass().getMethod("getSize");
                Object sizeObj = getSize.invoke(border);
                if (sizeObj instanceof Number) {
                    double size = ((Number) sizeObj).doubleValue();
                    return Optional.of(size * 0.5D);
                }
            } catch (NoSuchMethodException ignore) {
                // fallback to methods that may exist on some platforms
            }

            // Try border.getDiameter() as a rare alternative
            try {
                Method getDiameter = border.getClass().getMethod("getDiameter");
                Object diamObj = getDiameter.invoke(border);
                if (diamObj instanceof Number) {
                    double diam = ((Number) diamObj).doubleValue();
                    return Optional.of(diam * 0.5D);
                }
            } catch (NoSuchMethodException ignore) {
            }

            // Try border.getCenter() -> Location, then compute distance-based radius is not available here
            try {
                Method getCenter = border.getClass().getMethod("getCenter");
                Object centerObj = getCenter.invoke(border);
                if (centerObj instanceof Location) {
                    // We have a center but no size method: cannot determine radius
                    return Optional.empty();
                }
            } catch (NoSuchMethodException ignore) {
            }

        } catch (Exception e) {
            // reflection failed; treat as no border support
        }
        return Optional.empty();
    }

    /**
     * Convenience: returns radius if present, otherwise defaultRadius.
     */
    public static double getBorderRadiusOrDefault(World world, double defaultRadius) {
        return getBorderRadius(world).orElse(defaultRadius);
    }

    /**
     * Returns the border center as a Location when available.
     */
    public static Optional<Location> getCenter(World world) {
        if (world == null) return Optional.empty();
        try {
            Method getWorldBorder = World.class.getMethod("getWorldBorder");
            Object border = getWorldBorder.invoke(world);
            if (border == null) return Optional.empty();

            try {
                Method getCenter = border.getClass().getMethod("getCenter");
                Object center = getCenter.invoke(border);
                if (center instanceof Location) {
                    return Optional.of((Location) center);
                }
            } catch (NoSuchMethodException ignore) {
            }

            // Try separate getCenterX/getCenterZ methods
            try {
                Method getCenterX = border.getClass().getMethod("getCenterX");
                Method getCenterZ = border.getClass().getMethod("getCenterZ");
                Object xObj = getCenterX.invoke(border);
                Object zObj = getCenterZ.invoke(border);
                if (xObj instanceof Number && zObj instanceof Number) {
                    double x = ((Number) xObj).doubleValue();
                    double z = ((Number) zObj).doubleValue();
                    Location loc = new Location(world, x, world.getSpawnLocation() != null ? world.getSpawnLocation().getY() : 64.0, z);
                    return Optional.of(loc);
                }
            } catch (NoSuchMethodException ignore) {
            }

        } catch (Exception e) {
            // ignore
        }
        return Optional.empty();
    }

    /**
     * Returns true if the given location is inside the world's border, when supported.
     * Falls back to a conservative computation when possible.
     */
    public static boolean isInside(Location location) {
        if (location == null) return false;
        World world = location.getWorld();
        if (world == null) return false;
        try {
            Method getWorldBorder = World.class.getMethod("getWorldBorder");
            Object border = getWorldBorder.invoke(world);
            if (border == null) return true; // no border => inside

            // Try isInside(Location)
            try {
                Method isInsideLoc = border.getClass().getMethod("isInside", Location.class);
                Object res = isInsideLoc.invoke(border, location);
                if (res instanceof Boolean) return (Boolean) res;
            } catch (NoSuchMethodException ignore) {
            }

            // Try isInside(double,double)
            try {
                Method isInsideD = border.getClass().getMethod("isInside", double.class, double.class);
                Object res = isInsideD.invoke(border, location.getX(), location.getZ());
                if (res instanceof Boolean) return (Boolean) res;
            } catch (NoSuchMethodException ignore) {
            }

            // Fall back to center+radius check if possible
            Optional<Location> center = getCenter(world);
            Optional<Double> radius = getBorderRadius(world);
            if (center.isPresent() && radius.isPresent()) {
                double dx = location.getX() - center.get().getX();
                double dz = location.getZ() - center.get().getZ();
                return dx * dx + dz * dz <= radius.get() * radius.get();
            }

            // Unable to determine; assume inside
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
