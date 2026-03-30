package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Handles the `/rtp setcenter` subcommand which allows admins to set the configured
 * RTP center coordinates (persisted to the main config and reloaded).
 *
 * Usage:
 * - /rtp setcenter <x> <z>           (player uses current world)
 * - /rtp setcenter <world> <x> <z>  (explicit world name)
 */
public class SetCenterSubcommand extends Subcommand {

    private final EzRtpPlugin plugin;
    private final Supplier<EzRtpConfiguration> configurationSupplier;

    public SetCenterSubcommand(EzRtpPlugin plugin, Supplier<EzRtpConfiguration> configurationSupplier) {
        super("setcenter", "ezrtp.setcenter");
        this.plugin = plugin;
        this.configurationSupplier = configurationSupplier;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        String targetWorld;
        int xIndex;
        if (args.length == 2 && sender instanceof Player) {
            // /rtp setcenter <x> <z> (player's world)
            Player player = (Player) sender;
            targetWorld = player.getWorld().getName();
            xIndex = 0;
        } else if (args.length == 3) {
            // /rtp setcenter <world> <x> <z>
            targetWorld = args[0];
            xIndex = 1;
        } else {
            sender.sendMessage("§eUsage: /rtp setcenter <x> <z>  or  /rtp setcenter <world> <x> <z>");
            return true;
        }

        try {
            int x = Integer.parseInt(args[xIndex]);
            int z = Integer.parseInt(args[xIndex + 1]);

            // Persist to main config: if target world matches default settings' world name, set root 'center'
            EzRtpConfiguration config = configurationSupplier.get();
            String defaultWorld = config != null && config.getDefaultSettings() != null
                    ? config.getDefaultSettings().getWorldName() : null;

            FileConfiguration fc = plugin.getConfig();
            if (defaultWorld != null && defaultWorld.equalsIgnoreCase(targetWorld)) {
                fc.set("center.x", x);
                fc.set("center.z", z);
            } else {
                // Store under a dedicated per-world path so admins can later migrate if desired
                fc.set("world-centers." + targetWorld + ".x", x);
                fc.set("world-centers." + targetWorld + ".z", z);
            }

            plugin.saveConfig();
            // Reload plugin configuration so in-memory settings are refreshed
            plugin.reloadPluginConfiguration();

            sender.sendMessage(String.format("§aSet RTP center for world '%s' to X=%d Z=%d and reloaded configuration.", targetWorld, x, z));
        } catch (NumberFormatException ex) {
            sender.sendMessage("§cX and Z must be integers.");
        } catch (Exception ex) {
            sender.sendMessage("§cAn error occurred while saving the configuration: " + ex.getMessage());
        }
        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        // Provide world name suggestions for the first argument
        if (args.length == 1) {
            List<String> names = new java.util.ArrayList<>();
            plugin.getServer().getWorlds().forEach(w -> names.add(w.getName()));
            return names;
        }
        return Collections.emptyList();
    }
}
