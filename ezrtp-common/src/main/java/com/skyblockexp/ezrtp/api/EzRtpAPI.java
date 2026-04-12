package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Simple static API helpers for other plugins to interact with EzRTP.
 * <p>
 * Example usage:
 * <pre>
 * import com.skyblockexp.ezrtp.api.EzRtpAPI;
 * import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
 *
 * // RTP with default settings
 * EzRtpAPI.rtpPlayer(player);
 *
 * // RTP with custom settings and callback
 * RandomTeleportSettings settings = ...; // create or obtain settings
 * EzRtpAPI.rtpPlayer(player, settings, success -> {
 *     if (success) System.out.println("teleported");
 * });
 * </pre>
 */
public final class EzRtpAPI {

    private static final String PLUGIN_NAME = "EzRTP";

    private EzRtpAPI() {}

    /**
     * Attempts to locate the running EzRTP plugin and return its teleport service.
     *
     * @return the {@link RandomTeleportService} or {@code null} if EzRTP is not present or not enabled.
     */
    public static RandomTeleportService getTeleportService() {
        var plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        if (plugin instanceof EzRtpPlugin ez) {
            return ez.getTeleportService();
        }
        return null;
    }

    /**
     * Returns true if EzRTP is present and its teleport service is available.
     */
    public static boolean isAvailable() {
        return getTeleportService() != null;
    }

    /**
     * Teleports the player using EzRTP with the default configured settings.
     * This is a convenience wrapper that uses {@link TeleportReason#COMMAND}.
     *
     * @param player the player to teleport
     */
    public static void rtpPlayer(Player player) {
        Objects.requireNonNull(player, "player");
        RandomTeleportService service = getTeleportService();
        if (service == null) {
            Bukkit.getLogger().warning("EzRTP not available - rtpPlayer ignored.");
            return;
        }
        service.teleportPlayer(player, TeleportReason.COMMAND);
    }

    /**
     * Teleports the player using EzRTP with the supplied {@link RandomTeleportSettings}.
     * Uses {@link TeleportReason#COMMAND} by default.
     *
     * @param player the player to teleport
     * @param settings settings to use for this teleport (may be null)
     */
    public static void rtpPlayer(Player player, RandomTeleportSettings settings) {
        Objects.requireNonNull(player, "player");
        RandomTeleportService service = getTeleportService();
        if (service == null) {
            Bukkit.getLogger().warning("EzRTP not available - rtpPlayer ignored.");
            return;
        }
        if (settings == null) {
            service.teleportPlayer(player, TeleportReason.COMMAND);
        } else {
            service.teleportPlayer(player, settings, TeleportReason.COMMAND);
        }
    }

    /**
     * Teleports the player using EzRTP with the supplied settings and a callback.
     * Uses {@link TeleportReason#COMMAND} by default.
     *
     * @param player the player to teleport
     * @param settings settings to use for this teleport (may be null)
     * @param callback called with {@code true} if teleport succeeded, otherwise {@code false}
     */
    public static void rtpPlayer(Player player, RandomTeleportSettings settings, Consumer<Boolean> callback) {
        Objects.requireNonNull(player, "player");
        RandomTeleportService service = getTeleportService();
        if (service == null) {
            Bukkit.getLogger().warning("EzRTP not available - rtpPlayer ignored.");
            if (callback != null) callback.accept(false);
            return;
        }
        if (settings == null) {
            service.teleportPlayer(player, TeleportReason.COMMAND, callback);
        } else {
            service.teleportPlayer(player, settings, TeleportReason.COMMAND, callback);
        }
    }
}
