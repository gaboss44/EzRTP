package org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SimpleItemMeta;

public class ItemStack implements Cloneable {

    private final Material material;
    private final int amount;
        public Material getType() {
            return material;
        }

        public int getAmount() {
            return amount;
        }
    private ItemMeta meta;

    public ItemStack(Material material) {
        this(material, 1);
    }

    public ItemStack(Material material, int amount) {
        this.material = material;
        this.amount = amount;
        this.meta = new SimpleItemMeta();
    }

    public ItemMeta getItemMeta() {
        return meta;
    }

    public boolean setItemMeta(ItemMeta meta) {
        this.meta = meta;
        return true;
    }

    @Override
    public ItemStack clone() {
        ItemStack copy = new ItemStack(material, amount);
        if (meta instanceof SimpleItemMeta simpleMeta) {
            copy.meta = simpleMeta.copy();
        } else {
            copy.meta = meta;
        }
        return copy;
    }
}
