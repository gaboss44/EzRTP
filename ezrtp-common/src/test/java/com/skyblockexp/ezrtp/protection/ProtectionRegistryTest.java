package com.skyblockexp.ezrtp.protection;

import com.skyblockexp.ezrtp.config.ProtectionSettings;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ProtectionRegistryTest {

    @Test
    void findProtectionProvider_returnsEmptyWhenAvoidClaimsFalse() {
        PluginManager pm = Mockito.mock(PluginManager.class);
        Logger logger = Logger.getAnonymousLogger();
        ProtectionRegistry registry = new ProtectionRegistry(pm, logger);

        ProtectionSettings settings = new ProtectionSettings(false, List.of("griefprevention"));
        Location loc = null; // method should return empty without needing a real location

        assertFalse(registry.findProtectionProvider(loc, settings).isPresent());
    }

    @Test
    void warnMissingProviders_logsWarningForUnavailableProviders() {
        PluginManager pm = Mockito.mock(PluginManager.class);
        // Ensure pm.getPlugin("WorldGuard") and getPlugin("GriefPrevention") return null by default

        List<String> messages = new ArrayList<>();
        Logger testLogger = Logger.getLogger("ProtectionRegistryTest-" + System.nanoTime());
        testLogger.setUseParentHandlers(false);
        testLogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record == null || record.getMessage() == null) return;
                messages.add(record.getLevel() + ": " + record.getMessage());
            }

            @Override
            public void flush() { }

            @Override
            public void close() throws SecurityException { }
        });

        ProtectionRegistry registry = new ProtectionRegistry(pm, testLogger);
        ProtectionSettings settings = new ProtectionSettings(true, List.of("worldguard", "griefprevention"));

        registry.warnMissingProviders(settings);

        boolean found = messages.stream().anyMatch(m -> m.toLowerCase(Locale.ROOT).contains("configured but not available")
                || m.toLowerCase(Locale.ROOT).contains("configured but not available;"));
        assertTrue(found, "Expected a warning about configured but not available providers, got: " + messages);
    }
}
