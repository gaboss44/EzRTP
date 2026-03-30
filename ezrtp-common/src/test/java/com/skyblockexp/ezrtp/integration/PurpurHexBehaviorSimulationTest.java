package com.skyblockexp.ezrtp.integration;

import com.skyblockexp.ezrtp.message.MessageFormatter;
import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simulate a Purpur-like filter that strips certain characters from serialized
 * legacy color output and reproduce the user's broken message: "2fff7aEzRTP..."
 */
class PurpurHexBehaviorSimulationTest {

    @Test
    void purpurStripsSectionAndHashesProducingRawHexPrefix() {
        String mm = "<#2FFF7A>EzRTP configuration reloaded.</#2FFF7A>";

        Component comp = MessageFormatter.format(mm);
        String serialized = MessageUtil.serializeComponent(comp);
        assertNotNull(serialized);

        // Simulate Purpur behavior reported by user: strip section sign, hash and angle brackets
        String simulated = serialized.replace("\u00A7", "")
                .replace("#", "")
                .replace("<", "")
                .replace(">", "");

        String norm = simulated.trim().toLowerCase();
        assertTrue(norm.startsWith("2fff7aezrtp"), () -> "Simulated output not matching broken report: " + norm);
    }
}
