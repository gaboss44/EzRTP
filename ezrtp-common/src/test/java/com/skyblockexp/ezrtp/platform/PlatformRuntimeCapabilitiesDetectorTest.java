package com.skyblockexp.ezrtp.platform;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the package-private {@code isAtLeast121} helper in
 * {@link PlatformRuntimeCapabilitiesDetector}.
 */
class PlatformRuntimeCapabilitiesDetectorTest {

    @ParameterizedTest
    @ValueSource(strings = {"1.21", "1.21.1", "1.21.4", "1.22", "2.0", "2.0.0"})
    void testVersionAtLeast121ReturnsTrue(String version) {
        assertTrue(PlatformRuntimeCapabilitiesDetector.isAtLeast121(version), version);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.20", "1.20.6", "1.19", "1.8", "1.0"})
    void testVersionBelow121ReturnsFalse(String version) {
        assertFalse(PlatformRuntimeCapabilitiesDetector.isAtLeast121(version), version);
    }

    @ParameterizedTest
    @CsvSource({"1.21-pre1", "1.21-rc2", "1.22-pre3"})
    void testSnapshotVersionStripped(String version) {
        assertTrue(PlatformRuntimeCapabilitiesDetector.isAtLeast121(version), version);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "abc", "not-a-version"})
    void testMalformedVersionReturnsFalse(String version) {
        assertFalse(PlatformRuntimeCapabilitiesDetector.isAtLeast121(version), version);
    }
}
