
package com.skyblockexp.ezrtp.listener;
import com.skyblockexp.ezrtp.config.OnJoinTeleportSettings;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Handles automatically teleporting players when they join the server if configured.
 */
public final class PlayerJoinTeleportListener implements Listener {

    private final JavaPlugin plugin;
    private final Supplier<RandomTeleportService> teleportServiceSupplier;
    private final Supplier<RandomTeleportSettings> settingsSupplier;

    public PlayerJoinTeleportListener(JavaPlugin plugin,
                                      Supplier<RandomTeleportService> teleportServiceSupplier,
                                      Supplier<RandomTeleportSettings> settingsSupplier) {
        this.plugin = plugin;
        this.teleportServiceSupplier = teleportServiceSupplier;
        this.settingsSupplier = settingsSupplier;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RandomTeleportSettings settings = settingsSupplier.get();
        if (settings == null) {
            return;
        }
        OnJoinTeleportSettings onJoin = settings.getOnJoinTeleportSettings();
        if (!onJoin.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (onJoin.onlyFirstJoin() && player.hasPlayedBefore()) {
            return;
        }
        if (!onJoin.bypassPermission().isEmpty() && player.hasPermission(onJoin.bypassPermission())) {
            return;
        }

        RandomTeleportService service = Objects.requireNonNull(teleportServiceSupplier.get(),
                "Teleport service not initialised");
        long delay = Math.max(0L, onJoin.delayTicks());
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> service.teleportPlayer(player, TeleportReason.JOIN), delay);
    }

    public void updateSettings(RandomTeleportSettings newSettings) {
        // No caching required for now, but method retained for future use if needed.
    }
}
