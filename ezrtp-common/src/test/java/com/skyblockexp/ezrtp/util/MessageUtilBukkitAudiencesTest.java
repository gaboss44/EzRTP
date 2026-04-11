package com.skyblockexp.ezrtp.util;

import net.kyori.adventure.text.Component;
import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class MessageUtilBukkitAudiencesTest {

    @Test
    void sendsUsingBukkitAudiencesWhenAvailable() throws Exception {
        Player player = mock(Player.class);
        AtomicReference<Component> captured = new AtomicReference<>();
        PlatformMessageService previous = PlatformMessageServiceRegistry.get();
        String previousPrefix = MessageUtil.getPrefix();
        // ensure tests are deterministic regardless of global prefix state
        MessageUtil.setPrefix("");
        PlatformMessageServiceRegistry.register(new PlatformMessageService() {
            @Override
            public String resolvePlaceholders(Object player, String text, Logger logger) {
                return text;
            }

            @Override
            public boolean sendToSender(Object sender, Component component) {
                captured.set(component);
                return true;
            }
        });

        try {
            Component msg = Component.text("hello");
            MessageUtil.send(player, msg);
            assertNotNull(captured.get(), "Platform message service should have been invoked");
            assertEquals("hello", MessageUtil.serializeToMiniMessage(captured.get()));
        } finally {
            if (previous != null) {
                PlatformMessageServiceRegistry.register(previous);
            } else {
                PlatformMessageServiceRegistry.unregister();
            }
            // restore global prefix
            MessageUtil.setPrefix(previousPrefix);
        }
    }
}
