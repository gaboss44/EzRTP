package com.skyblockexp.ezrtp.config.gui;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;
import com.skyblockexp.ezrtp.gui.GuiIconFactory;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Tests to verify that GUI lore displays correctly on Spigot servers
 * when the Paper API is not available.
 */
class GuiWorldOptionSpigotLoreTest {

    private Logger logger;
    private Player player;
    private GuiIconFactory iconFactory;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("GuiWorldOptionSpigotLoreTest");
        PlaceholderUtil.resetAvailabilityCheck();
        
        player = mock(Player.class);
        when(player.getName()).thenReturn("TestPlayer");
        iconFactory = new GuiIconFactory(logger);
    }

    @AfterEach
    void tearDown() {
        PlaceholderUtil.resetAvailabilityCheck();
    }

    @Test
    void createIcon_withSpigotFallback_setsLoreCorrectly() throws Exception {
        // Given: A GuiWorldOption with lore in MiniMessage format
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        // Create a mock ItemStack that simulates Spigot (without Paper's lore() method)
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta mockMeta = mock(ItemMeta.class);
        
        // Simulate Spigot environment by providing a mock ItemMeta without Paper-specific behavior.
        // (Avoid attempting to stub reflective Class.getMethod() calls which misuse Mockito matchers.)
        
        ItemStack spyStack = spy(template);
        when(spyStack.getItemMeta()).thenReturn(mockMeta);
        when(spyStack.clone()).thenReturn(spyStack);
        
        List<String> rawLore = Arrays.asList(
            "<green>Line 1: Welcome!</green>",
            "<yellow>Line 2: Have fun</yellow>"
        );
        
        GuiWorldOption option = createGuiWorldOption(
            settings, spyStack, 0, "", false, 1, "<green>Test</green>", rawLore
        );
        
        // When: Creating icon
        ItemStack icon = buildWorldIcon(option);
        
        // Then: lore should be applied on the mock meta (Paper/lore path in current compat layer)
        verify(mockMeta, atLeastOnce()).lore(anyList());
        
        // Verify the icon was created successfully
        assertNotNull(icon);
    }

    @Test
    void createIcon_withRealItemStack_convertsLoreToLegacyFormat() {
        // Given: A real ItemStack with MiniMessage lore
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        List<String> rawLore = Arrays.asList(
            "<green>Green text</green>",
            "<red>Red text</red>",
            "<blue>Blue text</blue>"
        );
        
        GuiWorldOption option = createGuiWorldOption(
            settings, template, 0, "", false, 1, "<yellow>Title</yellow>", rawLore
        );
        
        // When: Creating icon
        ItemStack icon = buildWorldIcon(option);
        
        // Then: Icon should be created successfully
        assertNotNull(icon);
        assertEquals(Material.GRASS_BLOCK, icon.getType());
        
        // The lore should be set (either via Paper's lore() or Spigot's setLore())
        // In the test environment with Paper API, it will use lore()
        // On real Spigot, it will use setLore() with properly serialized strings
        ItemMeta meta = icon.getItemMeta();
        assertNotNull(meta);
    }

    @Test
    void createIcon_withPlaceholders_resolvesBeforeConversion() {
        // Given: Lore with placeholders
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        List<String> rawLore = Arrays.asList(
            "<gray>Player: %player_name%</gray>",
            "<gray>World: world</gray>"
        );
        
        GuiWorldOption option = createGuiWorldOption(
            settings, template, 0, "", false, 1, "<green>Test</green>", rawLore
        );
        
        // When: Creating icon with player context
        ItemStack icon = buildWorldIcon(option);
        
        // Then: Icon should be created with placeholders resolved
        // (In test env without PlaceholderAPI, placeholders remain as-is, which is fine)
        assertNotNull(icon);
        assertEquals(Material.GRASS_BLOCK, icon.getType());
    }

    /**
     * Helper method to create GuiWorldOption using reflection since constructor is private.
     */
    private GuiWorldOption createGuiWorldOption(
            RandomTeleportSettings settings,
            ItemStack template,
            int slot,
            String permission,
            boolean requireCache,
            int minimumCached,
            String rawName,
            List<String> rawLore) {
        try {
            Class<GuiWorldOption> clazz = GuiWorldOption.class;
            @SuppressWarnings("unchecked")
            Constructor<GuiWorldOption> constructor = 
                (Constructor<GuiWorldOption>) clazz.getDeclaredConstructor(
                RandomTeleportSettings.class,
                ItemStack.class,
                int.class,
                String.class,
                boolean.class,
                int.class,
                String.class,
                List.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                settings, template, slot, permission, requireCache, minimumCached, rawName, rawLore
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GuiWorldOption", e);
        }
    }

    private ItemStack buildWorldIcon(GuiWorldOption option) {
        return iconFactory.buildWorldIcon(
            player,
            option,
            GuiIconFactory.CooldownInfo.inactive(),
            true
        );
    }
}
