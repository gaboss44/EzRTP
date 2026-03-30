package com.skyblockexp.ezrtp.util;

import net.kyori.adventure.text.Component;

/**
 * Bukkit-specific low-level messaging helper. Keep high-level, plugin-specific
 * message formatting in the plugin module; this helper focuses on delivery.
 */
public final class PluginMessageHelperBukkit {

    private PluginMessageHelperBukkit() {}

    public static void sendToSender(Object sender, Component component) {
        MessageUtil.send(sender, component);
    }

}

