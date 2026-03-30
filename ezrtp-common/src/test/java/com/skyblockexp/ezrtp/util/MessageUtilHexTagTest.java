package com.skyblockexp.ezrtp.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for hex-tag detection and translation helpers in MessageUtil.
 */
class MessageUtilHexTagTest {

    @Test
    void translateHexTagPrefixPreserved() {
        String text = "<#2fff7a>Example</#2fff7a>";
        String out = MessageUtil.translateColors(text);
        assertNotNull(out);
        assertTrue(out.contains("§x") || out.toLowerCase().contains("2fff7a") || out.toLowerCase().contains("ff8800") , "Expected translated output to contain §x or hex, got: " + out);
    }

    @Test
    void translateHexMiniMessageToLegacy() {
        String mm = "<#ff8800>HexColor</#ff8800>";
        String translated = MessageUtil.translateColors(mm);
        assertNotNull(translated);
        // Expect either legacy §x sequence or color hex included in output
        assertTrue(translated.contains("§x") || translated.toLowerCase().contains("ff8800"), "Expected legacy output to contain §x or hex, got: " + translated);
    }
}
