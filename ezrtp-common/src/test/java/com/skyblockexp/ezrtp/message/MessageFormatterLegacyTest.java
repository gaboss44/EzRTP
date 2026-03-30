package com.skyblockexp.ezrtp.message;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for MessageFormatter handling of legacy codes and MiniMessage.
 */
class MessageFormatterLegacyTest {
    
    @Test
    void testLegacyCodesOnly() {
        // Test pure legacy codes
        Component result = MessageFormatter.format("&cRed text &aGreen text");
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("§"), "Should contain legacy section codes");
    }
    
    @Test
    void testMiniMessageOnly() {
        // Test pure MiniMessage
        Component result = MessageFormatter.format("<red>Red text</red> <green>Green text</green>");
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("§"), "Should contain legacy section codes");
    }
    
    @Test
    void testLegacyWithPlaceholders() {
        // Test legacy codes with placeholders
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", "Steve");
        placeholders.put("amount", "100");
        
        Component result = MessageFormatter.format("&aHello <player>, you have &e<amount> &acoins", placeholders);
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("Steve"), "Should contain placeholder value");
        assertTrue(legacy.contains("100"), "Should contain placeholder value");
    }
    
    @Test
    void testMiniMessageWithPlaceholders() {
        // Test MiniMessage with placeholders
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("x", "100");
        placeholders.put("z", "200");
        
        Component result = MessageFormatter.format("<green>Teleported to <white><x></white>, <white><z></white></green>", placeholders);
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("100"), "Should contain placeholder value");
        assertTrue(legacy.contains("200"), "Should contain placeholder value");
    }
    
    @Test
    void testSectionCodesDirectly() {
        // Test § codes directly (should work with legacy parser)
        Component result = MessageFormatter.format("§cRed §aGreen");
        assertNotNull(result);
        String legacy = MessageFormatter.toLegacy(result);
        assertTrue(legacy.contains("§"), "Should contain legacy section codes");
    }
    
    @Test
    void testEmptyMessage() {
        Component result = MessageFormatter.format("");
        assertNotNull(result);
        assertEquals(Component.empty(), result);
    }
    
    @Test
    void testNullMessage() {
        Component result = MessageFormatter.format(null);
        assertNotNull(result);
        assertEquals(Component.empty(), result);
    }
}
