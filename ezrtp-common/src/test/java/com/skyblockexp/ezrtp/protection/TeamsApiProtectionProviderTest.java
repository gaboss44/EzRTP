package com.skyblockexp.ezrtp.protection;

import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class TeamsApiProtectionProviderTest {

    @Test
    void getId_returnsTeamsapi() {
        PluginManager pm = Mockito.mock(PluginManager.class);
        TeamsApiProtectionProvider provider = new TeamsApiProtectionProvider(pm, Logger.getAnonymousLogger());
        assertEquals("teamsapi", provider.getId());
    }

    @Test
    void isAvailable_falseWhenPluginNotPresent() {
        PluginManager pm = Mockito.mock(PluginManager.class);
        // pm.getPlugin("TeamsAPI") returns null by default — plugin not installed
        TeamsApiProtectionProvider provider = new TeamsApiProtectionProvider(pm, Logger.getAnonymousLogger());
        assertFalse(provider.isAvailable(),
                "Provider should be unavailable when TeamsAPI plugin is not installed");
    }

    @Test
    void isLocationProtected_returnsFalseWhenPluginNotPresent() {
        PluginManager pm = Mockito.mock(PluginManager.class);
        TeamsApiProtectionProvider provider = new TeamsApiProtectionProvider(pm, Logger.getAnonymousLogger());
        // Should not throw and should return false when plugin is absent
        assertFalse(provider.isLocationProtected(null));
    }

    @Test
    void isLocationProtected_returnsFalseForNullLocation() {
        PluginManager pm = Mockito.mock(PluginManager.class);
        TeamsApiProtectionProvider provider = new TeamsApiProtectionProvider(pm, Logger.getAnonymousLogger());
        assertFalse(provider.isLocationProtected(null));
    }

    @Test
    void constructor_toleratesNullPluginManager() {
        // Must not throw even when no plugin manager is supplied
        TeamsApiProtectionProvider provider = new TeamsApiProtectionProvider(null, Logger.getAnonymousLogger());
        assertFalse(provider.isAvailable());
        assertFalse(provider.isLocationProtected(Mockito.mock(Location.class)));
    }
}
