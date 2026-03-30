package com.skyblockexp.ezrtp.paper;

import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/**
 * Paper-specific messaging helpers. Replace reflection-based audience code
 * with direct Paper APIs here (future work).
 */
public final class PluginMessageHelperPaper {

    private PluginMessageHelperPaper() {}

    public static void sendToSender(CommandSender sender, Component component) {
        if (sender == null || component == null) return;
        try {
            var svc = com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry.get();
            if (svc != null && svc.sendToSender(sender, component)) return;
        } catch (Throwable ignored) {}
        MessageUtil.send(sender, component);
    }

}
