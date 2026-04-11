package com.skyblockexp.ezrtp.util.compat;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for `ItemMeta` display name / lore methods across server versions.
 */
public final class ItemMetaCompat {

    private ItemMetaCompat() {}

    public static void setDisplayName(ItemMeta meta, Component component) {
        if (meta == null || component == null) return;
        // If the component serializes to MiniMessage containing gradient/hex tags,
        // prefer storing a legacy string instead to avoid raw MiniMessage tags
        // appearing in environments that later serialize components back to MiniMessage.
        try {
            String mmPreview = com.skyblockexp.ezrtp.util.MessageUtil.serializeToMiniMessage(component);
            if (mmPreview != null) {
                boolean hasGradient = mmPreview.toLowerCase().contains("<gradient");
                boolean hasHex = java.util.regex.Pattern.compile("(?i)<#([0-9a-fA-F]{1,6})>").matcher(mmPreview).find();
                if (hasGradient || hasHex) {
                    // Convert to a plain-text display to avoid gradient/hex tags entirely.
                    String legacy = com.skyblockexp.ezrtp.util.MessageUtil.componentToLegacy(component);
                    // Remove hex-style section sequences like §#rrggbb then strip remaining section codes
                    String withoutHex = legacy.replaceAll("(?i)\\u00A7#[0-9a-fA-F]{6}", "");
                    String plain = com.skyblockexp.ezrtp.util.MessageUtil.stripColors(withoutHex);
                    Component plainComponent = Component.text(plain);
                    // Try to set a plain-text component on the runtime meta first.
                    for (Method m : meta.getClass().getMethods()) {
                        String name = m.getName();
                        if (!name.equals("displayName") && !name.equals("setDisplayName")) continue;
                        Class<?>[] params = m.getParameterTypes();
                        if (params.length != 1) continue;
                        try {
                            m.invoke(meta, plainComponent);
                            return;
                        } catch (IllegalArgumentException | InvocationTargetException iae) {
                            // ignore and continue to next candidate
                            continue;
                        } catch (ReflectiveOperationException | LinkageError ignored) {
                            continue;
                        }
                    }
                    // Fallback to string-based if component-set didn't work
                    try {
                        Method set = meta.getClass().getMethod("setDisplayName", String.class);
                        set.invoke(meta, plain);
                        return;
                    } catch (ReflectiveOperationException ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        // Try to find any method named displayName or setDisplayName with a single parameter
        // and attempt to invoke it with the provided Component. We avoid depending on the
        // compile-time Component class to tolerate classloader relocation/shading.
        for (Method m : meta.getClass().getMethods()) {
            String name = m.getName();
            if (!name.equals("displayName") && !name.equals("setDisplayName")) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            try {
                m.invoke(meta, component);
                return;
            } catch (IllegalArgumentException | InvocationTargetException iae) {
                // argument type mismatch (likely different Component classloader)
                // Attempt to convert our Component into the target parameter type by
                // serializing to MiniMessage and deserializing using the parameter's
                // classloader (if MiniMessage is available there).
                try {
                    String mm = com.skyblockexp.ezrtp.util.MessageUtil.serializeToMiniMessage(component);
                    ClassLoader loader = safeClassLoader(meta.getClass().getClassLoader());
                    Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage", true, loader);
                    java.lang.reflect.Method factory = mmClass.getMethod("miniMessage");
                    Object mmInst = factory.invoke(null);
                    java.lang.reflect.Method deser = mmClass.getMethod("deserialize", String.class);
                    Object targetComp = deser.invoke(mmInst, mm);
                    m.invoke(meta, targetComp);
                    return;
                } catch (Throwable ex) {
                    // conversion failed, try other candidate methods
                    continue;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // other reflection issues - try other candidates
            }
        }

        // Fallback to string-based display name using the plugin's legacy serializer
        try {
            Method set = meta.getClass().getMethod("setDisplayName", String.class);
            set.invoke(meta, com.skyblockexp.ezrtp.util.MessageUtil.componentToLegacy(component));
        } catch (ReflectiveOperationException ignored) {}
    }

    @SuppressWarnings("deprecation")
    public static void setLore(ItemMeta meta, List<Component> lore) {
        if (meta == null) return;
        if (lore == null) {
            try { meta.setLore(null); } catch (Exception ignored) {}
            return;
        }
        // Prefer calling `lore(List<Component>)` when available on the ItemMeta mock/type.
        for (Method m : meta.getClass().getMethods()) {
            if (!m.getName().equals("lore")) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            try {
                m.invoke(meta, lore);
                return;
            } catch (IllegalArgumentException | InvocationTargetException iae) {
                // Try to convert into the runtime component type and invoke
                try {
                    List<Object> converted = convertLoreForRuntime(lore, meta.getClass().getClassLoader());
                    m.invoke(meta, converted);
                    return;
                } catch (Throwable conv) {
                    continue;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                continue;
            }
        }

        // If no `lore(...)` method handled it, try `setLore(...)` overloads next
        for (Method m : meta.getClass().getMethods()) {
            if (!m.getName().equals("setLore")) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            try {
                m.invoke(meta, lore);
                return;
            } catch (IllegalArgumentException | InvocationTargetException iae) {
                try {
                    List<Object> converted = convertLoreForRuntime(lore, meta.getClass().getClassLoader());
                    m.invoke(meta, converted);
                    return;
                } catch (Throwable conv) {
                    continue;
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                continue;
            }
        }

        // Fallback to legacy String lore using the plugin's legacy serializer
        List<String> legacy = new ArrayList<>(lore.size());
        for (Component c : lore) {
            legacy.add(com.skyblockexp.ezrtp.util.MessageUtil.componentToLegacy(c));
        }
        // Try common setLore signatures
        trySetLoreString(meta, legacy);
    }

    private static void trySetLoreString(ItemMeta meta, List<String> legacy) {
        for (Method m : meta.getClass().getMethods()) {
            if (!m.getName().equals("setLore")) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            try {
                m.invoke(meta, legacy);
                return;
            } catch (IllegalArgumentException iae) {
                // type mismatch, continue
                continue;
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // continue searching
            }
        }
    }

    private static List<Object> convertLoreForRuntime(List<Component> lore, ClassLoader loader) throws Exception {
        ClassLoader safeLoader = safeClassLoader(loader);
        Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage", true, safeLoader);
        java.lang.reflect.Method factory = mmClass.getMethod("miniMessage");
        Object mmInst = factory.invoke(null);
        java.lang.reflect.Method deser = mmClass.getMethod("deserialize", String.class);
        List<Object> converted = new ArrayList<>(lore.size());
        for (Component c : lore) {
            String mm = com.skyblockexp.ezrtp.util.MessageUtil.serializeToMiniMessage(c);
            converted.add(deser.invoke(mmInst, mm));
        }
        return converted;
    }

    private static ClassLoader safeClassLoader(ClassLoader loader) {
        return loader != null ? loader : ItemMetaCompat.class.getClassLoader();
    }
}
