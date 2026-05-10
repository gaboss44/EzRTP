package com.skyblockexp.ezrtp.protection;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsClaimService;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Protection provider backed by TeamsAPI's {@link TeamsClaimService}.
 *
 * <p>TeamsAPI is a soft-dependency: if it is not installed on the server, this
 * provider reports itself as unavailable and skips all checks without error.
 * If TeamsAPI is installed but no team plugin has registered a claim provider,
 * {@link #isAvailable()} returns {@code false} at the time of the check and
 * the provider is silently skipped.</p>
 *
 * <p>Availability is re-evaluated on every call to {@link #isAvailable()} so
 * that a claim provider registered after EzRTP starts is picked up
 * automatically without requiring a reload.</p>
 */
public final class TeamsApiProtectionProvider implements ProtectionProvider {

    private final Logger logger;
    /**
     * {@code true} when the TeamsAPI plugin was detected as loaded and enabled
     * at the time this provider was constructed. When {@code false}, every
     * method short-circuits immediately.
     */
    private final boolean teamsApiPluginPresent;

    public TeamsApiProtectionProvider(PluginManager pluginManager, Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
        boolean present = false;
        if (pluginManager != null) {
            try {
                Plugin plugin = pluginManager.getPlugin("TeamsAPI");
                present = plugin != null && plugin.isEnabled();
            } catch (Exception e) {
                // Defensive: plugin manager threw unexpectedly; treat as absent.
            }
        }
        this.teamsApiPluginPresent = present;
    }

    @Override
    public String getId() {
        return "teamsapi";
    }

    /**
     * Returns {@code true} when TeamsAPI is installed <em>and</em> a
     * {@link TeamsClaimService} provider is currently registered.
     *
     * <p>This check is dynamic: it re-queries Bukkit's service registry on
     * every call so that a claim plugin registered after server startup is
     * transparently picked up.</p>
     */
    @Override
    public boolean isAvailable() {
        if (!teamsApiPluginPresent) {
            return false;
        }
        try {
            return TeamsAPI.isClaimAvailable();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    @Override
    public boolean isLocationProtected(Location location) {
        if (!teamsApiPluginPresent || location == null) {
            return false;
        }
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        try {
            TeamsClaimService claimService = TeamsAPI.getClaimService();
            if (claimService == null) {
                return false;
            }
            Chunk chunk = location.getChunk();
            return claimService.isClaimed(world.getName(), chunk.getX(), chunk.getZ());
        } catch (NoClassDefFoundError | RuntimeException e) {
            logger.log(Level.WARNING, "Failed to query TeamsAPI claims for RTP protection check.", e);
            return false;
        }
    }
}
