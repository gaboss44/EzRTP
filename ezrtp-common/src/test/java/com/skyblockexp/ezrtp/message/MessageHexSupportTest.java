package com.skyblockexp.ezrtp.message;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HEX color and gradient support in MiniMessage messages.
 */
class MessageHexSupportTest {

    @Test
    void hexTagProducesHexLegacy() {
        String message = "<#ff8800>HexColor</#ff8800>";
        Component comp = MessageFormatter.format(message);
        assertNotNull(comp);
        String legacy = MessageFormatter.toLegacy(comp);
        assertNotNull(legacy);
        // Legacy hex serializer should produce the §x format when hex colors are preserved
        assertTrue(legacy.contains("§x") || legacy.toLowerCase().contains("ff8800"), "Expected legacy output to contain hex sequence or §x, got: " + legacy);
    }

    @Test
    void gradientTagProducesHexLegacy() {
        String message = "<gradient:#ff0000:#00ff00>Grad</gradient>";
        Component comp = MessageFormatter.format(message);
        assertNotNull(comp);
        String legacy = MessageFormatter.toLegacy(comp);
        assertNotNull(legacy);
        // Gradient should serialize into legacy hex sequences (per-char §x) or contain one of the hex colors
        boolean hasX = legacy.contains("§x");
        boolean hasHex = legacy.toLowerCase().contains("ff0000") || legacy.toLowerCase().contains("00ff00");
        assertTrue(hasX || hasHex, "Expected gradient legacy to contain §x or hex colors, got: " + legacy);
    }
}
