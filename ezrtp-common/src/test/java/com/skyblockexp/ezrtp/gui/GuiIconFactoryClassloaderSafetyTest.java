package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import net.kyori.adventure.text.Component;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuiIconFactoryClassloaderSafetyTest {

    private GuiIconFactory iconFactory;
    private Player player;

    @BeforeEach
    void setUp() {
        PlaceholderUtil.resetAvailabilityCheck();
        PlatformGuiBridgeRegistry.unregister();
        iconFactory = new GuiIconFactory(Logger.getLogger("GuiIconFactoryClassloaderSafetyTest"));

        player = mock(Player.class);
        when(player.getName()).thenReturn("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        PlaceholderUtil.resetAvailabilityCheck();
        PlatformGuiBridgeRegistry.unregister();
    }

    @Test
    void buildWorldIcon_handlesAdventureClassCastFromItemMetaMethods() {
        RandomTeleportSettings settings = mock(RandomTeleportSettings.class);
        when(settings.getWorldName()).thenReturn("world");

        ItemStack stack = spy(new ItemStack(Material.GRASS_BLOCK));
        ItemMeta meta = mock(ItemMeta.class);
        when(stack.clone()).thenReturn(stack);
        when(stack.getItemMeta()).thenReturn(meta);

        doThrow(new ClassCastException("simulated component classloader mismatch"))
                .when(meta).displayName(any(Component.class));
        doThrow(new ClassCastException("simulated component classloader mismatch"))
                .when(meta).lore(anyList());

        GuiWorldOption option = createGuiWorldOption(
                settings,
                stack,
                10,
                "",
                false,
                1,
                "<green>Overworld</green>",
                List.of("<gray>Click to teleport</gray>")
        );

        ItemStack built = assertDoesNotThrow(
                () -> iconFactory.buildWorldIcon(player, option, GuiIconFactory.CooldownInfo.inactive(), true),
                "GUI icon creation should not propagate ClassCastException from Adventure meta methods"
        );

        verify(meta).displayName(any(Component.class));
        verify(meta).lore(anyList());
        verify(stack, atLeastOnce()).setItemMeta(meta);
        assertNotNull(built);
    }

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
            return constructor.newInstance(settings, template, slot, permission, requireCache, minimumCached, rawName, rawLore);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create GuiWorldOption", e);
        }
    }
}
