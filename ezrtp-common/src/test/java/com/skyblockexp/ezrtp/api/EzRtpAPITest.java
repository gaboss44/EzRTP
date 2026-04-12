package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EzRtpAPITest {

    @Test
    void getTeleportServiceAndRtpPlayer_delegateToService() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        RandomTeleportService service = mock(RandomTeleportService.class);
        EzRtpPlugin ez = mock(EzRtpPlugin.class);
        when(ez.getTeleportService()).thenReturn(service);
        when(pluginManager.getPlugin("EzRTP")).thenReturn(ez);

        boolean mockedStaticUsed = false;
        try {
            var mocked = org.mockito.Mockito.mockStatic(org.bukkit.Bukkit.class);
            mockedStaticUsed = true;
            try (org.mockito.MockedStatic<org.bukkit.Bukkit> ignored = mocked) {
                mocked.when(org.bukkit.Bukkit::getPluginManager).thenReturn(pluginManager);
                mocked.when(org.bukkit.Bukkit::getLogger).thenReturn(java.util.logging.Logger.getLogger("EzRTP-Test"));

                Player player = mock(Player.class);

                // Ensure we can fetch the service and the convenience wrapper delegates
                RandomTeleportService fetched = (RandomTeleportService) EzRtpAPI.getTeleportService();
                assert fetched == service;

                EzRtpAPI.rtpPlayer(player);
                verify(service).teleportPlayer(player, TeleportReason.COMMAND);
            }
        } catch (org.mockito.exceptions.base.MockitoException ex) {
            // Static mocking unavailable (already registered) — fall back to reflection
            java.lang.reflect.Field serverField = null;
            Object previousServer = null;
            try {
                serverField = Bukkit.class.getDeclaredField("server");
                serverField.setAccessible(true);
                previousServer = serverField.get(null);
                serverField.set(null, server);

                Player player = mock(Player.class);

                RandomTeleportService fetched = (RandomTeleportService) EzRtpAPI.getTeleportService();
                assert fetched == service;

                EzRtpAPI.rtpPlayer(player);
                verify(service).teleportPlayer(player, TeleportReason.COMMAND);
            } finally {
                try { if (serverField != null) serverField.set(null, previousServer); } catch (Throwable ignored) {}
            }
        }
    }
}
