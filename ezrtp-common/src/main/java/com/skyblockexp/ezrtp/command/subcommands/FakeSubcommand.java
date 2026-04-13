package com.skyblockexp.ezrtp.command.subcommands;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.SearchPattern;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.CircularSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.TriangleSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.DiamondSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.SquareSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;
import com.skyblockexp.ezrtp.teleport.heatmap.HeatmapSimulationStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.util.MessageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles the /rtp fake subcommand for heatmap simulation.
 */
public class FakeSubcommand extends Subcommand {

    private static final int MAX_FAKE_POINTS_PER_COMMAND = 5000;

    private final EzRtpPlugin plugin;
    private final Supplier<RandomTeleportService> teleportServiceSupplier;
    private final Supplier<EzRtpConfiguration> configurationSupplier;
    private final HeatmapSimulationStore heatmapSimulationStore;

    public FakeSubcommand(EzRtpPlugin plugin,
                         Supplier<RandomTeleportService> teleportServiceSupplier,
                         Supplier<EzRtpConfiguration> configurationSupplier,
                         HeatmapSimulationStore heatmapSimulationStore) {
        super("fake", "ezrtp.heatmap.fake");
        this.plugin = plugin;
        this.teleportServiceSupplier = teleportServiceSupplier;
        this.configurationSupplier = configurationSupplier;
        this.heatmapSimulationStore = heatmapSimulationStore;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        handleHeatmapSimulationCommand(sender, args);
        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("clear");
            suggestions.add("<amount>");
            return suggestions;
        }

        if (args.length == 2) {
            // Suggest world names
            List<String> suggestions = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                suggestions.add(world.getName());
            }
            return suggestions;
        }

        return Collections.emptyList();
    }

    /**
     * Handles /rtp fake operations for injecting or clearing simulated heatmap points.
     */
    private void handleHeatmapSimulationCommand(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.COMMAND_NO_PERMISSION));
            return;
        }

        if (heatmapSimulationStore == null) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_STORE_MISSING));
            return;
        }

        if (args.length < 1) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_USAGE));
            return;
        }

        String worldArgument = args.length >= 2 ? args[1] : null;
        boolean worldProvided = worldArgument != null && !worldArgument.isBlank();
        World targetWorld = resolveSimulationWorld(sender, worldArgument);
        if (targetWorld == null) {
            return;
        }

        String action = args[0];
        if ("clear".equalsIgnoreCase(action)) {
            int cleared = heatmapSimulationStore.clearWorld(targetWorld.getName());
                MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.HEATMAP_SIMULATION_CLEARED, Map.of(
                "count", String.valueOf(cleared),
                "s", cleared == 1 ? "" : "s",
                "world", targetWorld.getName()
            )));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_INVALID_AMOUNT, Map.of("amount", args[0])));
            return;
        }

        if (amount <= 0) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_AMOUNT_NEGATIVE));
            return;
        }

        int commandLimit = Math.min(MAX_FAKE_POINTS_PER_COMMAND, heatmapSimulationStore.getPerWorldCapacity());
        if (amount > commandLimit) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_AMOUNT_TOO_LARGE, Map.of("limit", String.valueOf(commandLimit))));
            return;
        }

        EzRtpConfiguration configuration = configurationSupplier.get();
        if (configuration == null) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_CONFIG_MISSING));
            return;
        }

        RandomTeleportSettings settings = configuration.getSettingsForWorld(targetWorld.getName());
        if (settings == null) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_SETTINGS_MISSING, Map.of("world", targetWorld.getName())));
            return;
        }

        BiomeSearchStrategy strategy = resolveSearchStrategy(settings.getSearchPattern());
        int minRadius = Math.max(0, settings.getMinimumRadius());
        int maxRadius = Math.max(minRadius, resolveMaximumRadius(targetWorld, settings));

        int centerX = settings.getCenterX();
        int centerZ = settings.getCenterZ();
        if (settings.useWorldBorderRadius()) {
            java.util.Optional<Double> radius = com.skyblockexp.ezrtp.util.compat.WorldBorderCompat.getBorderRadius(targetWorld);
            if (radius.isPresent()) {
                int resolved = (int) Math.floor(radius.get());
                maxRadius = Math.max(minRadius, resolved);
            }
        }

        double sampleY = resolveSimulationY(targetWorld, settings);
        // If generating many samples, perform coordinate computation off the main thread
        final int ASYNC_THRESHOLD = 200;
        if (amount >= ASYNC_THRESHOLD) {
            MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_HINT));
            // Capture locals into finals for safe use inside lambdas
            final BiomeSearchStrategy strategyFinal = strategy;
            final World targetWorldFinal = targetWorld;
            final int centerXFinal = centerX;
            final int centerZFinal = centerZ;
            final int minRadiusFinal = minRadius;
            final int maxRadiusFinal = maxRadius;
            final RandomTeleportSettings settingsFinal = settings;
            final double sampleYFinal = sampleY;
            final CommandSender senderFinal = sender;

            // Compute integer coordinates asynchronously to avoid blocking server thread
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                final java.util.List<int[]> coords = new java.util.ArrayList<>(amount);
                for (int i = 0; i < amount; i++) {
                    int[] c = strategyFinal.generateCandidateCoordinates(targetWorldFinal, centerXFinal, centerZFinal, minRadiusFinal, maxRadiusFinal, settingsFinal.getBiomeInclude(), null);
                    if (c != null && c.length >= 2) coords.add(new int[]{c[0], c[1]});
                }

                // Switch back to main thread to create Location objects and add to store
                PlatformRuntimeRegistry.get().scheduler().executeGlobal(() -> {
                    java.util.List<Location> generatedLocations = new java.util.ArrayList<>(coords.size());
                    for (int[] c : coords) {
                        generatedLocations.add(new Location(targetWorldFinal, c[0], sampleYFinal, c[1]));
                    }
                    int inserted = heatmapSimulationStore.addSamples(targetWorldFinal.getName(), generatedLocations);
                    int totalSimulated = heatmapSimulationStore.getSamples(targetWorldFinal.getName()).size();

                    MessageUtil.send(senderFinal, plugin.getMessageProvider().format(MessageKey.HEATMAP_SIMULATION_ADDED, Map.of(
                        "count", String.valueOf(inserted),
                        "s", inserted == 1 ? "" : "s",
                        "world", targetWorldFinal.getName(),
                        "pattern", settingsFinal.getSearchPattern().getConfigKey()
                    )));
                    MessageUtil.send(senderFinal, plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_STATUS, Map.of(
                        "count", String.valueOf(totalSimulated),
                        "s", totalSimulated == 1 ? "" : "s",
                        "capacity", String.valueOf(heatmapSimulationStore.getPerWorldCapacity())
                    )));
                });
            });
            return;
        }

        List<Location> generated = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            int[] coordinates = strategy.generateCandidateCoordinates(targetWorld, centerX, centerZ, minRadius, maxRadius, settings.getBiomeInclude(), null);
            if (coordinates != null && coordinates.length >= 2) {
                Location loc = new Location(targetWorld, coordinates[0], sampleY, coordinates[1]);
                generated.add(loc);
            }
        }

        int inserted = heatmapSimulationStore.addSamples(targetWorld.getName(), generated);
        int totalSimulated = heatmapSimulationStore.getSamples(targetWorld.getName()).size();

        MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.HEATMAP_SIMULATION_ADDED, Map.of(
            "count", String.valueOf(inserted),
            "s", inserted == 1 ? "" : "s",
            "world", targetWorld.getName(),
            "pattern", settings.getSearchPattern().getConfigKey()
        )));
        MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_STATUS, Map.of(
            "count", String.valueOf(totalSimulated),
            "s", totalSimulated == 1 ? "" : "s",
            "capacity", String.valueOf(heatmapSimulationStore.getPerWorldCapacity())
        )));
        MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_SIMULATION_HINT));
    }

    private World resolveSimulationWorld(CommandSender sender, String explicitWorld) {
        if (explicitWorld != null && !explicitWorld.isBlank()) {
            World world = Bukkit.getWorld(explicitWorld);
            if (world == null) {
                MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_WORLD_MISSING, Map.of("world", explicitWorld)));
                return null;
            }
            return world;
        }
        if (sender instanceof Player player) {
            return player.getWorld();
        }
        MessageUtil.send(sender, plugin.getMessageProvider().format(MessageKey.FAKE_WORLD_REQUIRED_CONSOLE));
        return null;
    }

    private BiomeSearchStrategy resolveSearchStrategy(SearchPattern pattern) {
        if (pattern == null) {
            return new UniformSearchStrategy();
        }
        return switch (pattern) {
            case CIRCLE -> new CircularSearchStrategy();
            case DIAMOND -> new DiamondSearchStrategy();
            case TRIANGLE -> new TriangleSearchStrategy();
            case SQUARE -> new SquareSearchStrategy();
            case RANDOM -> new UniformSearchStrategy();
        };
    }

    private int resolveMaximumRadius(World world, RandomTeleportSettings settings) {
        if (!settings.useWorldBorderRadius()) {
            return settings.getMaximumRadius();
        }
        java.util.Optional<Double> radius = com.skyblockexp.ezrtp.util.compat.WorldBorderCompat.getBorderRadius(world);
        if (radius.isPresent()) {
            int resolved = (int) Math.floor(radius.get());
            return Math.max(settings.getMinimumRadius(), resolved);
        }
        return settings.getMaximumRadius();
    }

    private double resolveSimulationY(World world, RandomTeleportSettings settings) {
        if (settings.getMaxY() != null) {
            return (settings.getMinY() + settings.getMaxY()) / 2.0D;
        }
        if (settings.getMinY() != null) {
            return settings.getMinY() + 10.0D;
        }
        Location spawn = world.getSpawnLocation();
        return spawn != null ? spawn.getY() : 64.0D;
    }
}