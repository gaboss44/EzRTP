package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BukkitPlatformMessageServiceTest {

    @Mock
    private Plugin plugin;

    @Mock
    private CommandSender sender;

    @Test
    void sendToSenderReturnsFalseWhenSenderOrComponentNull() {
        BukkitPlatformMessageService service = new BukkitPlatformMessageService(plugin);

        assertFalse(service.sendToSender(null, Component.text("hello")));
        assertFalse(service.sendToSender(sender, null));
    }

    @Test
    void sendToSenderFallsBackToSerializedComponentForCommandSender() {
        BukkitPlatformMessageService service = new BukkitPlatformMessageService(plugin);
        Component message = Component.text("hello", NamedTextColor.GREEN);

        assertTrue(service.sendToSender(sender, message));

        verify(sender).sendMessage(MessageUtil.serializeComponent(message));
        assertNotEquals(message.toString(), MessageUtil.serializeComponent(message));
    }

    @Test
    void sendToSenderReflectiveFallbackUsesSerializedComponentForNonCommandSender() {
        BukkitPlatformMessageService service = new BukkitPlatformMessageService(plugin);
        Component message = Component.text("hello", NamedTextColor.GREEN);
        ReflectiveSender reflectiveSender = new ReflectiveSender();

        assertTrue(service.sendToSender(reflectiveSender, message));

        assertNotNull(reflectiveSender.lastMessage);
        assertEquals(MessageUtil.serializeComponent(message), reflectiveSender.lastMessage);
        assertFalse(reflectiveSender.lastMessage.contains("TextComponentImpl"));
    }

    @Test
    void sendToSenderReturnsFalseWhenNoCompatibleFallbackExists() {
        BukkitPlatformMessageService service = new BukkitPlatformMessageService(plugin);

        assertFalse(service.sendToSender(new Object(), Component.text("hello")));
    }

    private static final class ReflectiveSender {
        private String lastMessage;

        public void sendMessage(String message) {
            this.lastMessage = message;
        }
    }
}
