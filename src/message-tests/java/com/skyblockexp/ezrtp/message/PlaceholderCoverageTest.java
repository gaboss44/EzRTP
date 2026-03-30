package com.skyblockexp.ezrtp.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderCoverageTest {

    private final Logger logger = Logger.getLogger("PlaceholderCoverageTest");

    @Test
    void allPlaceholdersAreReplaced(@TempDir Path tempDir) throws IOException {
        File messagesDir = tempDir.toFile();
        File enFile = new File(messagesDir, "en.yml");
        messagesDir.mkdirs();

        // Use an existing MessageKey (teleport-success) and include all placeholders to verify
        try (FileWriter writer = new FileWriter(enFile)) {
            writer.write("teleport-success: 'You were teleported to <white><x></white>, <white><z></white> in <white><world></white>. Pos:<position> Server:<server> Cost:<cost> Biome:<target-biome> Cached:<cached-locations>'\n");
        }

        MessageProvider provider = MessageProvider.load(messagesDir, "en", logger);

        Map<String, String> placeholders = Map.of(
                "world", "MyWorld",
                "x", "100",
                "z", "200",
                "position", "(100,64,200)",
                "server", "lobby-1",
                "cost", "5",
                "target-biome", "PLAINS",
                "cached-locations", "42"
        );

        var component = provider.format(MessageKey.TELEPORT_SUCCESS, placeholders);
        assertNotNull(component);
        String legacy = MessageFormatter.toLegacy(component);

        assertTrue(legacy.contains("MyWorld"), "world placeholder should be replaced");
        assertTrue(legacy.contains("100"), "x placeholder should be replaced");
        assertTrue(legacy.contains("200"), "z placeholder should be replaced");
        assertTrue(legacy.contains("(100,64,200)"), "position placeholder should be replaced");
        assertTrue(legacy.contains("lobby-1"), "server placeholder should be replaced");
        assertTrue(legacy.contains("5"), "cost placeholder should be replaced");
        assertTrue(legacy.contains("PLAINS"), "target-biome placeholder should be replaced");
        assertTrue(legacy.contains("42"), "cached-locations placeholder should be replaced");
    }
}
