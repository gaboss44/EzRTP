package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.message.MessageKey;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import com.skyblockexp.ezrtp.util.MessageUtil;
import org.popcraft.chunky.api.ChunkyAPI;
import com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /rtp pregenerate subcommand.
 */
public class PregenerateSubcommand extends Subcommand {

    private final EzRtpPlugin plugin;
    private final ChunkyAPI chunkyAPI;
    private final ChunkyWarmupCoordinator chunkyWarmupCoordinator;

    public PregenerateSubcommand(EzRtpPlugin plugin, ChunkyAPI chunkyAPI, ChunkyWarmupCoordinator chunkyWarmupCoordinator) {
        super("pregenerate", "ezrtp.pregenerate");
        this.plugin = plugin;
        this.chunkyAPI = chunkyAPI;
        this.chunkyWarmupCoordinator = chunkyWarmupCoordinator;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (chunkyAPI == null) {
            MessageUtil.send(sender, "<red>Chunky plugin is not available.</red>");
            return true;
        }

        if (args.length > 2) {
            MessageUtil.send(sender, "<red>Usage: /rtp pregenerate [&lt;world&gt;] [&lt;radius&gt;]</red>");
            return true;
        }

        String defaultWorld = plugin.getConfig().getString("world", "world");
        int defaultRadius = plugin.getConfig().getInt("radius.min", 1000);

        String worldName = args.length > 0 ? args[0] : defaultWorld;
        int radius = defaultRadius;
        if (args.length > 1) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                MessageUtil.send(sender, "<red>Invalid radius: " + args[1] + "</red>");
                return true;
            }
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            MessageUtil.send(sender, "<red>World '" + worldName + "' not found.</red>");
            return true;
        }

        if (chunkyAPI.isRunning(worldName)) {
            MessageUtil.send(sender, "<red>Pregeneration is already running for world '" + worldName + "'.</red>");
            return true;
        }

        // Use default shape and pattern from config, or defaults
        String shape = "circle";
        String pattern = "concentric";

        // Instead of one large pregeneration, start multiple small ones at random locations within the radius
        // to pregenerate chunks at potential RTP locations
        int numTasks = Math.min(10, Math.max(1, radius / 100)); // Scale number of tasks with radius
        int smallRadius = 50; // Small radius for each task
        boolean anyStarted = false;

        for (int i = 0; i < numTasks; i++) {
            // Generate random angle and distance within the search radius
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * radius;
            double centerX = Math.cos(angle) * distance;
            double centerZ = Math.sin(angle) * distance;

            boolean started = chunkyAPI.startTask(worldName, shape, centerX, centerZ, smallRadius, smallRadius, pattern);
            if (started) {
                anyStarted = true;
                if (chunkyWarmupCoordinator != null) {
                    int centerChunkX = (int) Math.floor(centerX / 16.0);
                    int centerChunkZ = (int) Math.floor(centerZ / 16.0);
                    int radiusChunks = Math.max(0, (smallRadius + 15) / 16);
                    chunkyWarmupCoordinator.markRegionPlanned(worldName, centerChunkX, centerChunkZ, radiusChunks);
                }
            }
        }

        if (anyStarted) {
            MessageUtil.send(sender, "<green>Started " + numTasks + " pregeneration tasks for world '" + worldName + "' within radius " + radius + ".</green>");
        } else {
            MessageUtil.send(sender, "<red>Failed to start pregeneration tasks for world '" + worldName + "'.</red>");
        }

        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Suggest common radius values
            List<String> suggestions = Arrays.asList("500", "1000", "2000", "5000", "10000");
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}