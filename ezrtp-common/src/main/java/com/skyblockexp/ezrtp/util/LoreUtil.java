package com.skyblockexp.ezrtp.util;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import com.skyblockexp.ezrtp.util.compat.ItemMetaCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class for handling item lore and display name formatting with MiniMessage support.
 * Provides compatibility between Paper (Component-based) and Spigot/Bukkit (legacy string-based) APIs.
 */
public final class LoreUtil {

    
    private LoreUtil() {
        // Utility class
    }

    /**
     * Sets the lore on the given ItemMeta, resolving placeholders and handling MiniMessage formatting.
     * Compatible with both Paper and Spigot APIs.
     *
     * @param meta The ItemMeta to modify
     * @param rawLore The raw lore lines with MiniMessage formatting and placeholders
     * @param player The player for placeholder resolution
     * @param logger Logger for warnings
     */
    @SuppressWarnings("deprecation")
    public static void setLore(ItemMeta meta, List<String> rawLore, Player player, Logger logger) {
        if (rawLore.isEmpty()) {
            logger.info("LoreUtil.setLore: rawLore is empty, skipping");
            return;
        }

        logger.info("LoreUtil.setLore: rawLore = " + rawLore);
        logger.info("LoreUtil.setLore: hasMethod = " + hasMethod(meta.getClass(), "lore", List.class));

        if (hasMethod(meta.getClass(), "lore", List.class)) {
            // Paper API - use Component lore
            logger.info("LoreUtil.setLore: Using Paper API");
            List<Component> lore = new ArrayList<>();
            for (String line : rawLore) {
                String resolvedLine = PlaceholderUtil.resolvePlaceholders(player, line, logger);
                lore.add(MessageUtil.parseMiniMessage(resolvedLine));
            }
            ItemMetaCompat.setLore(meta, lore);
            logger.info("LoreUtil.setLore: Set Paper lore: " + lore);
        } else {
            // Spigot/Bukkit - deserialize MiniMessage to Component and serialize to legacy strings
            logger.info("LoreUtil.setLore: Using Spigot API");
            List<String> resolvedLore = new ArrayList<>();
            for (String line : rawLore) {
                String resolvedLine = PlaceholderUtil.resolvePlaceholders(player, line, logger);
                logger.info("LoreUtil.setLore: resolvedLine = " + resolvedLine);
                Component component = MessageUtil.parseMiniMessage(resolvedLine);
                String legacy = MessageUtil.componentToLegacy(component);
                logger.info("LoreUtil.setLore: legacy = " + legacy);
                resolvedLore.add(legacy);
            }
            ItemMetaCompat.setLore(meta, loreFromComponents(resolvedLore));
            logger.info("LoreUtil.setLore: final lore = " + resolvedLore);
            logger.info("LoreUtil.setLore: meta.hasLore() = " + meta.hasLore());
            if (meta.hasLore()) {
                logger.info("LoreUtil.setLore: meta.getLore() = " + meta.getLore());
            }
        }
    }

    /**
     * Sets the display name on the given ItemMeta, resolving placeholders and handling MiniMessage formatting.
     * Compatible with both Paper and Spigot APIs.
     *
     * @param meta The ItemMeta to modify
     * @param rawDisplayName The raw display name with MiniMessage formatting and placeholders
     * @param player The player for placeholder resolution
     * @param logger Logger for warnings
     */
    public static void setDisplayName(ItemMeta meta, String rawDisplayName, Player player, Logger logger) {
        if (rawDisplayName == null || rawDisplayName.isBlank()) {
            return;
        }

        String resolvedName = PlaceholderUtil.resolvePlaceholders(player, rawDisplayName, logger);
        Component component = MessageUtil.parseMiniMessage(resolvedName);

        ItemMetaCompat.setDisplayName(meta, component);

    }

    private static List<Component> loreFromComponents(List<String> legacyLines) {
        List<Component> list = new ArrayList<>();
        for (String s : legacyLines) {
            if (s == null) {
                list.add(Component.empty());
            } else {
                String amp = s.replace('\u00A7', '&');
                list.add(MessageUtil.legacyToComponent(amp));
            }
        }
        return list;
    }

    /**
     * Validates a MiniMessage string by attempting to deserialize it.
     * Logs a warning if invalid.
     *
     * @param miniMessageString The string to validate
     * @param context Description of the context for logging
     * @param logger Logger for warnings
     * @return true if valid, false if invalid
     */
    public static boolean validateMiniMessage(String miniMessageString, String context, Logger logger) {
        if (miniMessageString == null || miniMessageString.isBlank()) {
            return true;
        }
        try {
            MessageUtil.parseMiniMessage(miniMessageString);
            return true;
        } catch (Exception e) {
            logger.warning("Invalid MiniMessage in " + context + ": '" + miniMessageString + "' - " + e.getMessage());
            return false;
        }
    }

    private static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
