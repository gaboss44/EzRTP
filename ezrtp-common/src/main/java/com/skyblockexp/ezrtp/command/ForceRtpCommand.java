package com.skyblockexp.ezrtp.command;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.ForceRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.skyblockexp.ezrtp.util.MessageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles execution and tab completion for the {@code /forcertp} command.
 */
public final class ForceRtpCommand implements CommandExecutor, TabCompleter {

    private final EzRtpPlugin plugin;
    private final Supplier<RandomTeleportService> teleportServiceSupplier;
    private final Supplier<EzRtpConfiguration> configurationSupplier;
    private final Supplier<ForceRtpConfiguration> forceRtpConfigurationSupplier;

    public ForceRtpCommand(EzRtpPlugin plugin,
                          Supplier<RandomTeleportService> teleportServiceSupplier,
                          Supplier<EzRtpConfiguration> configurationSupplier,
                          Supplier<ForceRtpConfiguration> forceRtpConfigurationSupplier) {
        this.plugin = plugin;
        this.teleportServiceSupplier = teleportServiceSupplier;
        this.configurationSupplier = configurationSupplier;
        this.forceRtpConfigurationSupplier = forceRtpConfigurationSupplier;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Console always has permission for administrative commands
        if (sender instanceof org.bukkit.entity.Player && !sender.hasPermission("ezrtp.forcertp")) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.COMMAND_NO_PERMISSION));
            return true;
        }

        if (args.length < 1) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_INVALID_USAGE));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_PLAYER_NOT_FOUND, Map.of("player", targetName)));
            return true;
        }

        // Parse optional world argument
        String worldName = args.length >= 2 ? args[1] : null;
        if (worldName == null) {
            // Use default world from force-rtp configuration
            ForceRtpConfiguration forceConfig = forceRtpConfigurationSupplier.get();
            worldName = forceConfig.getDefaultWorld();
        }

        // Resolve "auto" to the target player's current world
        if ("auto".equalsIgnoreCase(worldName)) {
            worldName = target.getWorld().getName();
        }

        EzRtpConfiguration configuration = configurationSupplier.get();
        RandomTeleportSettings settings = configuration != null ? configuration.getSettingsForWorld(worldName) : null;
        if (settings == null) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_WORLD_MISSING, Map.of("world", worldName != null ? worldName : "default")));
            return false;
        }
        // If getSettingsForWorld returned fallback defaults for a different world, override the world name
        if (!worldName.equals(settings.getWorldName())) {
            settings = settings.withWorldName(worldName);
        }

        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_SERVICE_UNAVAILABLE));
            return false;
        }

        // Notify executor and target
        com.skyblockexp.ezrtp.util.MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_EXECUTOR_NOTIFICATION, Map.of("player", target.getName())));
        // Provide the target message with the 'world' placeholder so messages
        // like "You are being teleported to <world>..." are resolved.
        com.skyblockexp.ezrtp.util.MessageUtil.send(target, plugin.getMessageProvider().format(
            MessageKey.FORCERTP_TARGET_NOTIFICATION,
            Map.of("world", worldName != null ? worldName : target.getWorld().getName()),
            target));

        // Teleport instantly, bypassing configured restrictions
        service.teleportPlayer(target, settings, TeleportReason.COMMAND);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ezrtp.forcertp")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            // Suggest online player names
            List<String> suggestions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
            return suggestions;
        }

        if (args.length == 2) {
            // Suggest world names and the special "auto" sentinel
            List<String> suggestions = new ArrayList<>();
            suggestions.add("auto");
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                suggestions.add(world.getName());
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}