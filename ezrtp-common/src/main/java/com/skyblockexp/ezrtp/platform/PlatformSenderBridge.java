package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/**
 * Platform bridge for delivering Adventure components to command senders.
 */
public interface PlatformSenderBridge {

    /**
     * Sends the component to the target sender.
     *
     * @return true if the bridge accepted the send attempt.
     */
    boolean sendToSender(CommandSender sender, Component component);

    default void close() {}
}
