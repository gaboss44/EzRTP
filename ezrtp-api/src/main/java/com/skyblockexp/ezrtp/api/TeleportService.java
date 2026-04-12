package com.skyblockexp.ezrtp.api;

import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Public API interface for EzRTP teleport service. Implementations should register
 * themselves via Bukkit's ServicesManager so third-party plugins can obtain a
 * typed reference to the service without reflection.
 */
public interface TeleportService {

    void teleportPlayer(Player player, TeleportReason reason);

    void teleportPlayer(Player player, Object settings, TeleportReason reason);

    void teleportPlayer(Player player, TeleportReason reason, Consumer<Boolean> callback);

    void teleportPlayer(Player player, Object settings, TeleportReason reason, Consumer<Boolean> callback);

}
