package com.skyblockexp.ezrtp.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for handling PlaceholderAPI integration.
 * Provides graceful fallback when PlaceholderAPI is not available.
 */
public final class PlaceholderUtil {

    private static boolean placeholderAPIAvailable = false;
    private static boolean checkedAvailability = false;
    private static final Map<String, String> placeholderCache = new ConcurrentHashMap<>();

    private PlaceholderUtil() {
        // Utility class
    }

    /**
     * Checks if PlaceholderAPI is available on the server.
     * 
     * @param logger Logger for warning messages
     * @return true if PlaceholderAPI is available and enabled
     */
    public static boolean isPlaceholderAPIAvailable(Logger logger) {
        if (!checkedAvailability) {
            try {
                Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                // Access Bukkit plugin manager via reflection to avoid compile-time dependency
                Object pluginManager = null;
                try {
                    Class<?> bukkitCls = Class.forName("org.bukkit.Bukkit");
                    java.lang.reflect.Method getPluginManager = bukkitCls.getMethod("getPluginManager");
                    pluginManager = getPluginManager.invoke(null);
                    if (pluginManager != null) {
                        java.lang.reflect.Method getPlugin = pluginManager.getClass().getMethod("getPlugin", String.class);
                        Object papi = getPlugin.invoke(pluginManager, "PlaceholderAPI");
                        if (papi != null) {
                            try {
                                java.lang.reflect.Method isEnabled = papi.getClass().getMethod("isEnabled");
                                Object enabled = isEnabled.invoke(papi);
                                placeholderAPIAvailable = Boolean.TRUE.equals(enabled);
                            } catch (Throwable ignored) {
                                placeholderAPIAvailable = true;
                            }
                        } else {
                            placeholderAPIAvailable = false;
                        }
                    }
                } catch (Throwable t) {
                    placeholderAPIAvailable = false;
                }
                if (placeholderAPIAvailable) {
                    logger.info("PlaceholderAPI found and enabled. Placeholder support is available.");
                } else {
                    logger.info("PlaceholderAPI not found. Placeholder resolution will use fallback behavior.");
                }
            } catch (Exception e) {
                placeholderAPIAvailable = false;
                logger.info("PlaceholderAPI not available. Placeholder resolution will use fallback behavior.");
            }
            checkedAvailability = true;
        }
        return placeholderAPIAvailable;
    }

    /**
     * Resolves placeholders in the given text using PlaceholderAPI if available.
     * If PlaceholderAPI is not available, returns the text as-is.
     * 
     * @param player The player context for placeholder resolution
     * @param text   The text containing placeholders to resolve
     * @param logger Logger for debug messages
     * @return The text with placeholders resolved, or original text if PAPI is not available or text is null
     */
    public static String resolvePlaceholders(Object player, String text, Logger logger) {
        if (text == null) {
            return null;
        }
        if (!isPlaceholderAPIAvailable(logger)) {
            return text;
        }

        String cacheKey = (player != null ? player.toString() : "null") + ":" + text;
        String cached = placeholderCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            Class<?> cls = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method method = cls.getMethod("setPlaceholders", Class.forName("org.bukkit.entity.Player"), String.class);
            String resolved = (String) method.invoke(null, player, text);
            placeholderCache.put(cacheKey, resolved);
            return resolved;
        } catch (Exception e) {
            logger.warning("Failed to resolve placeholders: " + e.getMessage());
            return text;
        }
    }

    /**
     * Resolves placeholders in the given text without player context.
     * This is useful for offline player placeholders or server-wide placeholders.
     * 
     * @param text   The text containing placeholders to resolve
     * @param logger Logger for debug messages
     * @return The text with placeholders resolved, or original text if PAPI is not available or text is null
     */
    public static String resolvePlaceholders(String text, Logger logger) {
        if (text == null) {
            return null;
        }
        
        if (!isPlaceholderAPIAvailable(logger)) {
            return text;
        }

        try {
            Class<?> cls = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Class<?> playerCls = Class.forName("org.bukkit.entity.Player");
            java.lang.reflect.Method method = cls.getMethod("setPlaceholders", playerCls, String.class);
            return (String) method.invoke(null, null, text);
        } catch (Exception e) {
            logger.warning("Failed to resolve placeholders: " + e.getMessage());
            return text;
        }
    }

    /**
     * Formats seconds into a human-readable time string (e.g., "1h 30m 45s").
     * 
     * @param totalSeconds The total number of seconds to format
     * @return A human-readable time string
     */
    public static String formatTime(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append("s");
        }

        return result.toString().trim();
    }

    /**
     * Resets the availability check for testing purposes.
     */
    public static void resetAvailabilityCheck() {
        checkedAvailability = false;
        placeholderAPIAvailable = false;
        placeholderCache.clear();
    }
}
