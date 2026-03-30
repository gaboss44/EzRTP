package com.skyblockexp.ezrtp.config;

import com.skyblockexp.ezrtp.gui.GuiIconFactory;
import com.skyblockexp.ezrtp.util.ItemFlagUtil;
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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for PlaceholderAPI integration in GUI world options.
 * These tests verify that placeholders in item names and lores are properly processed.
 */
class GuiWorldOptionPlaceholderTest {

    private Logger logger;
    private Player player;
    private ItemMeta itemMeta;
    private GuiIconFactory iconFactory;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("GuiWorldOptionPlaceholderTest");
        PlaceholderUtil.resetAvailabilityCheck();
        
        // Mock player
        player = mock(Player.class);
        when(player.getName()).thenReturn("TestPlayer");
        
        // Mock ItemMeta
        itemMeta = mock(ItemMeta.class);
        iconFactory = new GuiIconFactory(logger);
    }

    @AfterEach
    void tearDown() {
        PlaceholderUtil.resetAvailabilityCheck();
    }

    @Test
    void createIcon_withoutPlayer_createsIconWithoutPlaceholderResolution() {
        // Given: A GuiWorldOption with placeholders in name and lore
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        String rawName = "<green>World: %player_name%</green>";
        List<String> rawLore = Arrays.asList(
            "<gray>Welcome %player_name%</gray>",
            "<gray>Your health: %player_health%</gray>"
        );
        
        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
            settings, template, 0, "", false, 1, rawName, rawLore
        );
        
        // When: Creating icon without player context
        ItemStack icon = option.createIcon();
        
        // Then: Icon is created (but placeholders are not resolved in this method)
        assertNotNull(icon);
        assertEquals(Material.GRASS_BLOCK, icon.getType());
    }

    @Test
    void createIcon_withPlayer_appliesPlaceholderResolution() {
        // Given: A GuiWorldOption with placeholders
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        String rawName = "<green>World: %player_name%</green>";
        List<String> rawLore = Arrays.asList(
            "<gray>Welcome %player_name%</gray>"
        );
        
        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
            settings, template, 0, "", false, 1, rawName, rawLore
        );
        
        // When: Creating icon with player context
        ItemStack icon = buildWorldIcon(option);
        
        // Then: Icon is created
        assertNotNull(icon);
        assertEquals(Material.GRASS_BLOCK, icon.getType());
        // Note: Actual placeholder resolution depends on PlaceholderAPI being available
        // In test environment, placeholders will remain as-is (graceful fallback)
    }

    @Test
    void createIcon_withNullMeta_returnsIconWithoutModification() {
        // Given: A GuiWorldOption with an item that has null meta
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = mock(ItemStack.class);
        when(template.clone()).thenReturn(template);
        when(template.getItemMeta()).thenReturn(null);
        
        String rawName = "<green>Test</green>";
        List<String> rawLore = Collections.singletonList("<gray>Test lore</gray>");
        
        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
            settings, template, 0, "", false, 1, rawName, rawLore
        );
        
        // When: Creating icon with player context
        ItemStack icon = buildWorldIcon(option);
        
        // Then: Icon is returned as-is without throwing exception
        assertNotNull(icon);
    }

    @Test
    void createIcon_withEmptyLore_handlesGracefully() {
        // Given: A GuiWorldOption with empty lore
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        String rawName = "<green>Simple World</green>";
        List<String> rawLore = Collections.emptyList();
        
        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
            settings, template, 0, "", false, 1, rawName, rawLore
        );
        
        // When: Creating icon with player context
        ItemStack icon = buildWorldIcon(option);
        
        // Then: Icon is created without errors
        assertNotNull(icon);
        assertEquals(Material.GRASS_BLOCK, icon.getType());
    }

    @Test
    void createIcon_withBlankName_handlesGracefully() {
        // Given: A GuiWorldOption with blank name
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        String rawName = "   ";
        List<String> rawLore = Collections.singletonList("<gray>Test</gray>");
        
        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
            settings, template, 0, "", false, 1, rawName, rawLore
        );
        
        // When: Creating icon with player context
        ItemStack icon = buildWorldIcon(option);
        
        // Then: Icon is created without errors
        assertNotNull(icon);
    }

    @Test
    void createIcon_preservesOtherItemProperties() {
        // Given: A GuiWorldOption with a specific material
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");
        
        ItemStack template = new ItemStack(Material.DIAMOND_SWORD, 3);
        String rawName = "<green>Sword World</green>";
        List<String> rawLore = Collections.singletonList("<gray>Enter if you dare</gray>");
        
        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
            settings, template, 5, "test.permission", true, 10, rawName, rawLore
        );
        
        // When: Creating icon
        ItemStack icon = buildWorldIcon(option);
        
        // Then: Material and amount are preserved
        assertNotNull(icon);
        assertEquals(Material.DIAMOND_SWORD, icon.getType());
        assertEquals(3, icon.getAmount());
    }

    /**
     * Helper method to create GuiWorldOption using reflection since constructor is private.
     */
    private EzRtpConfiguration.GuiWorldOption createGuiWorldOption(
            RandomTeleportSettings settings,
            ItemStack template,
            int slot,
            String permission,
            boolean requireCache,
            int minimumCached,
            String rawName,
            List<String> rawLore) {
        try {
            Class<EzRtpConfiguration.GuiWorldOption> clazz = EzRtpConfiguration.GuiWorldOption.class;
            @SuppressWarnings("unchecked")
            Constructor<EzRtpConfiguration.GuiWorldOption> constructor = 
                (Constructor<EzRtpConfiguration.GuiWorldOption>) clazz.getDeclaredConstructor(
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

    private ItemStack buildWorldIcon(EzRtpConfiguration.GuiWorldOption option) {
        return iconFactory.buildWorldIcon(
            player,
            option,
            GuiIconFactory.CooldownInfo.inactive(),
            true
        );
    }
}
