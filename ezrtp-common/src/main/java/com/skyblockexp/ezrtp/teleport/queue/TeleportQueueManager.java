package com.skyblockexp.ezrtp.teleport.queue;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.TeleportQueueSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.teleport.TeleportReason;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import com.skyblockexp.ezrtp.util.MessageUtil;

/**
 * Manages the teleport queue system and delegates execution to a handler.
 */
public final class TeleportQueueManager {

    @FunctionalInterface
    public interface QueueExecutionHandler {
        void execute(Player player, RandomTeleportSettings settings, TeleportReason reason, Runnable completionHook);
    }

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final MessageProvider messageProvider;
    private final Deque<QueuedTeleport> teleportQueue = new ArrayDeque<>();

    private TeleportQueueSettings queueSettings;
    private boolean queueProcessing;
    private UUID activeTeleportPlayer;
    private QueueExecutionHandler executionHandler;

    public TeleportQueueManager(org.bukkit.plugin.java.JavaPlugin plugin,
                               TeleportQueueSettings queueSettings,
                               MessageProvider messageProvider) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        this.queueSettings = queueSettings != null ? queueSettings : TeleportQueueSettings.disabled();
        this.messageProvider = messageProvider;
    }

    public void setExecutionHandler(QueueExecutionHandler executionHandler) {
        this.executionHandler = executionHandler;
    }

    /**
     * Enqueues a teleport request if queuing is enabled and player cannot bypass.
     * Returns true if enqueued, false if should proceed immediately.
     */
    public boolean enqueueIfNeeded(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
        if (queueSettings == null || !queueSettings.isEnabled() || queueSettings.canBypass(player)) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        if (activeTeleportPlayer != null && activeTeleportPlayer.equals(playerId)) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORTING, player));
            return true;
        }

        int existingPosition = findQueuePosition(playerId);
        if (existingPosition >= 0) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.QUEUE_QUEUED,
                Map.of("position", String.valueOf(existingPosition + 1)), player));
            return true;
        }

        if (queueSettings.getMaxSize() > 0 && teleportQueue.size() >= queueSettings.getMaxSize()) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.QUEUE_FULL, player));
            return true;
        }

        teleportQueue.addLast(new QueuedTeleport(playerId, teleportSettings, reason));
        com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.QUEUE_QUEUED,
            Map.of("position", String.valueOf(teleportQueue.size())), player));

        if (!queueProcessing) {
            queueProcessing = true;
            scheduleDispatch(Math.max(0L, queueSettings.getStartDelayTicks()));
        }

        return true;
    }

    private void scheduleDispatch(long delayTicks) {
        try {
            scheduler.runTaskLater(plugin, this::dispatchNextTeleport, delayTicks);
        } catch (IllegalStateException ex) {
            queueProcessing = false;
            teleportQueue.clear();
        }
    }

    private void dispatchNextTeleport() {
        if (executionHandler == null) {
            queueProcessing = false;
            return;
        }

        QueuedTeleport next;
        while ((next = teleportQueue.pollFirst()) != null) {
            Player player = plugin.getServer().getPlayer(next.playerId());
            if (player == null || !player.isOnline()) {
                continue;
            }
            activeTeleportPlayer = next.playerId();
            executionHandler.execute(player, next.settings(), next.reason(), this::completeQueuedTeleport);
            return;
        }

        queueProcessing = false;
        activeTeleportPlayer = null;
    }

    private void completeQueuedTeleport() {
        activeTeleportPlayer = null;
        if (teleportQueue.isEmpty()) {
            queueProcessing = false;
            return;
        }
        long delay = queueSettings != null ? Math.max(0L, queueSettings.getIntervalTicks()) : 0L;
        scheduleDispatch(delay);
    }

    private int findQueuePosition(UUID playerId) {
        int index = 0;
        for (QueuedTeleport queued : teleportQueue) {
            if (queued.playerId().equals(playerId)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void reload(TeleportQueueSettings newQueueSettings) {
        this.queueSettings = newQueueSettings != null ? newQueueSettings : TeleportQueueSettings.disabled();
        if (!this.queueSettings.isEnabled() && teleportQueue.isEmpty()) {
            queueProcessing = false;
            activeTeleportPlayer = null;
        }
    }

    public void shutdown() {
        teleportQueue.clear();
        queueProcessing = false;
        activeTeleportPlayer = null;
    }

    public record QueuedTeleport(UUID playerId, RandomTeleportSettings settings, TeleportReason reason) {}
}