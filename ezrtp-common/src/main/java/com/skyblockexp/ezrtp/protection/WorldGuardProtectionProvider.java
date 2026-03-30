package com.skyblockexp.ezrtp.protection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WorldGuardProtectionProvider implements ProtectionProvider {
    private static final String GLOBAL_REGION_ID = "__global__";

    private final Logger logger;
    private boolean available;

    // Reflection-backed handles
    private Object regionContainer;
    private Method regionContainerGetMethod;
    private Method bukkitAdapterAdaptMethod;
    private Method blockVector3AtMethod;
    private boolean wg7;
    private Object wg6PluginInstance;
    private Method wg6GetRegionManagerMethod;

    public WorldGuardProtectionProvider(PluginManager pluginManager, Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        Plugin plugin = pluginManager != null ? pluginManager.getPlugin("WorldGuard") : null;
        if (plugin == null || !plugin.isEnabled()) {
            this.available = false;
            this.regionContainer = null;
            this.regionContainerGetMethod = null;
            this.bukkitAdapterAdaptMethod = null;
            this.blockVector3AtMethod = null;
            this.wg7 = false;
            this.wg6PluginInstance = null;
            this.wg6GetRegionManagerMethod = null;
            return;
        }

        Object container = null;
        Method containerGet = null;
        Method adaptMethod = null;
        Method atMethod = null;
        boolean detectedWg7 = false;
        Object wg6Instance = null;
        Method wg6GetRegionManager = null;

        try {
            // Try WorldGuard 7+ (new API)
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstance = findMethod(worldGuardClass, "getInstance", 0);
            Object wgInstance = getInstance.invoke(null);
            Method getPlatform = findMethod(wgInstance.getClass(), "getPlatform", 0);
            Object platform = getPlatform.invoke(wgInstance);
            Method getRegionContainer = findMethod(platform.getClass(), "getRegionContainer", 0);
            container = getRegionContainer.invoke(platform);
            if (container == null) {
                this.available = false;
                this.regionContainer = null;
                this.regionContainerGetMethod = null;
                this.bukkitAdapterAdaptMethod = null;
                this.blockVector3AtMethod = null;
                this.wg7 = false;
                this.wg6PluginInstance = null;
                this.wg6GetRegionManagerMethod = null;
                return;
            }

            containerGet = findMethod(container.getClass(), "get", 1);

            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            adaptMethod = findMethod(bukkitAdapterClass, "adapt", 1);

            Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            atMethod = findMethod(blockVector3Class, "at", 3);

            detectedWg7 = true;
        } catch (ClassNotFoundException e) {
            // Not WG7. Try legacy WorldGuard 6.x via plugin instance
            try {
                wg6Instance = plugin;
                wg6GetRegionManager = findMethod(wg6Instance.getClass(), "getRegionManager", 1);
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "WorldGuard/WorldEdit detected but reflection setup failed; treating as unavailable.", ex);
                this.available = false;
                this.regionContainer = null;
                this.regionContainerGetMethod = null;
                this.bukkitAdapterAdaptMethod = null;
                this.blockVector3AtMethod = null;
                this.wg7 = false;
                this.wg6PluginInstance = null;
                this.wg6GetRegionManagerMethod = null;
                return;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "WorldGuard/WorldEdit detected but reflection setup failed; treating as unavailable.", ex);
            this.available = false;
            this.regionContainer = null;
            this.regionContainerGetMethod = null;
            this.bukkitAdapterAdaptMethod = null;
            this.blockVector3AtMethod = null;
            this.wg7 = false;
            this.wg6PluginInstance = null;
            this.wg6GetRegionManagerMethod = null;
            return;
        }

        this.available = true;
        this.regionContainer = container;
        this.regionContainerGetMethod = containerGet;
        this.bukkitAdapterAdaptMethod = adaptMethod;
        this.blockVector3AtMethod = atMethod;
        this.wg7 = detectedWg7;
        this.wg6PluginInstance = wg6Instance;
        this.wg6GetRegionManagerMethod = wg6GetRegionManager;
    }

    @Override
    public String getId() {
        return "worldguard";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public Map<String, RegionBounds> getAllRegions(World world) {
        if (!available || world == null) {
            return Collections.emptyMap();
        }
        Object manager = getRegionManagerForWorld(world);
        if (manager == null) {
            return Collections.emptyMap();
        }

        try {
            Method getRegions = findMethod(manager.getClass(), "getRegions", 0);
            Object regionsObj = getRegions.invoke(manager);
            if (!(regionsObj instanceof Map<?, ?> regionsMap) || regionsMap.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, RegionBounds> output = new HashMap<>();
            for (Map.Entry<?, ?> entry : regionsMap.entrySet()) {
                String regionId = null;
                if (entry.getKey() instanceof String key) {
                    regionId = key;
                } else if (entry.getValue() != null) {
                    Method getId = findMethod(entry.getValue().getClass(), "getId", 0);
                    Object idObj = getId.invoke(entry.getValue());
                    if (idObj instanceof String idStr) {
                        regionId = idStr;
                    }
                }
                if (regionId == null || GLOBAL_REGION_ID.equalsIgnoreCase(regionId)) {
                    continue;
                }

                RegionBounds bounds = extractBounds(entry.getValue());
                if (bounds != null) {
                    output.put(regionId.toLowerCase(Locale.ROOT), bounds);
                }
            }
            return output;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to query WorldGuard region list.", ex);
            return Collections.emptyMap();
        }
    }

    public Optional<RegionBounds> getRegionBounds(World world, String regionId) {
        if (regionId == null || regionId.isBlank()) {
            return Optional.empty();
        }
        Map<String, RegionBounds> regions = getAllRegions(world);
        if (regions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(regions.get(regionId.toLowerCase(Locale.ROOT)));
    }

    @Override
    public boolean isLocationProtected(Location location) {
        if (!available || location == null) {
            return false;
        }
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        if (wg7) {
            Object manager = getRegionManager(world);
            if (manager == null) {
                return false;
            }
            try {
                Object vector = blockVector3AtMethod.invoke(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                Method getApplicable = findMethod(manager.getClass(), "getApplicableRegions", 1);
                Object regions = getApplicable.invoke(manager, vector);
                if (regions == null) {
                    return false;
                }

                Method iteratorMethod = findMethod(regions.getClass(), "iterator", 0);
                Iterator<?> it = (Iterator<?>) iteratorMethod.invoke(regions);
                while (it.hasNext()) {
                    Object region = it.next();
                    Method getId = findMethod(region.getClass(), "getId", 0);
                    Object idObj = getId.invoke(region);
                    if (idObj instanceof String) {
                        String id = (String) idObj;
                        if (!GLOBAL_REGION_ID.equalsIgnoreCase(id)) {
                            return true;
                        }
                    }
                }
                return false;
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to query WorldGuard regions for RTP protection check.", ex);
                return false;
            }
        } else {
            // WG6 branch
            if (wg6PluginInstance == null || wg6GetRegionManagerMethod == null) {
                return false;
            }
            try {
                Object manager = wg6GetRegionManagerMethod.invoke(wg6PluginInstance, world);
                if (manager == null) {
                    return false;
                }

                // Try getApplicableRegions(Location)
                try {
                    Method getApplicable = findMethod(manager.getClass(), "getApplicableRegions", 1);
                    Object regions = getApplicable.invoke(manager, location);
                    if (regions == null) {
                        return false;
                    }
                    Method iteratorMethod = findMethod(regions.getClass(), "iterator", 0);
                    Iterator<?> it = (Iterator<?>) iteratorMethod.invoke(regions);
                    while (it.hasNext()) {
                        Object region = it.next();
                        Method getId = findMethod(region.getClass(), "getId", 0);
                        Object idObj = getId.invoke(region);
                        if (idObj instanceof String) {
                            String id = (String) idObj;
                            if (!GLOBAL_REGION_ID.equalsIgnoreCase(id)) {
                                return true;
                            }
                        }
                    }
                    return false;
                } catch (Exception ex) {
                    // Fallback to getApplicableRegions(x,y,z)
                    try {
                        Method getApplicable = findMethod(manager.getClass(), "getApplicableRegions", 3);
                        Object regions = getApplicable.invoke(manager, location.getBlockX(), location.getBlockY(), location.getBlockZ());
                        if (regions == null) {
                            return false;
                        }
                        Method iteratorMethod = findMethod(regions.getClass(), "iterator", 0);
                        Iterator<?> it = (Iterator<?>) iteratorMethod.invoke(regions);
                        while (it.hasNext()) {
                            Object region = it.next();
                            Method getId = findMethod(region.getClass(), "getId", 0);
                            Object idObj = getId.invoke(region);
                            if (idObj instanceof String) {
                                String id = (String) idObj;
                                if (!GLOBAL_REGION_ID.equalsIgnoreCase(id)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    } catch (Exception ex2) {
                        logger.log(Level.WARNING, "Failed to query WorldGuard regions for RTP protection check.", ex2);
                        return false;
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.log(Level.WARNING, "Failed to query WorldGuard regions for RTP protection check.", ex);
                return false;
            }
        }
    }

    private Object getRegionManagerForWorld(World world) {
        if (wg7) {
            return getRegionManager(world);
        }
        if (wg6PluginInstance == null || wg6GetRegionManagerMethod == null) {
            return null;
        }
        try {
            return wg6GetRegionManagerMethod.invoke(wg6PluginInstance, world);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            logger.log(Level.WARNING, "Unable to access WorldGuard region manager for world " + world.getName(), ex);
            return null;
        }
    }

    private RegionBounds extractBounds(Object region) {
        if (region == null) {
            return null;
        }
        try {
            Method getMinimumPoint = findMethod(region.getClass(), "getMinimumPoint", 0);
            Method getMaximumPoint = findMethod(region.getClass(), "getMaximumPoint", 0);
            Object minimum = getMinimumPoint.invoke(region);
            Object maximum = getMaximumPoint.invoke(region);
            if (minimum == null || maximum == null) {
                return null;
            }
            int minX = extractVectorAxis(minimum, "X");
            int minZ = extractVectorAxis(minimum, "Z");
            int maxX = extractVectorAxis(maximum, "X");
            int maxZ = extractVectorAxis(maximum, "Z");
            return new RegionBounds(Math.min(minX, maxX), Math.max(minX, maxX), Math.min(minZ, maxZ), Math.max(minZ, maxZ));
        } catch (Exception ex) {
            return null;
        }
    }

    private int extractVectorAxis(Object vector, String axis) throws InvocationTargetException, IllegalAccessException {
        try {
            Method getBlockAxis = findMethod(vector.getClass(), "getBlock" + axis, 0);
            Object value = getBlockAxis.invoke(vector);
            if (value instanceof Number number) {
                return number.intValue();
            }
        } catch (RuntimeException ignored) {
            // Fallback to getX/getZ style accessors.
        }

        Method getAxis = findMethod(vector.getClass(), "get" + axis, 0);
        Object value = getAxis.invoke(vector);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new RuntimeException("Unsupported vector axis getter: " + vector.getClass().getName() + "." + axis);
    }

    private Object getRegionManager(World world) {
        if (!wg7) {
            return null;
        }
        try {
            Object adapted = bukkitAdapterAdaptMethod.invoke(null, world);
            return regionContainerGetMethod.invoke(regionContainer, adapted);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            logger.log(Level.WARNING, "Unable to access WorldGuard region manager for world " + world.getName(), ex);
            return null;
        }
    }

    private static Method findMethod(Class<?> cls, String name, int paramCount) {
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                return m;
            }
        }
        throw new RuntimeException("Method not found: " + cls.getName() + "." + name + "(" + paramCount + " params)");
    }

    public record RegionBounds(int minX, int maxX, int minZ, int maxZ) {
        public int centerX() {
            return minX + ((maxX - minX) / 2);
        }

        public int centerZ() {
            return minZ + ((maxZ - minZ) / 2);
        }

        public int maxRadiusInsideRegion() {
            int halfWidth = Math.max(1, (maxX - minX) / 2);
            int halfLength = Math.max(1, (maxZ - minZ) / 2);
            return Math.max(1, Math.min(halfWidth, halfLength));
        }
    }
}
