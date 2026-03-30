package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class BukkitPlatformGuiBridge implements PlatformGuiBridge {

    private static final int MAX_TITLE_LENGTH = 32;

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, Component title) {
        String legacyTitle = com.skyblockexp.ezrtp.util.MessageUtil.componentToLegacy(title);
        String safeTitle = legacyTitle == null ? "" : legacyTitle;
        if (safeTitle.length() > MAX_TITLE_LENGTH) {
            safeTitle = safeTitle.substring(0, MAX_TITLE_LENGTH);
        }
        return Bukkit.createInventory(holder, size, safeTitle);
    }

    @Override
    public void setDisplayName(ItemMeta meta, Component displayName) {
        if (meta == null || displayName == null) {
            return;
        }
        String legacy = com.skyblockexp.ezrtp.util.MessageUtil.componentToLegacy(displayName);
        meta.setDisplayName(legacy == null ? "" : legacy);
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> lore) {
        if (meta == null) {
            return;
        }
        List<Component> safeLore = lore != null ? lore : List.of();
        List<String> legacy = new ArrayList<>(safeLore.size());
        for (Component component : safeLore) {
            legacy.add(com.skyblockexp.ezrtp.util.MessageUtil.componentToLegacy(component));
        }
        meta.setLore(legacy);
    }

    @Override
    public void applyItemMeta(ItemStack icon, ItemMeta meta) {
        if (icon != null && meta != null) {
            icon.setItemMeta(meta);
        }
    }
}
