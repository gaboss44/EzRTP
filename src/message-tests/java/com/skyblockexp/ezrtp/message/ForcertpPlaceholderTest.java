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

class ForcertpPlaceholderTest {

    private final Logger logger = Logger.getLogger("ForcertpPlaceholderTest");

    @Test
    void legacyKeyIsMigratedToNewKey(@TempDir Path tempDir) throws IOException {
        File messagesDir = tempDir.toFile();
        File enFile = new File(messagesDir, "en.yml");
        messagesDir.mkdirs();

        try (FileWriter writer = new FileWriter(enFile)) {
            writer.write("forcertp-target-notify: '<yellow>You are being teleported to <white><world></white></yellow>'\n");
        }

        MessageProvider provider = MessageProvider.load(messagesDir, "en", logger);

        // The legacy key should be copied to the newer key name
        String migrated = provider.getMessage(MessageKey.FORCERTP_TARGET_NOTIFICATION);
        assertNotNull(migrated);
        assertTrue(migrated.contains("<world>"));
    }

    @Test
    void formattingReplacesWorldPlaceholder(@TempDir Path tempDir) throws IOException {
        File messagesDir = tempDir.toFile();
        File enFile = new File(messagesDir, "en.yml");
        messagesDir.mkdirs();

        try (FileWriter writer = new FileWriter(enFile)) {
            writer.write("forcertp-target-notification: '<yellow>You are being teleported to <white><world></white></yellow>'\n");
        }

        MessageProvider provider = MessageProvider.load(messagesDir, "en", logger);

        var component = provider.format(MessageKey.FORCERTP_TARGET_NOTIFICATION, Map.of("world", "MyWorld"));
        assertNotNull(component);
        String legacy = MessageFormatter.toLegacy(component);
        assertTrue(legacy.contains("MyWorld"), "Formatted message should contain the world name");
    }
}
