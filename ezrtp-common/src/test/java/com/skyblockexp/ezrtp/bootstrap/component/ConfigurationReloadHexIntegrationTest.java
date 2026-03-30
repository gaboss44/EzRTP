package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.message.MessageFormatter;
import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test: simulate loading a config prefix with a HEX MiniMessage tag
 * and ensure serialization produces colored output rather than a malformed plain string.
 */
class ConfigurationReloadHexIntegrationTest {

    @Test
    void prefixWithHexIsSerializedCorrectly() {
        // Simulate admin putting a hex-colored prefix in config.yml
        String prefix = "<#2fff7a>EzRTP</#2fff7a> ";

        String previous = MessageUtil.getPrefix();
        try {
            // Plugin would call MessageUtil.setPrefix(prefix) during reload
            MessageUtil.setPrefix(prefix);

            // Compose a simple message component and serialize with the given prefix
            Component msg = MessageFormatter.format("EzRTP configuration reloaded.");
            String out = MessageUtil.serialize(prefix, msg);

            assertNotNull(out, "Serialized output should not be null");

            // Expect either legacy §x sequence or one of the hex strings to appear
            boolean hasLegacyX = out.contains("§x");
            boolean hasHex = out.toLowerCase().contains("2fff7a");
            assertTrue(hasLegacyX || hasHex, "Expected colored output (§x or hex) but got: " + out);

            // Guard against the malformed case reported by users (literal leading hex digits)
            assertFalse(out.startsWith("2fff7a"), "Output should not start with raw hex digits: " + out);
        } finally {
            MessageUtil.setPrefix(previous);
        }
    }
}
