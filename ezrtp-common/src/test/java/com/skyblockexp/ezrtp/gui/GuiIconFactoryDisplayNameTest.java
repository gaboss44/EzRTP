package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.ItemMetaCompat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests ensuring GUI icon display names do not contain raw MiniMessage tags
 * and that colorized text is present in the resulting legacy display name.
 */
class GuiIconFactoryDisplayNameTest {

    private Logger logger;
    private Player player;
    private GuiIconFactory iconFactory;

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("GuiIconFactoryDisplayNameTest");
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
    void buildWorldIcon_overworldName_noRawMiniMessageTags() {
        // Name taken from gui.yml example with gradient and bold tags
        String rawName = "<gradient:#7ed957:#2ecc71><bold>Overworld</bold></gradient>";
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);

        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
                mock(com.skyblockexp.ezrtp.config.RandomTeleportSettings.class),
                template, 22, "", false, 1, rawName, List.of("<gray>Click to begin your adventure.</gray>")
        );

        ItemStack icon = iconFactory.buildWorldIcon(player, option, GuiIconFactory.CooldownInfo.inactive(), true);
        assertNotNull(icon, "Icon should be created");
        ItemMeta meta = icon.getItemMeta();
        assertNotNull(meta, "ItemMeta should be present");

        String display = readDisplayName(meta);
        assertNotNull(display, "Display name should be set");
        assertFalse(display.contains("<gradient"), "Display name must not contain raw MiniMessage tags");
        String plain = display.replaceAll("(?i)<[^>]+>", "");
        assertTrue(plain.toLowerCase().contains("overworld"), "Display name should contain 'Overworld' text after stripping tags, got: " + display);
    }

    @Test
    void buildWorldIcon_blankRawName_preservesTemplateDisplayName() {
        ItemStack template = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta templateMeta = template.getItemMeta();
        assertNotNull(templateMeta, "Template ItemMeta should be present");
        ItemMetaCompat.setDisplayName(templateMeta, MessageUtil.parseMiniMessage("<green>Template Name</green>"));
        template.setItemMeta(templateMeta);

        EzRtpConfiguration.GuiWorldOption option = createGuiWorldOption(
                mock(com.skyblockexp.ezrtp.config.RandomTeleportSettings.class),
                template, 22, "", false, 1, "   ", List.of()
        );

        ItemStack icon = iconFactory.buildWorldIcon(player, option, GuiIconFactory.CooldownInfo.inactive(), true);
        assertNotNull(icon, "Icon should be created");
        ItemMeta meta = icon.getItemMeta();
        assertNotNull(meta, "ItemMeta should be present");

        String display = readDisplayName(meta);
        assertNotNull(display, "Display name should still be present");
        assertTrue(display.contains("Template Name"), "Blank raw names should not erase template display names");
    }

    private static String readDisplayName(ItemMeta meta) {
        try {
            Object result = meta.getClass().getMethod("displayName").invoke(meta);
            if (result instanceof net.kyori.adventure.text.Component comp) {
                return MessageUtil.serializeToMiniMessage(comp);
            }
        } catch (Exception ignored) {}
        try {
            Object legacy = meta.getClass().getMethod("getDisplayName").invoke(meta);
            if (legacy instanceof String s) return s;
        } catch (Exception ignored) {}
        return null;
    }

    private EzRtpConfiguration.GuiWorldOption createGuiWorldOption(
            com.skyblockexp.ezrtp.config.RandomTeleportSettings settings,
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
            Constructor<EzRtpConfiguration.GuiWorldOption> constructor = (Constructor<EzRtpConfiguration.GuiWorldOption>) clazz
                    .getDeclaredConstructor(
                            com.skyblockexp.ezrtp.config.RandomTeleportSettings.class,
                            org.bukkit.inventory.ItemStack.class,
                            int.class,
                            String.class,
                            boolean.class,
                            int.class,
                            String.class,
                            List.class
                    );
            constructor.setAccessible(true);
            return constructor.newInstance(settings, template, slot, permission, requireCache, minimumCached, rawName, rawLore);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GuiWorldOption", e);
        }
    }
}
