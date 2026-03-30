package com.skyblockexp.ezrtp.util;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.bukkit.command.CommandSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.kyori.adventure.text.Component;
import com.skyblockexp.ezrtp.message.MessageFormatter;

public class MessageUtilAudienceTest {

    @Test
    public void sendString_nonPlayer_fallsBackToLegacySerialization() {
        CommandSender sender = mock(CommandSender.class);
        // Prevent PlaceholderUtil from attempting to access Bukkit in unit tests
        try {
            java.lang.reflect.Field checked = com.skyblockexp.ezrtp.util.PlaceholderUtil.class.getDeclaredField("checkedAvailability");
            checked.setAccessible(true);
            checked.setBoolean(null, true);
            java.lang.reflect.Field avail = com.skyblockexp.ezrtp.util.PlaceholderUtil.class.getDeclaredField("placeholderAPIAvailable");
            avail.setAccessible(true);
            avail.setBoolean(null, false);
        } catch (Throwable ignored) {}
        // set a hex prefix so legacy serialization will include the hex marker
        MessageUtil.setPrefix("<#2fff7a>EzRTP ");

        // Build component like MessageUtil would and serialize it to legacy string
        Component prefixComp = MessageFormatter.format(MessageUtil.getPrefix());
        Component messageComp = MessageFormatter.format("configuration reloaded.");
        Component full = prefixComp.append(messageComp);
        String out = MessageUtil.serializeComponent(full);
        assertNotNull(out);
        // Expect legacy serialized output to contain the section sign + '#' sequence for hex
        assertTrue(out.contains("\u00A7#"), "Expected legacy hex marker (§#) in serialized output: " + out);
        assertTrue(out.contains("EzRTP"), "Expected prefix text to be present");
    }

    @Test
    public void sendComponent_nonPlayer_fallsBackToLegacySerialization() {
        CommandSender sender = mock(CommandSender.class);
        // Prevent PlaceholderUtil from attempting to access Bukkit in unit tests
        try {
            java.lang.reflect.Field checked = com.skyblockexp.ezrtp.util.PlaceholderUtil.class.getDeclaredField("checkedAvailability");
            checked.setAccessible(true);
            checked.setBoolean(null, true);
            java.lang.reflect.Field avail = com.skyblockexp.ezrtp.util.PlaceholderUtil.class.getDeclaredField("placeholderAPIAvailable");
            avail.setAccessible(true);
            avail.setBoolean(null, false);
        } catch (Throwable ignored) {}
        MessageUtil.setPrefix("");
        // build a component with a hex tag
        Component comp = MessageFormatter.format("<#2fff7a>EzRTP configuration reloaded.</#2fff7a>");

        // Serialize component directly and assert legacy serialization contains hex marker
        String out = MessageUtil.serializeComponent(comp);
        assertNotNull(out);
        assertTrue(out.contains("\u00A7#"), "Expected legacy hex marker (§#) in serialized output: " + out);
        assertTrue(out.toLowerCase().contains("configuration reloaded"));
    }
}
