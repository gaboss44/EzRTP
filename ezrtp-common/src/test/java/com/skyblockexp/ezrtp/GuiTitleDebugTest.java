package com.skyblockexp.ezrtp;

import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

public class GuiTitleDebugTest {

    @Test
    public void debugTitleSerialization() {
        String raw = "<gradient:#00b4db:#0083b0><bold>Random Teleport</bold></gradient>";
        Component c = MessageUtil.parseMiniMessage(raw);
        String legacy = MessageUtil.componentToLegacy(c);
        String mm = MessageUtil.serializeToMiniMessage(c);
        String alt = MessageUtil.miniMessageToLegacyString(mm);
        System.out.println("raw: " + raw);
        System.out.println("component.toString: " + (c == null ? "null" : c.toString()));
        System.out.println("componentToLegacy: " + legacy);
        System.out.println("serializeToMiniMessage: " + mm);
        System.out.println("miniMessageToLegacyString(mm): " + alt);
    }
}
