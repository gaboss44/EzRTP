package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EzRtpAPIIntegrationTest {

    @Test
    void rtpPlayer_withRealSettings_delegatesToService() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        RandomTeleportService service = mock(RandomTeleportService.class);
        EzRtpPlugin ez = mock(EzRtpPlugin.class);
        when(ez.getTeleportService()).thenReturn(service);
        when(pluginManager.getPlugin("EzRTP")).thenReturn(ez);

        try {
            var mocked = org.mockito.Mockito.mockStatic(org.bukkit.Bukkit.class);
            try (org.mockito.MockedStatic<org.bukkit.Bukkit> ignored = mocked) {
                mocked.when(org.bukkit.Bukkit::getPluginManager).thenReturn(pluginManager);
                mocked.when(org.bukkit.Bukkit::getLogger).thenReturn(java.util.logging.Logger.getLogger("EzRTP-Test"));

                Player player = mock(Player.class);
                RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(null, java.util.logging.Logger.getLogger("EzRTP-Test"));

                EzRtpAPI.rtpPlayer(player, settings);
                verify(service).teleportPlayer(player, settings, TeleportReason.COMMAND);
            }
        } catch (org.mockito.exceptions.base.MockitoException ex) {
            // Static mocking unavailable — fall back to reflection-based server replacement
            Field serverField = null;
            Object previousServer = null;
            try {
                serverField = Bukkit.class.getDeclaredField("server");
                serverField.setAccessible(true);
                previousServer = serverField.get(null);
                serverField.set(null, server);

                Player player = mock(Player.class);
                RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(null, java.util.logging.Logger.getLogger("EzRTP-Test"));

                EzRtpAPI.rtpPlayer(player, settings);
                verify(service).teleportPlayer(player, settings, TeleportReason.COMMAND);
            } finally {
                try { if (serverField != null) serverField.set(null, previousServer); } catch (Throwable ignored) {}
            }
        }
    }
}
