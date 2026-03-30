package com.skyblockexp.ezrtp.message;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MessageProvider class.
 */
class MessageProviderTest {

    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("MessageProviderTest");
    }

    @Test
    void loadFromValidLanguageFile(@TempDir Path tempDir) throws IOException {
        File messagesDir = tempDir.toFile();
        File enFile = new File(messagesDir, "en.yml");
        
        // Create a simple language file
        try (FileWriter writer = new FileWriter(enFile)) {
            writer.write("teleporting: '<gray>Searching...</gray>'\n");
            writer.write("teleport-success: '<green>Success!</green>'\n");
            writer.write("teleport-failed: '<red>Failed!</red>'\n");
        }
        
        MessageProvider provider = MessageProvider.load(messagesDir, "en", logger);
        
        assertNotNull(provider);
        assertEquals("en", provider.getLanguage());
        assertEquals("<gray>Searching...</gray>", provider.getMessage(MessageKey.TELEPORTING));
    }

    @Test
    void loadFallsBackToDefaultWhenFileNotFound(@TempDir Path tempDir) {
        File messagesDir = tempDir.toFile();
        
        MessageProvider provider = MessageProvider.load(messagesDir, "es", logger);
        
        assertNotNull(provider);
        assertEquals("es", provider.getLanguage());
        // Should use default messages
        assertNotNull(provider.getMessage(MessageKey.TELEPORTING));
    }

    @Test
    void loadFallsBackToDefaultWhenDirectoryNotFound() {
        File nonExistent = new File("/nonexistent/directory");
        
        MessageProvider provider = MessageProvider.load(nonExistent, "en", logger);
        
        assertNotNull(provider);
        assertEquals("en", provider.getLanguage());
        assertNotNull(provider.getMessage(MessageKey.TELEPORTING));
    }

    @Test
    void createDefaultProviderHasAllMessages() {
        MessageProvider provider = MessageProvider.createDefault("en", logger);
        
        assertNotNull(provider);
        assertEquals("en", provider.getLanguage());
        
        // Verify all message keys have values
        for (MessageKey key : MessageKey.values()) {
            String message = provider.getMessage(key);
            assertNotNull(message, "Message for key " + key + " should not be null");
            assertFalse(message.isEmpty(), "Message for key " + key + " should not be empty");
        }
    }

    @Test
    void formatMessageWithPlaceholders() {
        MessageProvider provider = MessageProvider.createDefault("en", logger);
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("x", "100");
        placeholders.put("z", "200");
        placeholders.put("world", "world");
        
        var component = provider.format(MessageKey.TELEPORT_SUCCESS, placeholders);
        
        assertNotNull(component);
        String legacy = MessageFormatter.toLegacy(component);
        assertTrue(legacy.contains("100"));
        assertTrue(legacy.contains("200"));
        assertTrue(legacy.contains("world"));
    }

    @Test
    void formatMessageWithoutPlaceholders() {
        MessageProvider provider = MessageProvider.createDefault("en", logger);
        
        var component = provider.format(MessageKey.TELEPORTING);
        
        assertNotNull(component);
    }

    @Test
    void getMessageReturnsDefaultWhenKeyMissing(@TempDir Path tempDir) throws IOException {
        File messagesDir = tempDir.toFile();
        File enFile = new File(messagesDir, "en.yml");
        
        // Create a language file with only one message
        try (FileWriter writer = new FileWriter(enFile)) {
            writer.write("teleporting: '<gray>Custom message</gray>'\n");
        }
        
        MessageProvider provider = MessageProvider.load(messagesDir, "en", logger);
        
        // Should return custom message for TELEPORTING
        assertEquals("<gray>Custom message</gray>", provider.getMessage(MessageKey.TELEPORTING));
        
        // Should return default for missing keys
        String failedMessage = provider.getMessage(MessageKey.TELEPORT_FAILED);
        assertNotNull(failedMessage);
        assertFalse(failedMessage.isEmpty());
    }

    @Test
    void loadHandlesMalformedYamlGracefully(@TempDir Path tempDir) throws IOException {
        File messagesDir = tempDir.toFile();
        File enFile = new File(messagesDir, "en.yml");
        
        // Create a malformed YAML file
        try (FileWriter writer = new FileWriter(enFile)) {
            writer.write("invalid: yaml: structure:\n");
            writer.write("  this is: not: valid\n");
        }
        
        MessageProvider provider = MessageProvider.load(messagesDir, "en", logger);
        
        // Should fall back to defaults
        assertNotNull(provider);
        assertNotNull(provider.getMessage(MessageKey.TELEPORTING));
    }

    @Test
    void allMessageKeysHaveDistinctKeys() {
        MessageKey[] keys = MessageKey.values();
        
        for (int i = 0; i < keys.length; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                assertNotEquals(keys[i].getKey(), keys[j].getKey(),
                    "Message keys should be unique: " + keys[i] + " and " + keys[j]);
            }
        }
    }
}
