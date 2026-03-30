package com.skyblockexp.ezrtp.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import static org.mockito.Mockito.*;

class EzRtpConfigurationTest {

    static {
        // Ensure Bukkit API classes are mocked for all tests
        try {
            mockStatic(org.bukkit.Bukkit.class);
        } catch (Throwable ignored) {}
    }

    private Logger logger;
    private CapturingHandler handler;

    @BeforeEach
    void setUpLogger() {
        logger = Logger.getLogger("EzRtpConfigurationTest");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        handler = new CapturingHandler();
        logger.addHandler(handler);
    }

    @AfterEach
    void tearDownLogger() {
        logger.removeHandler(handler);
    }

    @Test
    void duplicateServerSlotsAreIgnoredAndWarned() throws Exception {
        Map<Integer, EzRtpConfiguration.GuiWorldOption> worldOptions = new HashMap<>();
        worldOptions.put(1, null);

        NetworkConfiguration.NetworkServer serverA = createServer("alpha", 0);
        NetworkConfiguration.NetworkServer serverB = createServer("beta", 1);
        NetworkConfiguration.NetworkServer serverC = createServer("gamma", 0);

        NetworkConfiguration configuration = createConfiguration(serverA, serverB, serverC);

        Method parseServerOptions = EzRtpConfiguration.GuiSettings.class.getDeclaredMethod(
                "parseServerOptions", int.class, Map.class, NetworkConfiguration.class, Logger.class);
        parseServerOptions.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<EzRtpConfiguration.GuiServerOption> serverOptions =
                (List<EzRtpConfiguration.GuiServerOption>) parseServerOptions.invoke(null, 9, worldOptions,
                        configuration, logger);

        assertEquals(1, serverOptions.size(), "Only the first valid server should be available");
        assertEquals("alpha", serverOptions.get(0).getServer().getId(),
                "The GUI should retain the first server occupying the slot");

        List<String> warnings = handler.warnings();
        assertTrue(warnings.stream().anyMatch(message -> message.contains("beta")),
                "Conflicts with world options should emit a warning containing the server id");
        assertTrue(warnings.stream().anyMatch(message -> message.contains("gamma")),
                "Duplicate network server slots should emit a warning containing the server id");
    }

    @Test
    void iconTemplateUsesTemplateNameWhenServerDisplayNameEmpty() {
        Component expected = MiniMessage.miniMessage().deserialize("<gold>Lobby</gold>");

        // Minimal config for network
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("enabled", true);
        configuration.set("lobby", true);
        ConfigurationSection servers = configuration.createSection("servers");
        ConfigurationSection lobby = servers.createSection("lobby");
        lobby.set("bungee-server", "lobby");
        lobby.set("display-name", "");
        ConfigurationSection icon = lobby.createSection("icon");
        icon.set("material", "ENDER_PEARL");
        icon.set("name", "<gold>Lobby</gold>");

        NetworkConfiguration networkConfiguration = NetworkConfiguration.fromConfiguration(configuration, logger);
        NetworkConfiguration.NetworkServer server = networkConfiguration.getServers().get(0);
        NetworkConfiguration.ServerStatusSnapshot status = NetworkConfiguration.ServerStatusSnapshot.online(42L);

        // Use the real createIcon logic
        ItemStack iconStack = server.getIconTemplate().createIcon(status, server.getDisplayName());
        ItemMeta meta = iconStack.getItemMeta();
        Component displayName = readDisplayName(meta);

        assertNotNull(displayName, "The icon should have a display name when the template provides one");
        assertEquals(expected, displayName, "The icon should use the template name when provided");
    }

    private static NetworkConfiguration createConfiguration(NetworkConfiguration.NetworkServer... servers)
            throws Exception {
        Constructor<NetworkConfiguration> constructor = NetworkConfiguration.class.getDeclaredConstructor(
                boolean.class, boolean.class, long.class, int.class, List.class);
        constructor.setAccessible(true);
        List<NetworkConfiguration.NetworkServer> serverList = new ArrayList<>();
        Collections.addAll(serverList, servers);
        return constructor.newInstance(true, true, 200L, 1500, Collections.unmodifiableList(serverList));
    }

    private static NetworkConfiguration.NetworkServer createServer(String id, int slot) throws Exception {
        Constructor<NetworkConfiguration.IconTemplate> iconConstructor = NetworkConfiguration.IconTemplate.class
                .getDeclaredConstructor(Material.class, int.class, Integer.class, String.class, List.class);
        iconConstructor.setAccessible(true);
        NetworkConfiguration.IconTemplate iconTemplate = iconConstructor.newInstance(Material.ENDER_PEARL, 1,
            null, "<green><server></green>", Collections.emptyList());

        Constructor<NetworkConfiguration.NetworkServer> constructor = NetworkConfiguration.NetworkServer.class
                .getDeclaredConstructor(String.class, String.class, String.class, int.class, String.class, int.class,
                        String.class, boolean.class, boolean.class, Component.class, Component.class,
                        NetworkConfiguration.IconTemplate.class);
        constructor.setAccessible(true);
        return constructor.newInstance(id, id, "127.0.0.1", 25565, "", slot, id, false, false,
                Component.empty(), Component.empty(), iconTemplate);
    }

    private static Component readDisplayName(ItemMeta meta) {
        // Try both displayName() and getDisplayName() directly, catching exceptions
        try {
            // Paper/Adventure API
            Object result = meta.getClass().getMethod("displayName").invoke(meta);
            if (result instanceof Component component) {
                return component;
            }
        } catch (Exception ignored) {}
        try {
            // Bukkit/Spigot API
            Object legacyResult = meta.getClass().getMethod("getDisplayName").invoke(meta);
            if (legacyResult instanceof String str && str != null) {
                return MiniMessage.miniMessage().deserialize(str);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static final class CapturingHandler extends Handler {

        private final List<String> warnings = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                warnings.add(record.getMessage());
            }
        }

        @Override
        public void flush() {
            // No-op
        }

        @Override
        public void close() {
            // No-op
        }

        List<String> warnings() {
            return warnings;
        }
    }
}
