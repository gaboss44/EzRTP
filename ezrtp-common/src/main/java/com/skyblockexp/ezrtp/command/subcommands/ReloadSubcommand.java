package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.message.MessageKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import com.skyblockexp.ezrtp.util.MessageUtil;

import java.util.Collections;
import java.util.List;

/**
 * Handles the /rtp reload subcommand.
 */
public class ReloadSubcommand extends Subcommand {

    private final EzRtpPlugin plugin;

    public ReloadSubcommand(EzRtpPlugin plugin) {
        super("reload", "ezrtp.reload");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        plugin.reloadPluginConfiguration();
        MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.COMMAND_RELOAD_SUCCESS));
        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}