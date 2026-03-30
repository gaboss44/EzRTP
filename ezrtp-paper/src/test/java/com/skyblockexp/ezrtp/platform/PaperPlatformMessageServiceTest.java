package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaperPlatformMessageServiceTest {

    private static final Logger LOGGER = Logger.getLogger("PaperPlatformMessageServiceTest");

    @Mock
    private CommandSender sender;

    @Mock
    private Player player;

    @Test
    void sendToSenderReturnsFalseWhenSenderOrComponentIsNull() {
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                new Object(),
                (p, text) -> text,
                (audiences, commandSender, component) -> true,
                audiences -> {}
        );

        assertFalse(service.sendToSender(null, Component.text("hello")));
        assertFalse(service.sendToSender(sender, null));
    }

    @Test
    void sendToSenderReturnsFalseForNonCommandSender() {
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                new Object(),
                (p, text) -> text,
                (audiences, commandSender, component) -> true,
                audiences -> {}
        );

        assertFalse(service.sendToSender("not-a-sender", Component.text("hello")));
    }

    @Test
    void sendToSenderUsesAudienceWhenAvailable() {
        Component message = Component.text("hello");
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                new Object(),
                (p, text) -> text,
                (audiences, commandSender, component) -> true,
                audiences -> {}
        );

        assertTrue(service.sendToSender(sender, message));
        verify(sender, never()).sendMessage(any(String.class));
    }

    @Test
    void sendToSenderFallsBackToSerializedComponentWhenAudienceFails() {
        Component message = Component.text("hello");
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                new Object(),
                (p, text) -> text,
                (audiences, commandSender, component) -> false,
                audiences -> {}
        );

        assertTrue(service.sendToSender(sender, message));
        verify(sender).sendMessage(MessageUtil.serializeComponent(message));
    }

    @Test
    void sendToSenderFallsBackToSerializedComponentWhenAudiencesMissing() {
        Component message = Component.text("hello");
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                null,
                (p, text) -> text,
                (audiences, commandSender, component) -> true,
                audiences -> {}
        );

        assertTrue(service.sendToSender(sender, message));
        verify(sender).sendMessage(MessageUtil.serializeComponent(message));
    }

    @Test
    void resolvePlaceholdersReturnsNullForNullText() {
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                null,
                (p, text) -> "ignored",
                (audiences, commandSender, component) -> true,
                audiences -> {}
        );

        assertNull(service.resolvePlaceholders(player, null, LOGGER));
    }

    @Test
    void resolvePlaceholdersUsesTypedPlayerAndReturnsResolvedText() {
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                null,
                (p, text) -> {
                    assertEquals(player, p);
                    return text.replace("%name%", "Alex");
                },
                (audiences, commandSender, component) -> true,
                audiences -> {}
        );

        String resolved = service.resolvePlaceholders(player, "Hi %name%", LOGGER);

        assertEquals("Hi Alex", resolved);
    }

    @Test
    void resolvePlaceholdersPassesNullPlayerForNonPlayerAndFallsBackOnResolverFailure() {
        PaperPlatformMessageService service = new PaperPlatformMessageService(
                null,
                (p, text) -> {
                    assertNull(p);
                    throw new RuntimeException("no papi");
                },
                (audiences, commandSender, component) -> true,
                audiences -> {}
        );

        String resolved = service.resolvePlaceholders(sender, "Hi %name%", LOGGER);

        assertEquals("Hi %name%", resolved);
    }
}
