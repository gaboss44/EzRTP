package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public final class BukkitPlatformSenderBridge implements PlatformSenderBridge {

    @Override
    public boolean sendToSender(CommandSender sender, Component component) {
        if (sender == null || component == null) {
            return false;
        }
        MessageUtil.send(sender, component);
        return true;
    }
}
