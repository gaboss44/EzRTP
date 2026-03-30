package com.skyblockexp.ezrtp.storage;

import java.util.UUID;

public interface RtpUsageStorage {
    /**
     * Get the last RTP time for a player in a world (ms since epoch).
     */
    long getLastRtpTime(UUID player, String world);

    /**
     * Set the last RTP time for a player in a world.
     */
    void setLastRtpTime(UUID player, String world, long time);

    /**
     * Get the RTP usage count for a player in a world for a given period (e.g., day/week).
     */
    int getUsageCount(UUID player, String world, String period);

    /**
     * Increment the RTP usage count for a player in a world for a given period.
     */
    void incrementUsage(UUID player, String world, String period);

    /**
     * Reset the RTP usage count for a player in a world for a given period.
     */
    /**
     * Reset the RTP usage count for a player in a world for a given period.
     * If player or world is null, reset for all players/worlds.
     */
    void resetUsage(UUID player, String world, String period);

    /**
     * Save all data to storage (if needed).
     */
    void save();

    /**
     * Reload all data from storage (if needed).
     */
    void reload();
}