package com.skyblockexp.ezrtp.util.compat;

import net.kyori.adventure.text.Component;
import java.lang.reflect.Method;

/**
 * TextRenderer was removed and its functionality migrated into
 * com.skyblockexp.ezrtp.util.MessageUtil. This stub remains only to
 * avoid accidental compile-time references; it will throw if used at runtime.
 */
public final class TextRenderer {

    private static boolean HAS_MINIMESSAGE;
    private static Object MINI_MESSAGE_INSTANCE;
    private static Method MINI_PARSE_METHOD;
    private static Method MINI_SERIALIZE_METHOD;
    private static Object LEGACY_SERIALIZER_INSTANCE;
    private static Method LEGACY_DESERIALIZE_METHOD;
    private static Method LEGACY_SERIALIZE_METHOD;
    private static boolean FORCE_LEGACY_COLORS = false;

    static {
        Object mmInst = null;
        Method mmParse = null;
        boolean hasMm = false;

        Object legacyInst = null;
        Method legacyDeserialize = null;
        Method legacySerialize = null;

        try {
            Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Method miniMessageFactory = mmClass.getMethod("miniMessage");
            mmInst = miniMessageFactory.invoke(null);
            // Try common API names: prefer 'deserialize' but fall back to older 'parse'
            try {
                mmParse = mmClass.getMethod("deserialize", String.class);
            } catch (Throwable t) {
                mmParse = mmClass.getMethod("parse", String.class);
            }
            hasMm = mmInst != null && mmParse != null;
        } catch (Throwable ignored) {
            hasMm = false;
        }

        try {
            Class<?> legacyClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Method legacyFactory = legacyClass.getMethod("legacySection");
            legacyInst = legacyFactory.invoke(null);
            legacyDeserialize = legacyInst.getClass().getMethod("deserialize", String.class);
            legacySerialize = legacyInst.getClass().getMethod("serialize", Component.class);
        } catch (Throwable ignored) {
            // leave nulls
        }

        Method mmSerialize = null;
        if (hasMm && mmInst != null) {
            try {
                Class<?> mmClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
                mmSerialize = mmClass.getMethod("serialize", Component.class);
            } catch (Throwable ignored) {
                mmSerialize = null;
            }
        }

        HAS_MINIMESSAGE = hasMm;
        MINI_MESSAGE_INSTANCE = mmInst;
        MINI_PARSE_METHOD = mmParse;
        MINI_SERIALIZE_METHOD = mmSerialize;

        LEGACY_SERIALIZER_INSTANCE = legacyInst;
        LEGACY_DESERIALIZE_METHOD = legacyDeserialize;
        LEGACY_SERIALIZE_METHOD = legacySerialize;
    }

    public static Component parse(String input) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }
        // Prefer MiniMessage if available; otherwise use legacy serializer; otherwise return plain text.
        if (HAS_MINIMESSAGE && MINI_PARSE_METHOD != null && MINI_MESSAGE_INSTANCE != null) {
            try {
                Object parsed = MINI_PARSE_METHOD.invoke(MINI_MESSAGE_INSTANCE, input);
                if (parsed instanceof Component c) return c;
            } catch (Throwable ignored) {
            }
        }
        if (LEGACY_SERIALIZER_INSTANCE != null && LEGACY_DESERIALIZE_METHOD != null) {
            try {
                Object parsed = LEGACY_DESERIALIZE_METHOD.invoke(LEGACY_SERIALIZER_INSTANCE, input);
                if (parsed instanceof Component c) return c;
            } catch (Throwable ignored) {
            }
        }
        return Component.text(input);
    }

    public static String serialize(Component component) {
        if (component == null) return "";
        // Prefer a proper legacy serializer when available (produces § color codes)
        if (LEGACY_SERIALIZER_INSTANCE != null && LEGACY_SERIALIZE_METHOD != null) {
            try {
                Object out = LEGACY_SERIALIZE_METHOD.invoke(LEGACY_SERIALIZER_INSTANCE, component);
                if (out instanceof String s) return s;
            } catch (Throwable ignored) {
            }
        }

        // If no legacy serializer, try MiniMessage serialize followed by conversion
        if (MINI_MESSAGE_INSTANCE != null && MINI_SERIALIZE_METHOD != null) {
            try {
                Object mmOut = MINI_SERIALIZE_METHOD.invoke(MINI_MESSAGE_INSTANCE, component);
                if (mmOut instanceof String mmString) {
                    String legacy = miniMessageToLegacy(mmString);
                    if (legacy != null && !legacy.isBlank()) return legacy;
                    return mmString;
                }
            } catch (Throwable ignored) {
            }
        }

        // Fallback to plain text
        try {
            String s = component.toString();
            return s == null ? "" : s;
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String miniMessageToLegacy(String mm) {
        if (mm == null || mm.isBlank()) return mm;
        // Basic replacements for common color/style tags used in tests
        String out = mm;
        // Handle gradients: convert <gradient:#aabbcc:#ddeeff>text</gradient>
        try {
            java.util.regex.Pattern gradPat = java.util.regex.Pattern.compile("(?i)<gradient:([^>]+)>(.*?)</gradient>", java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher gm = gradPat.matcher(out);
            StringBuffer gsb = new StringBuffer();
            while (gm.find()) {
                String colors = gm.group(1);
                String inner = gm.group(2);
                String firstHex = null;
                java.util.regex.Matcher hexFinder = java.util.regex.Pattern.compile("#([0-9a-fA-F]{6})").matcher(colors);
                if (hexFinder.find()) firstHex = hexFinder.group(1);
                String replacement;
                if (firstHex != null) {
                    replacement = hexToNearestLegacy(firstHex) + inner + "\u00A7r";
                } else {
                    replacement = inner; // no hex found, just strip
                }
                gm.appendReplacement(gsb, java.util.regex.Matcher.quoteReplacement(replacement));
            }
            gm.appendTail(gsb);
            out = gsb.toString();
        } catch (Throwable ignored) {}
        // Convert hex color tags like <#rrggbb> to nearest legacy if forced
        if (FORCE_LEGACY_COLORS) {
            try {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("<#([0-9a-fA-F]{6})>").matcher(out);
                StringBuffer sb = new StringBuffer();
                while (m.find()) {
                    String hex = m.group(1);
                    String code = hexToNearestLegacy(hex);
                    m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(code));
                }
                m.appendTail(sb);
                out = sb.toString();
                // strip closing generic tags like </#...> or </>
                out = out.replaceAll("(?i)</#?[0-9a-fA-F]{0,6}>", "\u00A7r");
            } catch (Throwable ignored) {
            }
        }

        // Also handle simple <gradient> without explicit colors: <gradient>text</gradient>
        try {
            out = out.replaceAll("(?i)<gradient>", "");
            out = out.replaceAll("(?i)</gradient>", "\u00A7r");
        } catch (Throwable ignored) {}
        // colors
        out = out.replaceAll("(?i)<black>", "\u00A70");
        out = out.replaceAll("(?i)</black>", "\u00A7r");
        out = out.replaceAll("(?i)<dark_blue>", "\u00A71");
        out = out.replaceAll("(?i)</dark_blue>", "\u00A7r");
        out = out.replaceAll("(?i)<dark_green>", "\u00A72");
        out = out.replaceAll("(?i)</dark_green>", "\u00A7r");
        out = out.replaceAll("(?i)<dark_aqua>", "\u00A73");
        out = out.replaceAll("(?i)</dark_aqua>", "\u00A7r");
        out = out.replaceAll("(?i)<dark_red>", "\u00A74");
        out = out.replaceAll("(?i)</dark_red>", "\u00A7r");
        out = out.replaceAll("(?i)<dark_purple>", "\u00A75");
        out = out.replaceAll("(?i)</dark_purple>", "\u00A7r");
        out = out.replaceAll("(?i)<gold>", "\u00A76");
        out = out.replaceAll("(?i)</gold>", "\u00A7r");
        out = out.replaceAll("(?i)<gray>", "\u00A77");
        out = out.replaceAll("(?i)</gray>", "\u00A7r");
        out = out.replaceAll("(?i)<dark_gray>", "\u00A78");
        out = out.replaceAll("(?i)</dark_gray>", "\u00A7r");
        out = out.replaceAll("(?i)<blue>", "\u00A79");
        out = out.replaceAll("(?i)</blue>", "\u00A7r");
        out = out.replaceAll("(?i)<green>", "\u00A7a");
        out = out.replaceAll("(?i)</green>", "\u00A7r");
        out = out.replaceAll("(?i)<aqua>", "\u00A7b");
        out = out.replaceAll("(?i)</aqua>", "\u00A7r");
        out = out.replaceAll("(?i)<red>", "\u00A7c");
        out = out.replaceAll("(?i)</red>", "\u00A7r");
        out = out.replaceAll("(?i)<light_purple>", "\u00A7d");
        out = out.replaceAll("(?i)</light_purple>", "\u00A7r");
        out = out.replaceAll("(?i)<yellow>", "\u00A7e");
        out = out.replaceAll("(?i)</yellow>", "\u00A7r");
        out = out.replaceAll("(?i)<white>", "\u00A7f");
        out = out.replaceAll("(?i)</white>", "\u00A7r");
        // styles
        out = out.replaceAll("(?i)<bold>", "\u00A7l");
        out = out.replaceAll("(?i)</bold>", "\u00A7r");
        out = out.replaceAll("(?i)<italic>", "\u00A7o");
        out = out.replaceAll("(?i)</italic>", "\u00A7r");
        out = out.replaceAll("(?i)<underlined>", "\u00A7n");
        out = out.replaceAll("(?i)</underlined>", "\u00A7r");
        out = out.replaceAll("(?i)<strikethrough>", "\u00A7m");
        out = out.replaceAll("(?i)</strikethrough>", "\u00A7r");
        out = out.replaceAll("(?i)<reset>", "\u00A7r");
        return out;
    }

    private static String hexToNearestLegacy(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            int[][] legacy = new int[][]{
                    {0, 0, 0},       // black
                    {0, 0, 170},     // dark_blue
                    {0, 170, 0},     // dark_green
                    {0, 170, 170},   // dark_aqua
                    {170, 0, 0},     // dark_red
                    {170, 0, 170},   // dark_purple
                    {255, 170, 0},   // gold
                    {170, 170, 170}, // gray
                    {85, 85, 85},    // dark_gray
                    {85, 85, 255},   // blue
                    {85, 255, 85},   // green
                    {85, 255, 255},  // aqua
                    {255, 85, 85},   // red
                    {255, 85, 255},  // light_purple
                    {255, 255, 85},  // yellow
                    {255, 255, 255}  // white
            };
            char[] codes = new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
            double best = Double.MAX_VALUE;
            int idx = 15;
            for (int i = 0; i < legacy.length; i++) {
                int lr = legacy[i][0], lg = legacy[i][1], lb = legacy[i][2];
                double d = Math.pow(r - lr, 2) + Math.pow(g - lg, 2) + Math.pow(b - lb, 2);
                if (d < best) { best = d; idx = i; }
            }
            return "\u00A7" + codes[idx];
        } catch (Throwable t) {
            return "";
        }
    }

    /**
     * Force legacy color conversion for hex/gradient tags when true.
     */
    public static void setForceLegacyColors(boolean v) { FORCE_LEGACY_COLORS = v; }

    // Note: BukkitAudiences reflection support removed; platform message service registry
    // should be used for delivering Adventure Components to audiences.

    /**
     * Public wrapper to convert a MiniMessage string to a legacy color string.
     * Useful when you need to send a raw MiniMessage template as legacy text
     * (avoids Component classloader mismatches between server and shaded API).
     */
    public static String miniMessageToLegacyString(String miniMessage) {
        if (miniMessage == null) return "";
        // Prefer proper MiniMessage parse -> legacy serialize when available
        try {
            if (HAS_MINIMESSAGE && MINI_PARSE_METHOD != null && LEGACY_SERIALIZE_METHOD != null && MINI_MESSAGE_INSTANCE != null && LEGACY_SERIALIZER_INSTANCE != null) {
                Object comp = MINI_PARSE_METHOD.invoke(MINI_MESSAGE_INSTANCE, miniMessage);
                if (comp != null) {
                    Object out = LEGACY_SERIALIZE_METHOD.invoke(LEGACY_SERIALIZER_INSTANCE, comp);
                    if (out instanceof String s && s != null) return s;
                }
            }
        } catch (Throwable ignored) {
        }
        String converted = miniMessageToLegacy(miniMessage);
        if (converted == null) return "";
        return converted;
    }
}
