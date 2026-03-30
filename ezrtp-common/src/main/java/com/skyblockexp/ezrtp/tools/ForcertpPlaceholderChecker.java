package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.message.MessageFormatter;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Small runtime checker to verify legacy key migration and placeholder replacement
 * for the ForceRTP messages. Designed to be run locally via Maven exec:java.
 */
public final class ForcertpPlaceholderChecker {

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("ForcertpPlaceholderChecker");

        File temp = Files.createTempDirectory("ezrtp-msg-check").toFile();
        File en = new File(temp, "en.yml");
        try (FileWriter w = new FileWriter(en)) {
            w.write("forcertp-target-notify: '<yellow>You are being teleported to <white><world></white></yellow>'\n");
        }

        MessageProvider provider = MessageProvider.load(temp, "en", logger);
        String migrated = provider.getMessage(MessageKey.FORCERTP_TARGET_NOTIFICATION);
        System.out.println("Migrated message: " + migrated);
        if (migrated == null || !migrated.contains("<world>")) {
            throw new IllegalStateException("Legacy migration failed");
        }

        var comp = provider.format(MessageKey.FORCERTP_TARGET_NOTIFICATION, Map.of("world", "MyWorld"));
        String legacy = MessageFormatter.toLegacy(comp);
        System.out.println("Formatted legacy output: " + legacy);
        if (!legacy.contains("MyWorld")) {
            throw new IllegalStateException("Placeholder replacement failed");
        }

        System.out.println("ForceRTP placeholder checks passed");
    }
}
