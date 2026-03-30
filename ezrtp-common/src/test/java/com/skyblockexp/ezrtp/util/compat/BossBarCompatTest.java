package com.skyblockexp.ezrtp.util.compat;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossBarCompatTest {

    @Test
    void createReturnsWrapperEvenWhenUnsupported_andMethodsAreNoOps() {
        BossBarCompat.Wrapper wrapper = BossBarCompat.create("test", BarColor.WHITE, BarStyle.SOLID);
        // In unit tests the Bukkit#createBossBar reflective call will usually fail,
        // so wrapper should be present but not supported. Methods must not throw.
        assertNotNull(wrapper);
        assertFalse(wrapper.isSupported());

        // these should be no-ops and not throw
        wrapper.setTitle(net.kyori.adventure.text.Component.text("hello"));
        wrapper.setColor(BarColor.PINK);
        wrapper.setStyle(BarStyle.SOLID);
        wrapper.setProgress(0.5);
        wrapper.addPlayer(null);
        wrapper.removeAll();
        assertTrue(wrapper.getPlayers().isEmpty());
    }
}
