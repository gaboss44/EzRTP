package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.ItemMetaCompat;

public class PaperPlatformGuiBridge implements PlatformGuiBridge {

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Component title) {
        try {
            return Bukkit.createInventory(holder, size, title);
        } catch (ClassCastException ex) {
            String legacyTitle = MessageUtil.componentToLegacy(title);
            String safeTitle = legacyTitle == null ? "" : legacyTitle;
            if (safeTitle.length() > 32) {
                safeTitle = safeTitle.substring(0, 32);
            }
            return Bukkit.createInventory(holder, size, safeTitle);
        }
    }

    @Override
    public void setDisplayName(ItemMeta meta, Component displayName) {
        if (meta != null && displayName != null) {
            ItemMetaCompat.setDisplayName(meta, displayName);
        }
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> lore) {
        if (meta != null) {
            ItemMetaCompat.setLore(meta, lore != null ? lore : List.of());
        }
    }

    @Override
    public void applyItemMeta(ItemStack icon, ItemMeta meta) {
        if (icon != null && meta != null) {
            icon.setItemMeta(meta);
        }
    }
}
