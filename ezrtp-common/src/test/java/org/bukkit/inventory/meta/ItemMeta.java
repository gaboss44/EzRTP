package org.bukkit.inventory.meta;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public interface ItemMeta {
    void displayName(Component component);

    void lore(List<Component> lore);

    void setCustomModelData(Integer data);

    void addItemFlags(ItemFlag... flags);

    // Bukkit/Spigot fallback for legacy lore
    void setLore(List<String> legacyLore);

    // Bukkit/Spigot lore reader
    List<String> getLore();
}
