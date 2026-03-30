package com.skyblockexp.ezrtp.message;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MessageFormatter utility class.
 */
class MessageFormatterTest {

    @Test
    void formatWithMiniMessageSyntax() {
        String message = "<red>This is a <bold>test</bold> message</red>";
        Component result = MessageFormatter.format(message);
        
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("§"));
    }

    @Test
    void formatWithLegacyColorCodes() {
        String message = "&cThis is a &ltest&r&c message";
        Component result = MessageFormatter.format(message);
        
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("§"));
    }

    @Test
    void formatWithPlaceholders() {
        String message = "<red>Player <white><player></white> teleported to <white><x></white>, <white><z></white></red>";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", "TestPlayer");
        placeholders.put("x", "100");
        placeholders.put("z", "200");
        
        Component result = MessageFormatter.format(message, placeholders);
        
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("TestPlayer"));
        assertTrue(legacy.contains("100"));
        assertTrue(legacy.contains("200"));
    }

    @Test
    void formatWithEmptyMessage() {
        Component result = MessageFormatter.format("");
        
        assertNotNull(result);
        assertEquals(Component.empty(), result);
    }

    @Test
    void formatWithNullMessage() {
        Component result = MessageFormatter.format(null);
        
        assertNotNull(result);
        assertEquals(Component.empty(), result);
    }

    @Test
    void formatWithEmptyPlaceholders() {
        String message = "<green>Simple message</green>";
        Component result = MessageFormatter.format(message, new HashMap<>());
        
        assertNotNull(result);
    }

    @Test
    void toLegacyConvertsComponentToString() {
        String message = "<red>Test <bold>Message</bold></red>";
        Component component = MessageFormatter.format(message);
        String legacy = MessageFormatter.toLegacy(component);
        
        assertNotNull(legacy);
        assertTrue(legacy.contains("§"));
    }

    @Test
    void toLegacyWithNullComponent() {
        String result = MessageFormatter.toLegacy(null);
        
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void fromLegacyParsesColorCodes() {
        String legacyMessage = "§cRed text §lbold §r§anormal green";
        Component result = MessageFormatter.fromLegacy(legacyMessage);
        
        assertNotNull(result);
    }

    @Test
    void fromLegacyWithAmpersandCodes() {
        String legacyMessage = "&cRed text &lbold &r&anormal green";
        Component result = MessageFormatter.fromLegacy(legacyMessage);
        
        assertNotNull(result);
    }

    @Test
    void fromLegacyWithNullMessage() {
        Component result = MessageFormatter.fromLegacy(null);
        
        assertNotNull(result);
        assertEquals(Component.empty(), result);
    }

    @Test
    void stripColorsRemovesAllColorCodes() {
        String message = "§cRed §lBold &aGreen §rNormal text";
        String result = MessageFormatter.stripColors(message);
        
        assertNotNull(result);
        assertFalse(result.contains("§"));
        assertFalse(result.contains("&"));
        assertTrue(result.contains("Red"));
        assertTrue(result.contains("Normal text"));
    }

    @Test
    void stripColorsWithNullMessage() {
        String result = MessageFormatter.stripColors(null);
        
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void mixedMiniMessageAndLegacyCodes() {
        String message = "<red>&lBold red text</red>";
        Component result = MessageFormatter.format(message);
        
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("§"));
    }
}
