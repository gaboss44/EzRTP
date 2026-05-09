package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.skyblockexp.ezrtp.util.MessageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles the /rtp forcertp subcommand.
 */
public class ForcertpSubcommand extends Subcommand {

    private final EzRtpPlugin plugin;
    private final Supplier<RandomTeleportService> teleportServiceSupplier;
    private final Supplier<EzRtpConfiguration> configurationSupplier;

    public ForcertpSubcommand(EzRtpPlugin plugin,
                             Supplier<RandomTeleportService> teleportServiceSupplier,
                             Supplier<EzRtpConfiguration> configurationSupplier) {
        super("forcertp", "ezrtp.forcertp");
        this.plugin = plugin;
        this.teleportServiceSupplier = teleportServiceSupplier;
        this.configurationSupplier = configurationSupplier;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.COMMAND_NO_PERMISSION));
            return true;
        }

        // Strip --skip-message flag from args before normal processing
        boolean skipMessage = false;
        java.util.List<String> filteredArgs = new java.util.ArrayList<>();
        for (String arg : args) {
            if ("--skip-message".equalsIgnoreCase(arg)) {
                skipMessage = true;
            } else {
                filteredArgs.add(arg);
            }
        }
        args = filteredArgs.toArray(new String[0]);

        if (args.length < 1) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_INVALID_USAGE));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null || !target.isOnline()) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_PLAYER_NOT_FOUND, Map.of("player", targetName)));
            return true;
        }

        // Parse optional world argument
        String worldName = args.length >= 2 ? args[1] : null;

        // Resolve "auto" to the target player's current world
        if ("auto".equalsIgnoreCase(worldName)) {
            worldName = target.getWorld().getName();
        }

        EzRtpConfiguration configuration = configurationSupplier.get();
        RandomTeleportSettings settings = configuration != null ? configuration.getSettingsForWorld(worldName) : null;
        if (settings == null) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_WORLD_MISSING, Map.of("world", worldName != null ? worldName : "default")));
            return false;
        }
        // If getSettingsForWorld returned fallback defaults for a different world, override the world name
        if (worldName != null && !worldName.equals(settings.getWorldName())) {
            settings = settings.withWorldName(worldName);
        }
        if (skipMessage) {
            settings = settings.withSuppressPlayerMessages(true);
        }

        RandomTeleportService service = teleportServiceSupplier.get();
        if (service == null) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_SERVICE_UNAVAILABLE));
            return false;
        }

        boolean suppressConsole = skipMessage || (configuration != null && configuration.isSuppressConsoleMessages());
        // Notify executor and target
        if (!suppressConsole) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FORCERTP_EXECUTOR_NOTIFICATION, Map.of("player", target.getName())));
        }
        if (!settings.isSuppressPlayerMessages()) {
            MessageUtil.send(target, plugin.getMessageProvider().format(MessageKey.FORCERTP_TARGET_NOTIFICATION, target));
        }

        // Teleport instantly, bypassing GUI and limits
        service.teleportPlayer(target, settings, TeleportReason.COMMAND);
        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
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
            // Suggest world names, the special "auto" sentinel, and the --skip-message flag
            List<String> suggestions = new ArrayList<>();
            suggestions.add("auto");
            suggestions.add("--skip-message");
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                suggestions.add(world.getName());
            }
            return suggestions;
        }

        if (args.length == 3) {
            List<String> suggestions = new ArrayList<>();
            if (!java.util.Arrays.asList(args).contains("--skip-message")) {
                suggestions.add("--skip-message");
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}