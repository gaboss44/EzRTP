package com.skyblockexp.ezrtp.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Thin wrapper that delegates ItemFlag-related operations to a reflection-based
 * compatibility implementation so the plugin can run on runtimes without
 * `org.bukkit.inventory.ItemFlag`.
 */
public final class ItemFlagUtil {

    private ItemFlagUtil() {
    }

    public static void applyStandardHideFlags(ItemMeta meta) {
        ItemFlagCompat.applyStandardHideFlags(meta);
    }

    public static void setItemMetaCompatibly(ItemStack itemStack, ItemMeta meta) {
        ItemFlagCompat.setItemMetaCompatibly(itemStack, meta);
    }
}
