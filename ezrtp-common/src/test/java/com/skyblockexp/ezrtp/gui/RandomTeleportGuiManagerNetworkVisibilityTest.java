package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.NetworkConfiguration;
import com.skyblockexp.ezrtp.config.NetworkConfiguration.NetworkServer;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.network.NetworkService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.when;

class RandomTeleportGuiManagerNetworkVisibilityTest {

        @Test
        void hideWhenOfflineServerRemainsVisibleAndAllowsTransferWhenStatusUnknown() throws Exception {
                // Deregister any previous static mock for Bukkit
                try { org.mockito.Mockito.framework().clearInlineMocks(); } catch (Throwable ignored) {}
        NetworkServer server = createServer(true, false, 3);
        NetworkConfiguration networkConfiguration = createNetworkConfiguration(server);
        EzRtpConfiguration configuration = createEzRtpConfiguration(networkConfiguration, server);

        NetworkService networkService = mock(NetworkService.class);
        NetworkConfiguration.ServerStatusSnapshot unknown = NetworkConfiguration.ServerStatusSnapshot.unknown();
        when(networkService.getStatus(server)).thenReturn(unknown);
        when(networkService.createIcon(server)).thenReturn(new ItemStack(Material.ENDER_PEARL));

        com.skyblockexp.ezrtp.storage.RtpUsageStorage usageStorage = mock(com.skyblockexp.ezrtp.storage.RtpUsageStorage.class);
        RandomTeleportGuiManager manager = new RandomTeleportGuiManager(null, () -> null,
                () -> configuration, () -> networkService, () -> mock(MessageProvider.class), usageStorage);

        ItemStack[] contents = new ItemStack[9];
        Inventory inventory = mockInventory(contents);
        UUID playerId = UUID.randomUUID();
        Player player = mockPlayer(playerId);
        InventoryView view = mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(inventory);

        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getView()).thenReturn(view);
        when(event.getRawSlot()).thenReturn(server.getSlot());

        try (MockedStatic<Bukkit> mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(() -> Bukkit.createInventory(any(InventoryHolder.class), eq(9), any(Component.class)))
                    .thenReturn(inventory);

            boolean opened = manager.openSelection(player);
            assertTrue(opened, "GUI should open when options are available");
            assertNotNull(contents[server.getSlot()],
                    "Server icon should be placed while status is unknown");

            manager.onInventoryClick(event);

            verify(networkService).transferPlayer(player, server);
        }

        verify(event).setCancelled(true);
        verify(player, never()).sendMessage("§cThat server is currently offline.");
    }

        @Test
        void hideWhenOfflineServerDisappearsAndBlocksTransfersWhenOffline() throws Exception {
                // Deregister any previous static mock for Bukkit
                try { org.mockito.Mockito.framework().clearInlineMocks(); } catch (Throwable ignored) {}
        NetworkServer server = createServer(true, false, 4);
        NetworkConfiguration networkConfiguration = createNetworkConfiguration(server);
        EzRtpConfiguration configuration = createEzRtpConfiguration(networkConfiguration, server);

        // Scenario 1: Opening while offline hides the option entirely.
        NetworkService offlineService = mock(NetworkService.class);
        when(offlineService.getStatus(server)).thenReturn(NetworkConfiguration.ServerStatusSnapshot.offline());
        when(offlineService.createIcon(server)).thenReturn(new ItemStack(Material.ENDER_PEARL));

        com.skyblockexp.ezrtp.storage.RtpUsageStorage usageStorage = mock(com.skyblockexp.ezrtp.storage.RtpUsageStorage.class);
        RandomTeleportGuiManager manager = new RandomTeleportGuiManager(null, () -> null,
                () -> configuration, () -> offlineService, () -> mock(MessageProvider.class), usageStorage);

        ItemStack[] offlineContents = new ItemStack[9];
        Inventory offlineInventory = mockInventory(offlineContents);
        Player offlinePlayer = mockPlayer(UUID.randomUUID());

        try (MockedStatic<Bukkit> mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(() -> Bukkit.createInventory(any(InventoryHolder.class), eq(9), any(Component.class)))
                    .thenReturn(offlineInventory);

            boolean opened = manager.openSelection(offlinePlayer);
            assertTrue(opened, "GUI should still report opening even when no options remain");
        }

        assertNull(offlineContents[server.getSlot()],
                "Server icon should be hidden once marked offline");
        ArgumentCaptor<String> offlineCaptor = ArgumentCaptor.forClass(String.class);
        verify(offlinePlayer).sendMessage(offlineCaptor.capture());
        assertTrue(offlineCaptor.getValue().endsWith("§cNo teleport destinations are currently available."));

        // Scenario 2: Status flips to offline after the GUI was populated.
        NetworkService changingService = mock(NetworkService.class);
        when(changingService.getStatus(server)).thenReturn(
                NetworkConfiguration.ServerStatusSnapshot.unknown(),
                NetworkConfiguration.ServerStatusSnapshot.offline());
        when(changingService.createIcon(server)).thenReturn(new ItemStack(Material.ENDER_PEARL));

        com.skyblockexp.ezrtp.storage.RtpUsageStorage usageStorage2 = mock(com.skyblockexp.ezrtp.storage.RtpUsageStorage.class);
        RandomTeleportGuiManager switchingManager = new RandomTeleportGuiManager(null, () -> null,
                () -> configuration, () -> changingService, () -> mock(MessageProvider.class), usageStorage2);

        ItemStack[] contents = new ItemStack[9];
        Inventory inventory = mockInventory(contents);
        UUID playerId = UUID.randomUUID();
        Player player = mockPlayer(playerId);
        InventoryView view = mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(inventory);

        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getView()).thenReturn(view);
        when(event.getRawSlot()).thenReturn(server.getSlot());

        try (MockedStatic<Bukkit> mockedBukkit = mockStatic(Bukkit.class)) {
            mockedBukkit.when(() -> Bukkit.createInventory(any(InventoryHolder.class), eq(9), any(Component.class)))
                    .thenReturn(inventory);

            boolean opened = switchingManager.openSelection(player);
            assertTrue(opened, "GUI should open while the server is still pending status");
        }

        assertNotNull(contents[server.getSlot()],
                "Server icon should be visible before the offline state is detected");

        switchingManager.onInventoryClick(event);

        verify(event).setCancelled(true);
        ArgumentCaptor<String> playerCaptor = ArgumentCaptor.forClass(String.class);
        verify(player).sendMessage(playerCaptor.capture());
        assertTrue(playerCaptor.getValue().endsWith("§cThat server is currently offline."));
        verify(changingService, never()).transferPlayer(player, server);
    }

    private static Inventory mockInventory(ItemStack[] contents) {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getSize()).thenReturn(contents.length);
        when(inventory.getItem(anyInt())).thenAnswer(invocation -> contents[(int) invocation.getArgument(0)]);
        doAnswer(invocation -> {
            contents[(int) invocation.getArgument(0)] = (ItemStack) invocation.getArgument(1);
            return null;
        }).when(inventory).setItem(anyInt(), any(ItemStack.class));
        return inventory;
    }

    private static Player mockPlayer(UUID uuid) {
        Player player = mock(Player.class);
        when(player.hasPermission(anyString())).thenReturn(true);
        when(player.getUniqueId()).thenReturn(uuid);
        return player;
    }

    private static NetworkConfiguration createNetworkConfiguration(NetworkServer server) throws Exception {
        Constructor<NetworkConfiguration> constructor = NetworkConfiguration.class.getDeclaredConstructor(
                boolean.class, boolean.class, long.class, int.class, List.class);
        constructor.setAccessible(true);
        List<NetworkServer> servers = new ArrayList<>();
        servers.add(server);
        return constructor.newInstance(true, true, 200L, 1500, Collections.unmodifiableList(servers));
    }

    private static EzRtpConfiguration createEzRtpConfiguration(NetworkConfiguration networkConfiguration,
                                                               NetworkServer server) throws Exception {
        EzRtpConfiguration.GuiServerOption serverOption = createGuiServerOption(server);
        EzRtpConfiguration.GuiSettings guiSettings = createGuiSettings(serverOption);

        // Provide a minimal valid RandomTeleportSettings mock with a real config section
        com.skyblockexp.ezrtp.config.RandomTeleportSettings mockSettings = mock(com.skyblockexp.ezrtp.config.RandomTeleportSettings.class);
        org.bukkit.configuration.file.YamlConfiguration realConfig = new org.bukkit.configuration.file.YamlConfiguration();
        when(mockSettings.getConfigSection()).thenReturn(realConfig);

        Constructor<EzRtpConfiguration> constructor = EzRtpConfiguration.class.getDeclaredConstructor(
                com.skyblockexp.ezrtp.config.RandomTeleportSettings.class,
                EzRtpConfiguration.GuiSettings.class,
                com.skyblockexp.ezrtp.config.TeleportQueueSettings.class,
                NetworkConfiguration.class);
        constructor.setAccessible(true);
        return constructor.newInstance(mockSettings, guiSettings, null, networkConfiguration);
    }

    private static EzRtpConfiguration.GuiServerOption createGuiServerOption(NetworkServer server) throws Exception {
        Constructor<EzRtpConfiguration.GuiServerOption> constructor = EzRtpConfiguration.GuiServerOption.class
                .getDeclaredConstructor(NetworkServer.class);
        constructor.setAccessible(true);
        return constructor.newInstance(server);
    }

    private static EzRtpConfiguration.GuiSettings createGuiSettings(
            EzRtpConfiguration.GuiServerOption serverOption) throws Exception {
        Constructor<EzRtpConfiguration.GuiSettings> constructor = EzRtpConfiguration.GuiSettings.class
                .getDeclaredConstructor(boolean.class, Component.class, int.class, ItemStack.class,
                        List.class, List.class, Component.class, boolean.class);
        constructor.setAccessible(true);
        List<EzRtpConfiguration.GuiServerOption> serverOptions = new ArrayList<>();
        serverOptions.add(serverOption);
        return constructor.newInstance(true, Component.text("Select"), 9, null,
                Collections.emptyList(), Collections.unmodifiableList(serverOptions),
                Component.empty(), false);
    }

    private static NetworkServer createServer(boolean hideWhenOffline, boolean allowWhenOffline, int slot)
            throws Exception {
        Constructor<NetworkConfiguration.IconTemplate> iconConstructor = NetworkConfiguration.IconTemplate.class
                .getDeclaredConstructor(Material.class, int.class, Integer.class, String.class, List.class);
        iconConstructor.setAccessible(true);
        NetworkConfiguration.IconTemplate iconTemplate = iconConstructor.newInstance(Material.ENDER_PEARL, 1,
                null, "<green><server></green>", Collections.emptyList());

        Constructor<NetworkServer> constructor = NetworkServer.class.getDeclaredConstructor(String.class,
                String.class, String.class, int.class, String.class, int.class, String.class, boolean.class,
                boolean.class, Component.class, Component.class, NetworkConfiguration.IconTemplate.class);
        constructor.setAccessible(true);
        return constructor.newInstance("test", "test", "127.0.0.1", 25565, "", slot, "Test",
                hideWhenOffline, allowWhenOffline, Component.empty(), Component.empty(), iconTemplate);
    }
}

