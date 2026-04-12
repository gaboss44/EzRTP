package com.skyblockexp.ezrtp.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import com.skyblockexp.ezrtp.teleport.TeleportReason;

import java.util.function.Consumer;

/**
 * Lightweight public API facade for EzRTP. This class is intentionally small so
 * third-party plugins can depend only on the `ezrtp-api` artifact.
 */
public final class EzRtpAPI {

    private EzRtpAPI() {}




    /**
     * Locate the running EzRTP service instance.
     * First attempts Bukkit's ServicesManager lookup, then falls back to plugin reflection.
     */
    public static TeleportService getTeleportService() {
        // First try Bukkit ServicesManager registration (recommended and easiest to mock in tests)
        try {
            var sm = Bukkit.getServicesManager();
            if (sm != null) {
                RegisteredServiceProvider<TeleportService> reg = sm.getRegistration(TeleportService.class);
                if (reg != null) return reg.getProvider();
            }
        } catch (Throwable ignored) {}

        // Fallback: direct plugin lookup via reflection
        try {
            var pm = Bukkit.getPluginManager();
            if (pm != null) {
                var plugin = pm.getPlugin("EzRTP");
                if (plugin != null) {
                    try {
                        var m = plugin.getClass().getMethod("getTeleportService");
                        Object svc = m.invoke(plugin);
                        if (svc != null) {
                            if (svc instanceof TeleportService) return (TeleportService) svc;
                            return adaptToTeleportService(svc);
                        }
                    } catch (Throwable ignored) {
                        // give up and return null
                    }
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }

    private static TeleportService adaptToTeleportService(Object svc) {
        return new TeleportService() {
            @Override
            public void teleportPlayer(org.bukkit.entity.Player player, TeleportReason reason) {
                try { var m = svc.getClass().getMethod("teleportPlayer", org.bukkit.entity.Player.class, TeleportReason.class); m.invoke(svc, player, reason); } catch (Throwable ignored) {}
            }

            @Override
            public void teleportPlayer(org.bukkit.entity.Player player, Object settings, TeleportReason reason) {
                try {
                    var m = svc.getClass().getMethod("teleportPlayer", org.bukkit.entity.Player.class, settings.getClass(), TeleportReason.class);
                    m.invoke(svc, player, settings, reason);
                    return;
                } catch (NoSuchMethodException ex) {
                    try { var m = svc.getClass().getMethod("teleportPlayer", org.bukkit.entity.Player.class, Object.class, TeleportReason.class); m.invoke(svc, player, settings, reason); } catch (Throwable ignored) {}
                } catch (Throwable ignored) {}
            }

            @Override
            public void teleportPlayer(org.bukkit.entity.Player player, TeleportReason reason, java.util.function.Consumer<Boolean> callback) {
                try { var m = svc.getClass().getMethod("teleportPlayer", org.bukkit.entity.Player.class, TeleportReason.class, java.util.function.Consumer.class); m.invoke(svc, player, reason, callback); } catch (Throwable ignored) {}
            }

            @Override
            public void teleportPlayer(org.bukkit.entity.Player player, Object settings, TeleportReason reason, java.util.function.Consumer<Boolean> callback) {
                try {
                    var m = svc.getClass().getMethod("teleportPlayer", org.bukkit.entity.Player.class, settings.getClass(), TeleportReason.class, java.util.function.Consumer.class);
                    m.invoke(svc, player, settings, reason, callback);
                    return;
                } catch (NoSuchMethodException ex) {
                    try { var m = svc.getClass().getMethod("teleportPlayer", org.bukkit.entity.Player.class, TeleportReason.class, java.util.function.Consumer.class); m.invoke(svc, player, reason, callback); } catch (Throwable ignored) {}
                } catch (Throwable ignored) {}
            }
        };
    }

    /**
     * Register a TeleportService provider with Bukkit's ServicesManager.
     */
    public static void registerProvider(org.bukkit.plugin.Plugin plugin, TeleportService provider) {
        if (plugin == null || provider == null) return;
        try {
            Bukkit.getServicesManager().register(TeleportService.class, provider, plugin, ServicePriority.Normal);
        } catch (Throwable ignored) {}
    }

    /**
     * Unregister a TeleportService provider from Bukkit's ServicesManager.
     */
    public static void unregisterProvider(TeleportService provider) {
        if (provider == null) return;
        try {
            Bukkit.getServicesManager().unregister(TeleportService.class, provider);
        } catch (Throwable ignored) {}
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
