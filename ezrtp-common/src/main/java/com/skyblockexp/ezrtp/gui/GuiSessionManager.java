package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active GUI sessions for players.
 */
public class GuiSessionManager {

    private final Map<UUID, GuiSession> openMenus = new ConcurrentHashMap<>();

    /**
     * Starts a new GUI session for the player.
     *
     * @param player The player
     * @param inventory The GUI inventory
     * @param optionMap The slot to option mapping
     * @param settings The GUI settings
     */
    public void startSession(Player player, Inventory inventory,
                           Map<Integer, GuiOption> optionMap,
                           EzRtpConfiguration.GuiSettings settings) {
        GuiSession session = new GuiSession(inventory, optionMap, settings);
        openMenus.put(player.getUniqueId(), session);
    }

    /**
     * Gets the current session for a player.
     *
     * @param player The player
     * @return The session, or null if no session is active
     */
    public GuiSession getSession(Player player) {
        return openMenus.get(player.getUniqueId());
    }

    /**
     * Ends the session for a player.
     *
     * @param player The player
     * @return The ended session, or null if no session was active
     */
    public GuiSession endSession(Player player) {
        return openMenus.remove(player.getUniqueId());
    }

    /**
     * Checks if a player has an active session.
     *
     * @param player The player
     * @return true if the player has an active session
     */
    public boolean hasSession(Player player) {
        return openMenus.containsKey(player.getUniqueId());
    }

    /**
     * Clears all active sessions.
     */
    public void clearAllSessions() {
        openMenus.clear();
    }

    /**
     * Gets all active sessions.
     *
     * @return Map of player UUID to session
     */
    public Map<UUID, GuiSession> getAllSessions() {
        return new ConcurrentHashMap<>(openMenus);
    }

    /**
     * Represents an active GUI session.
     */
    public static class GuiSession {
        private final Inventory inventory;
        private final Map<Integer, GuiOption> options;
        private final EzRtpConfiguration.GuiSettings settings;

        public GuiSession(Inventory inventory, Map<Integer, GuiOption> options,
                         EzRtpConfiguration.GuiSettings settings) {
            this.inventory = inventory;
            this.options = options;
            this.settings = settings;
        }

        public Inventory inventory() {
            return inventory;
        }

        public Map<Integer, GuiOption> options() {
            return options;
        }

        public EzRtpConfiguration.GuiSettings settings() {
            return settings;
        }
    }
}