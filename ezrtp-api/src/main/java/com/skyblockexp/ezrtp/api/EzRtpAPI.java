package com.skyblockexp.ezrtp.api;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Lightweight public API facade for EzRTP. This class is intentionally small so
 * third-party plugins can depend only on the `ezrtp-api` artifact.
 */
public final class EzRtpAPI {

    private EzRtpAPI() {}

    /**
     * Locate the running EzRTP service instance using reflection. Returns null when not available.
     */
    public static Object getTeleportService() {
        var pm = Bukkit.getPluginManager();
        if (pm == null) return null;
        var plugin = pm.getPlugin("EzRTP");
        if (plugin == null) return null;
        try {
            var m = plugin.getClass().getMethod("getTeleportService");
            return m.invoke(plugin);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean isAvailable() {
        return getTeleportService() != null;
    }

    /**
     * Convenience: request an RTP using default behaviour. Uses reflection so callers
     * need only depend on the small `ezrtp-api` artifact.
     */
    public static void rtpPlayer(Player player) {
        Object service = getTeleportService();
        if (service == null) return;
        try {
            TeleportReason reason = TeleportReason.COMMAND;
            var m = service.getClass().getMethod("teleportPlayer", Player.class, TeleportReason.class);
            m.invoke(service, player, reason);
        } catch (Throwable ignored) {
        }
    }

    public static void rtpPlayer(Player player, Object settings) {
        Object service = getTeleportService();
        if (service == null) return;
        try {
            TeleportReason reason = TeleportReason.COMMAND;
            var m = service.getClass().getMethod("teleportPlayer", Player.class, settings.getClass(), TeleportReason.class);
            m.invoke(service, player, settings, reason);
        } catch (NoSuchMethodException ex) {
            // Try fallback signature: (Player, Object, TeleportReason)
            try {
                TeleportReason reason = TeleportReason.COMMAND;
                var m = service.getClass().getMethod("teleportPlayer", Player.class, Object.class, TeleportReason.class);
                m.invoke(service, player, settings, reason);
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {
        }
    }

    public static void rtpPlayer(Player player, Object settings, java.util.function.Consumer<Boolean> callback) {
        Object service = getTeleportService();
        if (service == null) {
            if (callback != null) callback.accept(false);
            return;
        }
        try {
            TeleportReason reason = TeleportReason.COMMAND;
            var m = service.getClass().getMethod("teleportPlayer", Player.class, settings.getClass(), TeleportReason.class, java.util.function.Consumer.class);
            m.invoke(service, player, settings, reason, callback);
            return;
        } catch (NoSuchMethodException ex) {
            // Try other likely signature: (Player, TeleportReason, Consumer<Boolean>)
            try {
                TeleportReason reason = TeleportReason.COMMAND;
                var m = service.getClass().getMethod("teleportPlayer", Player.class, TeleportReason.class, java.util.function.Consumer.class);
                m.invoke(service, player, reason, callback);
                return;
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {
        }
        if (callback != null) callback.accept(false);
    }

    /**
     * Advanced usage: caller can retrieve the service via `getTeleportService()` and
     * call implementation-specific APIs (or use reflection) to provide settings/callbacks.
     */
    public static void rtpPlayerWithSettingsFallback(Player player, java.util.function.Consumer<Boolean> fallback) {
        Object service = getTeleportService();
        if (service == null) {
            if (fallback != null) fallback.accept(false);
            return;
        }
        // Prefer invoking simple teleport; callers should obtain the service for advanced calls.
        rtpPlayer(player);
        if (fallback != null) fallback.accept(true);
    }
}
