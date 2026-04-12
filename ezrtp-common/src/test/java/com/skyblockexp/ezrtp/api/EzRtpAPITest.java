package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EzRtpAPITest {

    static {
        // Ensure Bukkit is statically mocked so getTeleportService() can be exercised.
        // If another test class already registered the mock, this is a no-op.
        try {
            org.mockito.Mockito.mockStatic(Bukkit.class);
        } catch (Throwable ignored) {}
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTeleportServiceAndRtpPlayer_delegateToService() {
        RandomTeleportService service = mock(RandomTeleportService.class);
        RegisteredServiceProvider<TeleportService> rsp = mock(RegisteredServiceProvider.class);
        when(rsp.getProvider()).thenReturn(service);

        ServicesManager sm = mock(ServicesManager.class);
        when(sm.getRegistration(TeleportService.class)).thenReturn(rsp);

        // Bukkit is already statically mocked (by this class's static init or another test's init).
        // Adding a stub here is valid Mockito syntax when a static mock is already active.
        when(Bukkit.getServicesManager()).thenReturn(sm);

        Player player = mock(Player.class);

        TeleportService fetched = EzRtpAPI.getTeleportService();
        assertNotNull(fetched);

        EzRtpAPI.rtpPlayer(player);
        verify(service).teleportPlayer(player, TeleportReason.COMMAND);
    }
}
