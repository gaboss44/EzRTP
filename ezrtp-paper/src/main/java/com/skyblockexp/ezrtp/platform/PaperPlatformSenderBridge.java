package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class PaperPlatformSenderBridge implements PlatformSenderBridge {

    @Override
    public boolean sendToSender(CommandSender sender, Component component) {
        if (sender == null || component == null) {
            return false;
        }
        sender.sendMessage(component);
        return true;
    }
}
