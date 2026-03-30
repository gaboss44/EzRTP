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

class SquareBracketPlaceholderTest {

    private final Logger logger = Logger.getLogger("SquareBracketPlaceholderTest");

    @Test
    void squareBracketPlaceholderIsReplaced(@TempDir Path tempDir) throws IOException {
        File messagesDir = tempDir.toFile();
        File enFile = new File(messagesDir, "en.yml");
        messagesDir.mkdirs();

        try (FileWriter writer = new FileWriter(enFile)) {
            writer.write("forcertp-target-notification: '<yellow>You are being teleported to <white>[world]</white></yellow>'\n");
        }

        MessageProvider provider = MessageProvider.load(messagesDir, "en", logger);

        var component = provider.format(MessageKey.FORCERTP_TARGET_NOTIFICATION, Map.of("world", "MyWorld"));
        assertNotNull(component);
        String legacy = MessageFormatter.toLegacy(component);
        assertTrue(legacy.contains("MyWorld"), "Square-bracket world placeholder should be replaced");
    }
}
