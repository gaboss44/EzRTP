package com.skyblockexp.ezrtp.command.subcommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Abstract base class for RTP subcommands.
 * Each subcommand handles its own execution and tab completion.
 */
public abstract class Subcommand {

    protected final String name;
    protected final String permission;

    protected Subcommand(String name, String permission) {
        this.name = name;
        this.permission = permission;
    }

    /**
     * Executes the subcommand.
     * @param sender the command sender
     * @param args the arguments (excluding the subcommand name)
     * @return true if the command was handled, false otherwise
     */
    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Provides tab completion for the subcommand.
     * @param sender the command sender
     * @param args the arguments (excluding the subcommand name)
     * @return list of tab completion suggestions
     */
    @NotNull
    public abstract List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Gets the name of this subcommand.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the permission required for this subcommand.
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Checks if the sender has the required permission.
     */
    protected boolean hasPermission(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }
}