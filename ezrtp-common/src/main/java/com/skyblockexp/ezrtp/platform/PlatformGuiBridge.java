package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Bridge for GUI operations that vary by platform/runtime.
 */
public interface PlatformGuiBridge {

    Inventory createInventory(InventoryHolder holder, int size, Component title);

    void setDisplayName(ItemMeta meta, Component displayName);

    void setLore(ItemMeta meta, List<Component> lore);

    void applyItemMeta(ItemStack icon, ItemMeta meta);
}
