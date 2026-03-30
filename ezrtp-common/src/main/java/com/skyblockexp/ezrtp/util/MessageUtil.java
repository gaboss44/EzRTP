package com.skyblockexp.ezrtp.util;

import com.skyblockexp.ezrtp.message.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized message utilities: MiniMessage <-> legacy handling and
 * helper methods for sending/broadcasting/logging plugin messages.
 */
public final class MessageUtil {

        private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
        private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .character('&')
            .build();
        private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .character('\u00A7')
            .build();
    private static final Pattern MINIMESSAGE_TAG_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(:.*)?");

    private static String prefix = "";
    private static final Logger LOGGER = Logger.getLogger("EzRTP");

    private MessageUtil() {}

    // Force legacy conversion for hex/gradient tags when true (used by older servers/clients)
    private static boolean FORCE_LEGACY_COLORS = false;

    public static void setForceLegacyColors(boolean v) { FORCE_LEGACY_COLORS = v; }

    /**
     * Convert a MiniMessage string into a legacy section-colored string (§ codes).
     * Attempts to use MiniMessage -> legacy serializer when available, otherwise
     * falls back to a best-effort tag replacement.
     */
    public static String miniMessageToLegacyString(String miniMessage) {
        if (miniMessage == null) return "";
        try {
            Component comp = MINI_MESSAGE.deserialize(miniMessage);
            String out = SECTION_SERIALIZER.serialize(comp);
            if (out != null) return out;
        } catch (Throwable ignored) {}
        // Fallback: simple tag replacement similar to previous compatibility shim
        return miniMessageToLegacy(miniMessage);
    }

    private static String miniMessageToLegacy(String mm) {
        if (mm == null || mm.isBlank()) return mm;
        String out = mm;
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
                    replacement = hexToNearestLegacy(firstHex) + inner + '\u00A7' + 'r';
                } else {
                    replacement = inner;
                }
                gm.appendReplacement(gsb, java.util.regex.Matcher.quoteReplacement(replacement));
            }
            gm.appendTail(gsb);
            out = gsb.toString();
        } catch (Throwable ignored) {}
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
                out = out.replaceAll("(?i)</#?[0-9a-fA-F]{0,6}>", "\u00A7r");
            } catch (Throwable ignored) {}
        }
        try {
            out = out.replaceAll("(?i)<gradient>", "");
            out = out.replaceAll("(?i)</gradient>", "\u00A7r");
        } catch (Throwable ignored) {}
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

    public static Component parseLegacySection(String legacy) {
        if (legacy == null || legacy.isBlank()) return Component.empty();
        try {
            return SECTION_SERIALIZER.deserialize(legacy);
        } catch (Throwable ignored) {
            try { return Component.text(legacy); } catch (Throwable ignored2) { return Component.empty(); }
        }
    }

    /* ---------------------- MiniMessage / legacy helpers ---------------------- */

    @SuppressWarnings("deprecation")
    public static String translateLegacyColors(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.replace('&', '\u00A7');
    }

    public static String translateColors(String text) {
        if (text == null || text.isEmpty()) return "";
        if (containsMiniMessageTags(text)) {
            try {
                Component component = MINI_MESSAGE.deserialize(text);
                return SECTION_SERIALIZER.serialize(component);
            } catch (Throwable t) {
                return translateLegacyColors(text);
            }
        }
        return translateLegacyColors(text);
    }

    private static boolean containsMiniMessageTags(String text) {
        if (text == null || text.isEmpty()) return false;
        int length = text.length();
        for (int i = 0; i < length - 1; i++) {
            if (text.charAt(i) == '<') {
                int closeIdx = text.indexOf('>', i + 1);
                if (closeIdx > i + 1 && closeIdx < i + 30) {
                    String potential = text.substring(i + 1, closeIdx);
                    // Accept named tags like <green> and also hex tags like <#ff8800>
                    if (MINIMESSAGE_TAG_PATTERN.matcher(potential).matches()) return true;
                    if (potential.startsWith("#") && potential.length() <= 7) {
                        String hex = potential.substring(1);
                        if (hex.matches("(?i:[0-9a-f]{1,6})")) return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) return "";
        try {
            return text.replaceAll("(?i)\u00A7[0-9A-FK-OR]", "");
        } catch (Throwable ignored) {
            return text;
        }
    }

    public static Component parseMiniMessage(String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) return Component.empty();
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    public static String serializeToMiniMessage(Component component) {
        if (component == null) return "";
        return MINI_MESSAGE.serialize(component);
    }

    public static Component legacyToComponent(String legacyText) {
        if (legacyText == null || legacyText.isEmpty()) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(legacyText);
    }

    public static String componentToLegacy(Component component) {
        if (component == null) return "";
        return SECTION_SERIALIZER.serialize(component);
    }

    public static String ampersandToSection(String text) {
        return translateLegacyColors(text);
    }

    /* ---------------------- Messaging helpers (prefix/send/etc) ---------------------- */

    /**
     * Sets the prefix for all plugin messages. Supports color codes (&) and MiniMessage.
     */
    public static void setPrefix(String newPrefix) {
        if (newPrefix == null) {
            prefix = "";
            return;
        }
        prefix = newPrefix.trim();
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void send(Object recipient, String message) {
        if (recipient == null || message == null) return;
        Component prefixComp = prefix.isEmpty() ? Component.empty() : MessageFormatter.format(prefix);
        // Resolve placeholders using platform service if available, otherwise reflection fallback
        String resolved;
        try {
            var svc = PlatformMessageServiceRegistry.get();
            if (svc != null) resolved = svc.resolvePlaceholders(recipient, message, LOGGER);
            else resolved = PlaceholderUtil.resolvePlaceholders(recipient, message, LOGGER);
        } catch (Throwable t) {
            resolved = PlaceholderUtil.resolvePlaceholders(recipient, message, LOGGER);
        }
        Component messageComp = MessageFormatter.format(resolved);
        Component full = prefixComp.append(messageComp);
        // Prefer platform service for delivery if present
        try {
            var svc = PlatformMessageServiceRegistry.get();
            if (svc != null && svc.sendToSender(recipient, full)) return;
        } catch (Throwable ignored) {}
        String out = serializeComponent(full);
        out = extractLegacySubstringIfNeeded(out);
        try { recipient.getClass().getMethod("sendMessage", String.class).invoke(recipient, out); } catch (Throwable ignored) {}
    }

    public static void send(Object recipient, Component message) {
        if (recipient == null || message == null) return;
        Component prefixComp = prefix.isEmpty() ? Component.empty() : MessageFormatter.format(prefix);
        Component resolvedMessage = message;
        try {
            var svc = PlatformMessageServiceRegistry.get();
            if (svc != null) {
                resolvedMessage = svc.resolvePlaceholdersComponent(recipient, message, LOGGER);
            } else {
                // fallback: serialize -> resolve -> parse
                String mm = serializeToMiniMessage(message);
                String replaced = PlaceholderUtil.resolvePlaceholders(recipient, mm, LOGGER);
                resolvedMessage = MessageUtil.parseMiniMessage(replaced);
            }
        } catch (Throwable ignored) {}
        Component full = prefixComp.append(resolvedMessage);
        try {
            var svc = PlatformMessageServiceRegistry.get();
            if (svc != null && svc.sendToSender(recipient, full)) return;
        } catch (Throwable ignored) {}
        String out = serializeComponent(full);
        out = extractLegacySubstringIfNeeded(out);
        try { recipient.getClass().getMethod("sendMessage", String.class).invoke(recipient, out); } catch (Throwable ignored) {}
    }

    public static void broadcast(String message) {
        // Per-player placeholder resolution: send individually so PAPI placeholders like %player_name% resolve
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            java.lang.reflect.Method getOnline = bukkit.getMethod("getOnlinePlayers");
            Object players = getOnline.invoke(null);
            if (players instanceof java.lang.Iterable) {
                for (Object p : (java.lang.Iterable<?>) players) {
                    String resolved;
                    try {
                        var svc = PlatformMessageServiceRegistry.get();
                        if (svc != null) resolved = svc.resolvePlaceholders(p, prefix + message, LOGGER);
                        else resolved = PlaceholderUtil.resolvePlaceholders(p, prefix + message, LOGGER);
                    } catch (Throwable t) {
                        resolved = PlaceholderUtil.resolvePlaceholders(p, prefix + message, LOGGER);
                    }
                    Component comp = MessageFormatter.format(resolved);
                    try {
                        var svc = PlatformMessageServiceRegistry.get();
                        if (svc != null && svc.sendToSender(p, comp)) continue;
                    } catch (Throwable ignored) {}
                    String out = serializeComponent(comp);
                    out = extractLegacySubstringIfNeeded(out);
                    try { p.getClass().getMethod("sendMessage", String.class).invoke(p, out); } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void broadcast(Component message) {
        Component prefixComp = prefix.isEmpty() ? Component.empty() : MessageFormatter.format(prefix);
        Component fullComp = prefixComp.append(message);
        try {
            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            java.lang.reflect.Method getOnline = bukkit.getMethod("getOnlinePlayers");
            Object players = getOnline.invoke(null);
            if (players instanceof java.lang.Iterable) {
                for (Object p : (java.lang.Iterable<?>) players) {
                    Component resolved = fullComp;
                    try {
                        var svc = PlatformMessageServiceRegistry.get();
                        if (svc != null) {
                            resolved = svc.resolvePlaceholdersComponent(p, fullComp, LOGGER);
                        } else {
                            String mm = serializeToMiniMessage(fullComp);
                            String replaced = PlaceholderUtil.resolvePlaceholders(p, mm, LOGGER);
                            resolved = MessageUtil.parseMiniMessage(replaced);
                        }
                    } catch (Throwable ignored) {}
                    try {
                        var svc = PlatformMessageServiceRegistry.get();
                        if (svc == null || !svc.sendToSender(p, resolved)) {
                            try { p.getClass().getMethod("sendMessage", String.class).invoke(p, serializeComponent(resolved)); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {
                        try { p.getClass().getMethod("sendMessage", String.class).invoke(p, serializeComponent(resolved)); } catch (Throwable ignored2) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void log(Logger logger, String message, Level level) {
        if (logger == null || message == null) return;
        logger.log(level != null ? level : Level.INFO, stripColors(prefix + message));
    }

    public static void log(Logger logger, Component message, Level level) {
        if (logger == null || message == null) return;
        logger.log(level != null ? level : Level.INFO, stripColors(prefix + serializeComponent(message)));
    }

    public static String serialize(String message) {
        if (message == null) return "";
        Component comp = MessageFormatter.format(message);
        return serializeComponent(comp);
    }

    public static String serialize(String prefix, Component message) {
        if (message == null) return "";
        Component prefixComp = (prefix == null || prefix.isEmpty()) ? Component.empty() : MessageFormatter.format(prefix);
        Component full = prefixComp.append(message);
        return serializeComponent(full);
    }

    public static String serializeComponent(Component message) {
        if (message == null) return "";
        try {
            return SECTION_SERIALIZER.serialize(message);
        } catch (Throwable t) {
            try {
                return MINI_MESSAGE.serialize(message);
            } catch (Throwable ignored) {
                try { return message.toString(); } catch (Throwable ignored2) { return ""; }
            }
        }
    }

    // Delivery is handled by platform-provided PlatformMessageService implementations.

    public static String stripColorsForLog(String input) {
        return MessageFormatter.stripColors(input);
    }

    private static String extractLegacySubstringIfNeeded(String out) {
        if (out != null && out.contains("TextComponentImpl")) {
            int first = out.indexOf('\u00A7');
            if (first >= 0) {
                int last = out.lastIndexOf("\u00A7r");
                if (last >= 0 && last + 2 > first) {
                    out = out.substring(first, last + 2);
                } else {
                    out = out.substring(first);
                }
                if (out.endsWith("\u00A7r")) out = out.substring(0, out.length() - 2);
            }
        }
        return out;
    }

    /* Convenience for setting lore/display elsewhere */
    public static List<Component> toComponentList(List<Component> list) { return list; }
}
