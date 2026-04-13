package com.skyblockexp.ezrtp.config.teleport;

import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

/**
 * Holds raw MiniMessage templates for all teleport-related feedback messages and
 * provides helper methods that interpolate placeholders and return rendered
 * {@link Component} objects ready for delivery to players.
 *
 * <p>Instances are created through {@link #fromConfiguration(ConfigurationSection)} or
 * {@link #defaultMessages()}.
 */
public final class TeleportMessages {

    private final String teleporting;
    private final String teleportSuccess;
    private final String teleportFailure;
    private final String worldMissing;
    private final String joinSearching;
    private final String queueQueued;
    private final String queueFull;
    private final String insufficientFunds;
    private final String countdownStart;
    private final String countdownTick;
    private final String teleportFailureBiome;
    private final String teleportFallbackSuccess;
    private final String teleportFallbackNoCache;
    private final String teleportFailedSearch;

    /**
     * Creates a fully specified messages object.
     * All parameters are raw MiniMessage strings; {@code null} values fall back to built-in
     * defaults when the corresponding render method is called.
     *
     * @param teleporting           shown while searching for a location
     * @param teleportSuccess       shown after a successful teleport ({@code <x>}, {@code <z>}, {@code <world>} placeholders)
     * @param teleportFailure       shown when no safe location was found
     * @param worldMissing          shown when the configured world doesn't exist ({@code <world>} placeholder)
     * @param joinSearching         shown on first-join RTP searches
     * @param queueQueued           shown when a player enters the queue ({@code <position>} placeholder)
     * @param queueFull             shown when the queue is at capacity ({@code <size>} placeholder)
     * @param insufficientFunds     shown when economy balance is too low ({@code <cost>} placeholder)
     * @param countdownStart        shown at countdown start ({@code <seconds>} placeholder)
     * @param countdownTick         shown on each countdown tick ({@code <seconds>} placeholder)
     * @param teleportFailureBiome  shown when no matching biome was found
     * @param teleportFallbackSuccess shown when falling back to a cached location ({@code <x>}, {@code <z>} placeholders)
     * @param teleportFallbackNoCache shown when fallback cache is empty
     * @param teleportFailedSearch  shown when the search algorithm itself failed
     */
    public TeleportMessages(
            String teleporting,
            String teleportSuccess,
            String teleportFailure,
            String worldMissing,
            String joinSearching,
            String queueQueued,
            String queueFull,
            String insufficientFunds,
            String countdownStart,
            String countdownTick,
            String teleportFailureBiome,
            String teleportFallbackSuccess,
            String teleportFallbackNoCache,
            String teleportFailedSearch) {
        this.teleporting = teleporting;
        this.teleportSuccess = teleportSuccess;
        this.teleportFailure = teleportFailure;
        this.worldMissing = worldMissing;
        this.joinSearching = joinSearching;
        this.queueQueued = queueQueued;
        this.queueFull = queueFull;
        this.insufficientFunds = insufficientFunds;
        this.countdownStart = countdownStart;
        this.countdownTick = countdownTick;
        this.teleportFailureBiome = teleportFailureBiome;
        this.teleportFallbackSuccess = teleportFallbackSuccess;
        this.teleportFallbackNoCache = teleportFallbackNoCache;
        this.teleportFailedSearch = teleportFailedSearch;
    }

    /** @return component rendered from the "teleporting" template */
    public Component teleporting() {
        return MessageUtil.parseMiniMessage(teleporting);
    }

    /**
     * @param x     X coordinate of the destination
     * @param z     Z coordinate of the destination
     * @param world world name of the destination
     * @return component with {@code <x>}, {@code <z>}, {@code <world>} interpolated
     */
    public Component teleportSuccess(int x, int z, String world) {
        String processed = teleportSuccess
                .replace("<x>", Integer.toString(x))
                .replace("<z>", Integer.toString(z))
                .replace("<world>", world);
        return MessageUtil.parseMiniMessage(processed);
    }

    /** @return component rendered from the "teleport-failed" template */
    public Component teleportFailure() {
        return MessageUtil.parseMiniMessage(teleportFailure);
    }

    /** @return component rendered from the "teleport-failed-biome" template */
    public Component teleportFailureBiome() {
        return MessageUtil.parseMiniMessage(teleportFailureBiome);
    }

    /**
     * @param world the world name that is unavailable
     * @return component with {@code <world>} interpolated
     */
    public Component worldMissing(String world) {
        String processed = worldMissing.replace("<world>", world);
        return MessageUtil.parseMiniMessage(processed);
    }

    /** @return component rendered from the "join-searching" template */
    public Component joinSearching() {
        return MessageUtil.parseMiniMessage(joinSearching);
    }

    /**
     * @param position 1-based queue position
     * @return component with {@code <position>} interpolated
     */
    public Component queued(int position) {
        String processed = queueQueued.replace("<position>", Integer.toString(Math.max(position, 1)));
        return MessageUtil.parseMiniMessage(processed);
    }

    /**
     * @param maxSize maximum capacity of the queue
     * @return component with {@code <size>} interpolated
     */
    public Component queueFull(int maxSize) {
        String processed = queueFull.replace("<size>", Integer.toString(Math.max(maxSize, 0)));
        return MessageUtil.parseMiniMessage(processed);
    }

    /**
     * @param cost the required amount
     * @return component with {@code <cost>} interpolated
     */
    public Component insufficientFunds(double cost) {
        String processed =
                insufficientFunds.replace("<cost>", String.format(Locale.US, "%.2f", Math.max(cost, 0.0D)));
        return MessageUtil.parseMiniMessage(processed);
    }

    /**
     * @param seconds seconds remaining at countdown start
     * @return component with {@code <seconds>} interpolated
     */
    public Component countdownStart(int seconds) {
        String template =
                countdownStart != null
                        ? countdownStart
                        : "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>";
        String processed = template.replace("<seconds>", Integer.toString(seconds));
        return MessageUtil.parseMiniMessage(processed);
    }

    /**
     * @param seconds seconds remaining on this tick
     * @return component with {@code <seconds>} interpolated
     */
    public Component countdownTick(int seconds) {
        String template = countdownTick != null ? countdownTick : "<gray><seconds>...</gray>";
        String processed = template.replace("<seconds>", Integer.toString(seconds));
        return MessageUtil.parseMiniMessage(processed);
    }

    /**
     * @param x X coordinate of the fallback location
     * @param z Z coordinate of the fallback location
     * @return component with {@code <x>} and {@code <z>} interpolated
     */
    public Component teleportFallbackSuccess(int x, int z) {
        String template =
                teleportFallbackSuccess != null
                        ? teleportFallbackSuccess
                        : "<yellow>No locations found through search. Falling back to a cached random location:"
                                + " (<white><x></white>, <white><z></white>).</yellow>";
        String processed = template.replace("<x>", Integer.toString(x)).replace("<z>", Integer.toString(z));
        return MessageUtil.parseMiniMessage(processed);
    }

    /** @return component rendered from the "teleport-fallback-no-cache" template */
    public Component teleportFallbackNoCache() {
        String template =
                teleportFallbackNoCache != null
                        ? teleportFallbackNoCache
                        : "<red>No cached locations are available for teleportation."
                                + " Please wait for locations to be pre-cached.</red>";
        return MessageUtil.parseMiniMessage(template);
    }

    /** @return component rendered from the "teleport-failed-search" template */
    public Component teleportFailedSearch() {
        String template =
                teleportFailedSearch != null
                        ? teleportFailedSearch
                        : "<red>Search failed: No valid locations found.</red>";
        return MessageUtil.parseMiniMessage(template);
    }

    /**
     * Parses message templates from a configuration section, using built-in defaults for any
     * missing keys.
     *
     * @param section the {@code messages} configuration section, or {@code null}
     * @return a fully populated {@code TeleportMessages} instance
     */
    public static TeleportMessages fromConfiguration(ConfigurationSection section) {
        if (section == null) {
            return defaultMessages();
        }
        return new TeleportMessages(
                section.getString("teleporting", "<gray>Searching for a safe location...</gray>"),
                section.getString(
                        "teleport-success",
                        "<green>Teleported to <white><x></white>, <white><z></white>"
                                + " in <white><world></white>.</green>"),
                section.getString(
                        "teleport-failed", "<red>Unable to find a safe location. Please try again.</red>"),
                section.getString(
                        "world-missing",
                        "<red>The configured world '<white><world></white>' is not available.</red>"),
                section.getString("join-searching", "<gray>Finding you a safe place to explore...</gray>"),
                section.getString(
                        "queue-queued",
                        "<gray>You joined the random teleport queue. Position:"
                                + " <white><position></white>.</gray>"),
                section.getString(
                        "queue-full",
                        "<red>The random teleport queue is currently full. Please try again soon.</red>"),
                section.getString(
                        "insufficient-funds",
                        "<red>You need <white><cost></white> to use random teleport.</red>"),
                section.getString(
                        "countdown-start",
                        "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>"),
                section.getString("countdown-tick", "<gray><seconds>...</gray>"),
                section.getString(
                        "teleport-failed-biome", "<red>No valid biome was found. Please try again.</red>"),
                section.getString(
                        "teleport-fallback-success",
                        "<yellow>No locations found through search. Falling back to a cached random location:"
                                + " (<white><x></white>, <white><z></white>).</yellow>"),
                section.getString(
                        "teleport-fallback-no-cache",
                        "<red>No cached locations are available for teleportation."
                                + " Please wait for locations to be pre-cached.</red>"),
                section.getString(
                        "teleport-failed-search",
                        "<red>Search failed: No valid locations found.</red>"));
    }

    /**
     * Returns a {@code TeleportMessages} instance populated entirely with built-in English
     * defaults. Useful when no messages configuration is present.
     *
     * @return default messages instance
     */
    public static TeleportMessages defaultMessages() {
        return new TeleportMessages(
                "<gray>Searching for a safe location...</gray>",
                "<green>Teleported to <white><x></white>, <white><z></white>"
                        + " in <white><world></white>.</green>",
                "<red>Unable to find a safe location. Please try again.</red>",
                "<red>The configured world '<white><world></white>' is not available.</red>",
                "<gray>Finding you a safe place to explore...</gray>",
                "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>",
                "<red>The random teleport queue is currently full. Please try again soon.</red>",
                "<red>You need <white><cost></white> to use random teleport.</red>",
                "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>",
                "<gray><seconds>...</gray>",
                "<red>No valid biome was found. Please try again.</red>",
                "<yellow>No locations found through search. Falling back to a cached random location:"
                        + " (<white><x></white>, <white><z></white>).</yellow>",
                "<red>No cached locations are available for teleportation."
                        + " Please wait for locations to be pre-cached.</red>",
                "<red>Search failed: No valid locations found.</red>");
    }
}
