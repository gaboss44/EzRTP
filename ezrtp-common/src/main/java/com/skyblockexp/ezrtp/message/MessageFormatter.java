package com.skyblockexp.ezrtp.message;

import net.kyori.adventure.text.Component;
import com.skyblockexp.ezrtp.util.MessageUtil;

import java.util.Map;

/**
 * Utility class for formatting messages with MiniMessage and legacy color code support.
 * Provides methods to format messages with placeholders and convert between formats.
 */
public final class MessageFormatter {
    // Use TextRenderer for runtime compatible parsing/serialization
    
    private MessageFormatter() {
        // Utility class
    }
    
    /**
     * Formats a message using MiniMessage syntax with placeholders.
     * Also supports legacy color codes (&amp;) for backward compatibility.
     * 
     * The method intelligently handles both formats:
     * - Tries MiniMessage parsing first for messages with angle bracket tags
     * - Falls back to legacy color code parsing if MiniMessage fails
     * - Supports placeholders in both formats
     * 
     * @param message The message template with MiniMessage tags and/or legacy codes
     * @param placeholders Map of placeholder names to values
     * @return Formatted Component
     */
    public static Component format(String message, Map<String, String> placeholders) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String processed = message;
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                processed = processed.replace("<" + entry.getKey() + ">", entry.getValue());
            }
        }
        // If the message appears to be legacy-style (contains & or § and no MiniMessage tags),
        // parse using the legacy serializer. Otherwise prefer MiniMessage parsing.
        if ((processed.contains("&") || processed.contains("\u00A7")) && !processed.contains("<")) {
            return fromLegacy(processed);
        }
        return MessageUtil.parseMiniMessage(processed);
    }
    
    /**
     * Formats a message using MiniMessage syntax without placeholders.
     * Also supports legacy color codes (&amp;) for backward compatibility.
     * 
     * @param message The message template with MiniMessage tags
     * @return Formatted Component
     */
    public static Component format(String message) {
        return format(message, null);
    }
    
    /**
     * Converts a Component to a legacy format string (with § codes).
     * Useful for sending messages to players in legacy format.
     * 
     * @param component The component to serialize
     * @return Legacy formatted string
     */
    public static String toLegacy(Component component) {
        if (component == null) {
            return "";
        }
        return MessageUtil.componentToLegacy(component);
    }
    
    /**
     * Parses a legacy formatted string (with &amp; or § codes) to a Component.
     * 
     * @param legacyMessage The legacy formatted message
     * @return Parsed Component
     */
    public static Component fromLegacy(String legacyMessage) {
        if (legacyMessage == null || legacyMessage.isEmpty()) {
            return Component.empty();
        }
        // First convert & to § if present
        String normalized = legacyMessage.replace('&', '\u00A7');
        // Use the legacy serializer to turn § codes into a Component
        try {
            return MessageUtil.legacyToComponent(normalized);
        } catch (Throwable t) {
            // Fallback to parsing as MiniMessage if legacy deserialization fails
            try { return MessageUtil.parseMiniMessage(normalized); } catch (Throwable ignored) { return Component.empty(); }
        }
    }
    
    /**
     * Strips all color codes from a message for logging purposes.
     * 
     * @param message The message to strip
     * @return Plain text without color codes
     */
    public static String stripColors(String message) {
        if (message == null) {
            return "";
        }
        return message.replaceAll("[\u00A7&][0-9A-FK-ORa-fk-or]", "");
    }
}
