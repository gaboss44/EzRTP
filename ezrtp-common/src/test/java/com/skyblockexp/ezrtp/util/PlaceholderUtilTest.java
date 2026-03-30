package com.skyblockexp.ezrtp.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the PlaceholderUtil class.
 * 
 * Note: These tests verify the graceful fallback behavior when PlaceholderAPI is not available.
 * Testing with actual PlaceholderAPI would require integration tests with the full plugin environment.
 */
class PlaceholderUtilTest {

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("PlaceholderUtilTest");
        // Reset the availability check before each test
        PlaceholderUtil.resetAvailabilityCheck();
    }

    @AfterEach
    void tearDown() {
        // Clean up after tests
        PlaceholderUtil.resetAvailabilityCheck();
    }

    @Test
    void isPlaceholderAPIAvailable_returnsFalseWhenNotInstalled() {
        // PlaceholderAPI is not available in test environment
        boolean available = PlaceholderUtil.isPlaceholderAPIAvailable(logger);
        
        assertFalse(available, "PlaceholderAPI should not be available in test environment");
    }

    @Test
    void isPlaceholderAPIAvailable_cachesResult() {
        // First call
        boolean firstCall = PlaceholderUtil.isPlaceholderAPIAvailable(logger);
        
        // Second call should return the same cached result
        boolean secondCall = PlaceholderUtil.isPlaceholderAPIAvailable(logger);
        
        assertEquals(firstCall, secondCall, "Availability check should be cached");
    }

    @Test
    void resolvePlaceholders_returnsOriginalTextWhenPAPINotAvailable() {
        String originalText = "%player_name% wants to teleport";
        
        String result = PlaceholderUtil.resolvePlaceholders(originalText, logger);
        
        assertEquals(originalText, result, "Should return original text when PAPI is not available");
    }

    @Test
    void resolvePlaceholders_handlesNullTextGracefully() {
        String result = PlaceholderUtil.resolvePlaceholders((String) null, logger);
        
        assertNull(result, "Should handle null text gracefully");
    }

    @Test
    void resolvePlaceholders_handlesEmptyTextGracefully() {
        String result = PlaceholderUtil.resolvePlaceholders("", logger);
        
        assertEquals("", result, "Should handle empty text gracefully");
    }

    @Test
    void resetAvailabilityCheck_allowsRecheck() {
        // First check
        PlaceholderUtil.isPlaceholderAPIAvailable(logger);
        
        // Reset
        PlaceholderUtil.resetAvailabilityCheck();
        
        // Second check should re-evaluate (not cached)
        boolean afterReset = PlaceholderUtil.isPlaceholderAPIAvailable(logger);
        
        assertFalse(afterReset, "Should re-evaluate after reset");
    }

    @Test
    void resolvePlaceholders_preservesTextWithoutPlaceholders() {
        String text = "This is a simple text without placeholders";
        
        String result = PlaceholderUtil.resolvePlaceholders(text, logger);
        
        assertEquals(text, result, "Should preserve text without placeholders");
    }

    @Test
    void resolvePlaceholders_preservesMultiplePlaceholders() {
        String text = "Player %player_name% has %player_level% levels and %player_health% health";
        
        String result = PlaceholderUtil.resolvePlaceholders(text, logger);
        
        // When PAPI is not available, placeholders should remain as-is
        assertEquals(text, result, "Should preserve all placeholders when PAPI is not available");
    }

    @Test
    void resolvePlaceholders_handlesSpecialCharacters() {
        String text = "Special chars: <, >, &, \", '";
        
        String result = PlaceholderUtil.resolvePlaceholders(text, logger);
        
        assertEquals(text, result, "Should handle special characters");
    }
}
