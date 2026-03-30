package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.ParticleSettings;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.SearchResult.SearchLimitType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;

/**
 * Handles teleport result processing including success messages, particles, and cache operations.
 */
public final class TeleportResultHandler {

    private final org.bukkit.plugin.java.JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final MessageProvider messageProvider;
    private final RtpStatistics statistics;
    private final LocationFinder locationFinder;

    public TeleportResultHandler(org.bukkit.plugin.java.JavaPlugin plugin,
                                 MessageProvider messageProvider,
                                 RtpStatistics statistics,
                                 LocationFinder locationFinder) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        this.messageProvider = messageProvider;
        this.statistics = statistics;
        this.locationFinder = locationFinder;
    }

    /**
     * Handles successful teleport completion.
     */
    public void handleSuccess(Player player, Location destination, SearchResult result,
                             RandomTeleportSettings teleportSettings, long duration,
                             org.bukkit.block.Biome biome, boolean cacheHit, boolean cacheChecked, Location validLocation) {
        sendSuccessMessage(player, destination, result);
        playParticles(destination, teleportSettings.getParticleSettings());
        statistics.recordAttempt(true, duration, biome, cacheHit, cacheChecked);
        if (!cacheHit && validLocation != null) {
            locationFinder.cacheValidLocation(validLocation, teleportSettings);
        }
        // Attempt to unload the old chunk if it's now empty
        unloadOldChunk(player.getLocation(), destination);
    }

    /**
     * Handles teleport failure.
     */
    public void handleFailure(Player player, RandomTeleportSettings teleportSettings,
                             SearchResult result, long duration, org.bukkit.block.Biome biome, boolean cacheHit, boolean cacheChecked) {
        SearchLimitType limitType = result != null ? result.limitType() : SearchLimitType.NONE;

        boolean cacheFallbackFailure = result != null
                && result.noCacheAvailable()
                && limitType == SearchLimitType.NONE;
        String reasonClass;

        if (cacheFallbackFailure) {
            reasonClass = "CACHE_FALLBACK_NO_CACHE";
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FALLBACK_NO_CACHE, player));
            triggerCacheWarmup(teleportSettings);
            statistics.recordGenericSearchErrorFailure();
        } else if (limitType != SearchLimitType.NONE && limitType != SearchLimitType.BIOME_REJECTIONS) {
            reasonClass = "SEARCH_LIMIT_" + limitType.name();
            switch (teleportSettings.getBiomeSearchSettings().getFailoverMode()) {
                case ABORT -> com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FAILED, player));
                default -> com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FAILED_SEARCH, player));
            }
            statistics.recordTimeoutFailure();
        } else if (result != null && result.noValidBiome()) {
            reasonClass = "NO_VALID_BIOME";
            if (!teleportSettings.isEnableFallbackToCache()) {
                com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FAILED_SEARCH, player));
            } else {
                com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FAILED_BIOME, player));
            }
            statistics.recordBiomeFailure(null);
        } else {
            reasonClass = "SEARCH_EXHAUSTED_OR_GENERIC";
            if (!teleportSettings.isEnableFallbackToCache()) {
                com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FAILED_SEARCH, player));
            } else {
                com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FAILED, player));
            }
            statistics.recordGenericSearchErrorFailure();
        }

        logFailureDebug(player, teleportSettings, result, reasonClass, limitType, duration, cacheHit);
        statistics.recordAttempt(false, duration, biome, cacheHit, cacheChecked);
    }

    private void logFailureDebug(Player player,
                                 RandomTeleportSettings teleportSettings,
                                 SearchResult result,
                                 String reasonClass,
                                 SearchLimitType limitType,
                                 long duration,
                                 boolean cacheHit) {
        if (teleportSettings == null || !teleportSettings.isDebugRejectionLoggingEnabled()) {
            return;
        }
        try {
            String playerName = player != null ? player.getName() : "unknown";
            String worldName = player != null && player.getWorld() != null ? player.getWorld().getName() : "unknown";
            plugin.getLogger().warning("EzRTP debug: failure breadcrumb"
                    + " player=" + playerName
                    + " world=" + worldName
                    + " reasonClass=" + reasonClass
                    + " limitType=" + limitType
                    + " noValidBiome=" + (result != null && result.noValidBiome())
                    + " fallbackUsed=" + (result != null && result.fallbackUsed())
                    + " noCacheAvailable=" + (result != null && result.noCacheAvailable())
                    + " durationMs=" + duration
                    + " cacheHit=" + cacheHit);
        } catch (Exception ignored) {
        }
    }

    private void sendSuccessMessage(Player player, Location destination, SearchResult result) {
        if (result != null && result.fallbackUsed()) {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_FALLBACK_SUCCESS,
                Map.of("x", String.valueOf(destination.getBlockX()),
                       "z", String.valueOf(destination.getBlockZ())), player));
        } else {
            com.skyblockexp.ezrtp.util.MessageUtil.send(player, messageProvider.format(MessageKey.TELEPORT_SUCCESS,
                Map.of("x", String.valueOf(destination.getBlockX()),
                       "z", String.valueOf(destination.getBlockZ()),
                       "world", destination.getWorld().getName()), player));
        }
    }

    private void playParticles(Location destination, ParticleSettings particleSettings) {
        if (particleSettings == null || !particleSettings.isEnabled()) {
            return;
        }
        World world = destination.getWorld();
        if (world == null) {
            return;
        }
        com.skyblockexp.ezrtp.util.compat.ParticleCompat.spawnParticle(world, particleSettings.getParticle(), destination,
                particleSettings.getCount(),
                particleSettings.getOffsetX(), particleSettings.getOffsetY(), particleSettings.getOffsetZ(),
                particleSettings.getExtra(), null, particleSettings.isForce());
    }

    private void unloadOldChunk(Location oldLocation, Location newLocation) {
        scheduler.runTask(plugin, () -> {
            try {
                org.bukkit.Chunk oldChunk = oldLocation.getWorld().getChunkAt(oldLocation);
                if (oldLocation.getWorld().isChunkLoaded(oldChunk.getX(), oldChunk.getZ())) {
                    // Check if chunk has no entities (mobs, items, etc.)
                    if (oldChunk.getEntities().length == 0) {
                        // Check if any online players are in this chunk
                        boolean hasPlayers = false;
                        for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                            if (p.getWorld().equals(oldLocation.getWorld()) &&
                                (p.getLocation().getBlockX() >> 4) == oldChunk.getX() &&
                                (p.getLocation().getBlockZ() >> 4) == oldChunk.getZ()) {
                                hasPlayers = true;
                                break;
                            }
                        }
                        if (!hasPlayers) {
                            oldLocation.getWorld().unloadChunkRequest(oldChunk.getX(), oldChunk.getZ());
                        }
                    }
                }
            } catch (Exception ignored) {
                // Ignore errors in chunk unloading
            }
        });
    }

    private void triggerCacheWarmup(RandomTeleportSettings teleportSettings) {
        if (teleportSettings == null
                || teleportSettings.getPreCacheSettings() == null
                || !teleportSettings.getPreCacheSettings().isEnabled()
                || !LocationValidator.hasBiomeFilters(teleportSettings)) {
            return;
        }

        World world = Bukkit.getWorld(teleportSettings.getWorldName());
        if (world == null) {
            return;
        }

        locationFinder.generateSafeLocationForCache(world, teleportSettings).thenAccept(location -> {
            if (location != null) {
                locationFinder.cacheValidLocation(location, teleportSettings);
            }
        });
    }
}
