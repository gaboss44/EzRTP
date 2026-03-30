package com.skyblockexp.ezrtp.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Compatibility helpers for block-related API differences across Bukkit versions.
 */
public final class BlockCompat {

    private BlockCompat() {}

    /**
     * Returns whether the given block is passable. Uses the modern {@code Block#isPassable}
     * when available, otherwise falls back to a conservative check using {@code Material.isSolid()}.
     */
    public static boolean isPassable(Block block) {
        if (block == null) return true;

        // Try calling the modern method reflectively
        try {
            Method m = block.getClass().getMethod("isPassable");
            Object res = m.invoke(block);
            if (res instanceof Boolean) return (Boolean) res;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | SecurityException ignored) {
            // fall through to legacy fallback
        }

        // Legacy fallback: consider a block passable when its material is not solid or is AIR
        Material type = block.getType();
        if (type == null) return true;
        try {
            return !type.isSolid() || type == Material.AIR;
        } catch (NoSuchMethodError e) {
            // Older Material implementations should still have enum constants; treat AIR as passable
            return type == Material.AIR;
        }
    }
}
