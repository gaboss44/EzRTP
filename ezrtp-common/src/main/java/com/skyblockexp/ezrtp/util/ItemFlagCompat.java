package com.skyblockexp.ezrtp.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reflection-based compatibility helper for org.bukkit.inventory.ItemFlag.
 * All access to the ItemFlag enum is done reflectively so classes that run
 * on older servers (without ItemFlag) won't fail to load.
 */
public final class ItemFlagCompat {

    private static final Set<String> SAFE_FLAGS = Set.of(
            "HIDE_ENCHANTS",
            "HIDE_ATTRIBUTES",
            "HIDE_UNBREAKABLE",
            "HIDE_DESTROYS",
            "HIDE_PLACED_ON",
            "HIDE_POTION_EFFECTS",
            "HIDE_DYE",
            "HIDE_ARMOR_TRIM"
    );

    private ItemFlagCompat() {
    }

    public static void applyStandardHideFlags(ItemMeta meta) {
        if (meta == null) return;

        try {
            Class<?> itemFlagClass = Class.forName("org.bukkit.inventory.ItemFlag");
            Object[] constants = itemFlagClass.getEnumConstants();
            if (constants == null || constants.length == 0) return;

            List<Object> toAdd = new ArrayList<>();
            for (Object c : constants) {
                if (c instanceof Enum<?> && SAFE_FLAGS.contains(((Enum<?>) c).name())) {
                    toAdd.add(c);
                }
            }
            if (toAdd.isEmpty()) return;

            // Find the addItemFlags method that accepts an array of the enum type
            Method addItemFlagsMethod = null;
            for (Method m : meta.getClass().getMethods()) {
                if (!m.getName().equals("addItemFlags")) continue;
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 1 && params[0].isArray() && params[0].getComponentType().equals(itemFlagClass)) {
                    addItemFlagsMethod = m;
                    break;
                }
            }
            if (addItemFlagsMethod == null) return;

            Object arr = Array.newInstance(itemFlagClass, toAdd.size());
            for (int i = 0; i < toAdd.size(); i++) {
                Array.set(arr, i, toAdd.get(i));
            }

            // Invoke with single array argument (varargs)
            addItemFlagsMethod.invoke(meta, new Object[]{arr});
        } catch (ClassNotFoundException e) {
            // ItemFlag doesn't exist on this runtime - gracefully do nothing
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Swallow reflection failures; nothing we can do safely at runtime
        }
    }

    public static void setItemMetaCompatibly(ItemStack itemStack, ItemMeta meta) {
        if (itemStack == null) return;
        try {
            Method setItemMetaMethod = itemStack.getClass().getMethod("setItemMeta", ItemMeta.class);
            Object result = setItemMetaMethod.invoke(itemStack, meta);
            if (result instanceof Boolean && !(Boolean) result) {
                // Some implementations return false to indicate failure. Try the
                // older/void-returning overload as a fallback instead of throwing,
                // to avoid leaving the ItemStack without applied metadata.
                try {
                    Method oldSetItemMetaMethod = itemStack.getClass().getMethod("setItemMeta", ItemMeta.class);
                    oldSetItemMetaMethod.invoke(itemStack, meta);
                    return;
                } catch (Exception fallbackException) {
                    // If fallback also fails, swallow and allow outer catch to handle.
                }
                return;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Fallback for older API versions where setItemMeta returns void
            try {
                Method oldSetItemMetaMethod = itemStack.getClass().getMethod("setItemMeta", ItemMeta.class);
                oldSetItemMetaMethod.invoke(itemStack, meta);
            } catch (Exception fallbackException) {
                throw new RuntimeException("Failed to set item meta", fallbackException);
            }
        }
    }
}
