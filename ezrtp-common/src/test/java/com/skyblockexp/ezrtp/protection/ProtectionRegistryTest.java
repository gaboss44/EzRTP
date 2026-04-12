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

        // When ALL configured providers are missing, the new aggregated warning fires.
        // It contains "not be avoided" to signal that claims will not be respected.
        boolean found = messages.stream().anyMatch(m -> {
            String lower = m.toLowerCase(Locale.ROOT);
            return lower.contains("configured but not available")
                    || lower.contains("not be avoided");
        });
        assertTrue(found, "Expected a warning about providers not being available, got: " + messages);
    }

    @Test
    void warnMissingProviders_noWarningWhenAvoidClaimsFalse() {
        PluginManager pm = Mockito.mock(PluginManager.class);
        List<String> messages = new ArrayList<>();
        Logger testLogger = Logger.getLogger("ProtectionRegistryTest-noWarn-" + System.nanoTime());
        testLogger.setUseParentHandlers(false);
        testLogger.addHandler(new Handler() {
            @Override public void publish(LogRecord record) {
                if (record != null && record.getMessage() != null) messages.add(record.getMessage());
            }
            @Override public void flush() { }
            @Override public void close() { }
        });

        ProtectionRegistry registry = new ProtectionRegistry(pm, testLogger);
        ProtectionSettings settings = new ProtectionSettings(false, List.of("worldguard", "griefprevention"));

        registry.warnMissingProviders(settings);

        assertTrue(messages.isEmpty(), "No warnings should be emitted when avoid-claims is false");
    }
}
