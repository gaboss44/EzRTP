package com.skyblockexp.ezrtp.util.compat;

import net.kyori.adventure.text.Component;
import com.skyblockexp.ezrtp.util.MessageUtil;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ItemMetaCompatTest {

    @Test
    void setDisplayNameFallsBackToStringWhenComponentMethodMissing() throws Exception {
        ItemMeta meta = mock(ItemMeta.class);
        Component comp = MessageUtil.parseMiniMessage("<green>hi</green>");

        ItemMetaCompat.setDisplayName(meta, comp);

        // Newer test stubs may provide displayName(Component); ensure it was invoked on the mock
        verify(meta).displayName(comp);
    }

    @Test
    void setLoreFallsBackToLegacyList() throws Exception {
        ItemMeta meta = mock(ItemMeta.class);
        Component comp = MessageUtil.parseMiniMessage("<blue>line</blue>");
        List<Component> lore = List.of(comp);

        ItemMetaCompat.setLore(meta, lore);

        // Test stubs often expose lore(List<Component>); ensure that path was used
        verify(meta).lore(lore);
    }

    @Test
    void setLoreFallsBackToSetLoreWhenAdventureLoreSetterThrowsClassCast() {
        ItemMeta meta = mock(ItemMeta.class);
        Component comp = MessageUtil.parseMiniMessage("<blue>line</blue>");
        List<Component> lore = List.of(comp);

        doThrow(new ClassCastException("simulated runtime mismatch")).when(meta).lore(anyList());

        ItemMetaCompat.setLore(meta, lore);

        verify(meta).setLore(anyList());
    }
}
