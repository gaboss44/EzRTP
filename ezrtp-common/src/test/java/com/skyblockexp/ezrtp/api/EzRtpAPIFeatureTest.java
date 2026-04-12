package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EzRtpAPIFeatureTest {

    @Test
    void rtpPlayer_withSettings_delegatesToService() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        RandomTeleportService service = mock(RandomTeleportService.class);
        EzRtpPlugin ez = mock(EzRtpPlugin.class);
        when(ez.getTeleportService()).thenReturn(service);
        when(pluginManager.getPlugin("EzRTP")).thenReturn(ez);

        java.lang.reflect.Field serverField = null;
        Object previousServer = null;
        try {
            serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            previousServer = serverField.get(null);
            serverField.set(null, server);

            Player player = mock(Player.class);
            RandomTeleportSettings settings = mock(RandomTeleportSettings.class);

            // Call the API variant that accepts a settings object
            EzRtpAPI.rtpPlayer(player, settings);

            verify(service).teleportPlayer(eq(player), any(RandomTeleportSettings.class), eq(TeleportReason.COMMAND));
        } finally {
            try {
                if (serverField != null) serverField.set(null, previousServer);
            } catch (Throwable ignored) {}
        }
    }

    @Test
    void rtpPlayer_withCallback_receivesCallbackResult() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        RandomTeleportService service = mock(RandomTeleportService.class);
        EzRtpPlugin ez = mock(EzRtpPlugin.class);
        when(ez.getTeleportService()).thenReturn(service);
        when(pluginManager.getPlugin("EzRTP")).thenReturn(ez);

        java.lang.reflect.Field serverField = null;
        Object previousServer = null;
        try {
            serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            previousServer = serverField.get(null);
            serverField.set(null, server);

            Player player = mock(Player.class);
            com.skyblockexp.ezrtp.config.RandomTeleportSettings settings = mock(com.skyblockexp.ezrtp.config.RandomTeleportSettings.class);

            AtomicBoolean callbackFired = new AtomicBoolean(false);
            Consumer<Boolean> callback = success -> callbackFired.set(success);

            // Arrange service to invoke the callback with 'true' when called with settings
            doAnswer(invocation -> {
                Consumer<Boolean> cb = invocation.getArgument(3);
                cb.accept(true);
                return null;
            }).when(service).teleportPlayer(any(Player.class), any(com.skyblockexp.ezrtp.config.RandomTeleportSettings.class), any(TeleportReason.class), any(Consumer.class));

            EzRtpAPI.rtpPlayer(player, settings, callback);

            // verify that callback was invoked with true
            assert callbackFired.get();
        } finally {
            try {
                if (serverField != null) serverField.set(null, previousServer);
            } catch (Throwable ignored) {}
        }
    }

    @Test
    void isAvailable_and_getTeleportService_handleMissingPlugin() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(pluginManager.getPlugin("EzRTP")).thenReturn(null);

        java.lang.reflect.Field serverField = null;
        Object previousServer = null;
        try {
            serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            previousServer = serverField.get(null);
            serverField.set(null, server);

            assert !EzRtpAPI.isAvailable();
            assert EzRtpAPI.getTeleportService() == null;
        } finally {
            try { if (serverField != null) serverField.set(null, previousServer); } catch (Throwable ignored) {}
        }
    }

    @Test
    void rtpPlayer_withNullSettings_usesDefaultTeleportPath() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        RandomTeleportService service = mock(RandomTeleportService.class);
        EzRtpPlugin ez = mock(EzRtpPlugin.class);
        when(ez.getTeleportService()).thenReturn(service);
        when(pluginManager.getPlugin("EzRTP")).thenReturn(ez);

        java.lang.reflect.Field serverField = null;
        Object previousServer = null;
        try {
            serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            previousServer = serverField.get(null);
            serverField.set(null, server);

            Player player = mock(Player.class);
            EzRtpAPI.rtpPlayer(player, (com.skyblockexp.ezrtp.config.RandomTeleportSettings) null);
            verify(service).teleportPlayer(player, TeleportReason.COMMAND);
        } finally {
            try { if (serverField != null) serverField.set(null, previousServer); } catch (Throwable ignored) {}
        }
    }

    @Test
    void rtpPlayer_whenServiceMissing_invokesCallbackWithFalse() throws Exception {
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        EzRtpPlugin ez = mock(EzRtpPlugin.class);
        when(ez.getTeleportService()).thenReturn(null);
        when(pluginManager.getPlugin("EzRTP")).thenReturn(ez);

        try (org.mockito.MockedStatic<org.bukkit.Bukkit> mocked = org.mockito.Mockito.mockStatic(org.bukkit.Bukkit.class)) {
            mocked.when(org.bukkit.Bukkit::getPluginManager).thenReturn(pluginManager);
            mocked.when(org.bukkit.Bukkit::getLogger).thenReturn(java.util.logging.Logger.getLogger("EzRTP-Test"));

            Player player = mock(Player.class);
            AtomicBoolean invoked = new AtomicBoolean(false);
            AtomicBoolean value = new AtomicBoolean(true);
            java.util.function.Consumer<Boolean> cb = success -> {
                invoked.set(true);
                value.set(success);
            };

            EzRtpAPI.rtpPlayer(player, (com.skyblockexp.ezrtp.config.RandomTeleportSettings) null, cb);

            assert invoked.get();
            assert !value.get();
        }
    }
}
