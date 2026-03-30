package org.bukkit;

import java.util.Locale;

public enum Material implements Keyed {
    GRAY_STAINED_GLASS_PANE,
    GRASS_BLOCK,
    DIRT,
    NETHERRACK,
    END_STONE,
    ENDER_PEARL,
    DIAMOND_SWORD;

    public static Material matchMaterial(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return Material.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Compatibility shim used by some mocked/platform classes during tests.
     * Real Bukkit enums expose this on newer platforms; return false in tests.
     */
    public boolean isLegacy() {
        return false;
    }

    @Override
    public NamespacedKey getKey() {
        return new NamespacedKey("test:" + this.name().toLowerCase());
    }
}
