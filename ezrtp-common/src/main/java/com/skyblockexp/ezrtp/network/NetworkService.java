package com.skyblockexp.ezrtp.network;

import com.skyblockexp.ezrtp.config.NetworkConfiguration;
import com.skyblockexp.ezrtp.config.NetworkConfiguration.NetworkServer;
import com.skyblockexp.ezrtp.config.NetworkConfiguration.ServerStatusSnapshot;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Coordinates proxy network functionality for EzRTP including server status polling and transfers.
 */
public final class NetworkService {

    private final JavaPlugin plugin;
    private final NetworkConfiguration configuration;
    private final Logger logger;
    private final Map<String, ServerStatusSnapshot> statusByServer = new ConcurrentHashMap<>();
    private BukkitTask pingTask;

    public NetworkService(JavaPlugin plugin, NetworkConfiguration configuration, Logger logger) {
        this.plugin = plugin;
        this.configuration = configuration;
        this.logger = logger;
        initialise();
    }

    private void initialise() {
        if (configuration == null || configuration.getServers().isEmpty()) {
            return;
        }
        for (NetworkServer server : configuration.getServers()) {
            statusByServer.put(server.getId(), ServerStatusSnapshot.unknown());
        }
        try {
            pingTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                    this::refreshStatuses, 1L, configuration.getPingIntervalTicks());
        } catch (IllegalStateException ex) {
            logger.warning("Unable to start network status polling: " + ex.getMessage());
        }
    }

    private void refreshStatuses() {
        for (NetworkServer server : configuration.getServers()) {
            ServerStatusSnapshot status = pingServer(server);
            statusByServer.put(server.getId(), status);
        }
    }

    private ServerStatusSnapshot pingServer(NetworkServer server) {
        long start = System.nanoTime();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server.getHost(), server.getPort()),
                    configuration.getPingTimeoutMillis());
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return ServerStatusSnapshot.online(elapsed);
        } catch (IOException ex) {
            return ServerStatusSnapshot.offline();
        }
    }

    public ServerStatusSnapshot getStatus(NetworkServer server) {
        if (server == null) {
            return ServerStatusSnapshot.unknown();
        }
        return statusByServer.getOrDefault(server.getId(), ServerStatusSnapshot.unknown());
    }

    public ItemStack createIcon(NetworkServer server) {
        ServerStatusSnapshot status = getStatus(server);
        return server.getIconTemplate().createIcon(status, server.getDisplayName());
    }

    public void transferPlayer(Player player, NetworkServer server) {
        if (player == null || server == null) {
            return;
        }
        if (!server.allowWhenOffline() && getStatus(server).isOffline()) {
            return;
        }
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(outputBuffer)) {
            output.writeUTF("Connect");
            output.writeUTF(server.getBungeeServer());
        } catch (IOException ex) {
            logger.warning(String.format("Failed to prepare network transfer to '%s': %s", server.getBungeeServer(),
                    ex.getMessage()));
            return;
        }

        player.sendPluginMessage(plugin, "BungeeCord", outputBuffer.toByteArray());

        Component message = server.connectMessage();
        if (message != null && !Objects.equals(message, Component.empty())) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, message);
        }
    }

    public void shutdown() {
        if (pingTask != null) {
            try {
                pingTask.cancel();
            } catch (Exception ignored) {
                // ignore
            }
            pingTask = null;
        }
        statusByServer.clear();
    }
}
