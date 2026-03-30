package org.bukkit.inventory.meta;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SimpleItemMeta implements ItemMeta {

    private Component displayName;
    private List<Component> lore = new ArrayList<>();
    private List<String> legacyLore = new ArrayList<>();
    private Integer customModelData;
    private List<ItemFlag> flags = new ArrayList<>();

    @Override
    public void displayName(Component component) {
        this.displayName = component;
    }

    // Getter used by tests that inspect the display name via reflection
    public Component displayName() {
        return this.displayName;
    }

    @Override
    public void lore(List<Component> lore) {
        this.lore = new ArrayList<>(lore);
    }

    @Override
    public void setCustomModelData(Integer data) {
        this.customModelData = data;
    }

    @Override
    public void addItemFlags(ItemFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
    }

    // Bukkit/Spigot fallback for legacy lore
    @Override
    public void setLore(List<String> legacyLore) {
        this.legacyLore = new ArrayList<>(legacyLore);
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(legacyLore);
    }

    public SimpleItemMeta copy() {
        SimpleItemMeta copy = new SimpleItemMeta();
        copy.displayName = this.displayName;
        copy.lore = new ArrayList<>(this.lore);
        copy.legacyLore = new ArrayList<>(this.legacyLore);
        copy.customModelData = this.customModelData;
        copy.flags = new ArrayList<>(this.flags);
        return copy;
    }
}
