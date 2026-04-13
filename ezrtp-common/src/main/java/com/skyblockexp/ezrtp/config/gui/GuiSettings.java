package com.skyblockexp.ezrtp.config.gui;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.biome.RareBiomeOptimizationSettings;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
import com.skyblockexp.ezrtp.util.ItemFlagUtil;
import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Holds the parsed configuration for the optional world-selection GUI.
 *
 * <p>Instances are produced by {@link #fromConfiguration(ConfigurationSection,
 * RandomTeleportSettings, NetworkConfiguration, RareBiomeOptimizationSettings, Logger)} or the
 * no-argument {@link #disabled()} factory and are immutable after construction.
 */
public final class GuiSettings {

    private static final int MAX_ROWS = 6;

    private final boolean enabled;
    private final Component title;
    private final int size;
    private final ItemStack fillerItem;
    private final List<GuiWorldOption> worldOptions;
    private final List<GuiServerOption> serverOptions;
    private final Component noPermissionMessage;
    private final boolean hasNoPermissionMessage;
    private final Component cacheFilterInfo;
    private final Component noDestinations;
    private final boolean disableCacheFiltering;
    private final boolean adminOnlyCacheInfo;

    private GuiSettings(
            boolean enabled,
            Component title,
            int size,
            ItemStack fillerItem,
            List<GuiWorldOption> worldOptions,
            List<GuiServerOption> serverOptions,
            Component noPermissionMessage,
            boolean hasNoPermissionMessage,
            Component cacheFilterInfo,
            Component noDestinations,
            boolean disableCacheFiltering,
            boolean adminOnlyCacheInfo) {
        this.enabled = enabled;
        this.title = title;
        this.size = size;
        this.fillerItem = fillerItem;
        this.worldOptions = worldOptions;
        this.serverOptions = serverOptions;
        this.noPermissionMessage = noPermissionMessage;
        this.hasNoPermissionMessage = hasNoPermissionMessage;
        this.cacheFilterInfo = cacheFilterInfo;
        this.noDestinations = noDestinations;
        this.disableCacheFiltering = disableCacheFiltering;
        this.adminOnlyCacheInfo = adminOnlyCacheInfo;
    }

    // Legacy-friendly constructor used by some tests / reflection-based callers.
    private GuiSettings(
            boolean enabled,
            Component title,
            int size,
            ItemStack fillerItem,
            List<GuiWorldOption> worldOptions,
            List<GuiServerOption> serverOptions,
            Component noPermissionMessage,
            boolean hasNoPermissionMessage) {
        this(
                enabled,
                title,
                size,
                fillerItem,
                worldOptions,
                serverOptions,
                noPermissionMessage,
                hasNoPermissionMessage,
                Component.empty(),
                MessageUtil.parseMiniMessage(
                        "<red>No teleport destinations are currently available.</red>"),
                false,
                false);
    }

    /**
     * Returns {@code true} if the GUI is configured as enabled.
     *
     * @return {@code true} when the GUI feature is active
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the rendered GUI title component.
     *
     * @return title component
     */
    public Component getTitle() {
        return title;
    }

    /**
     * Returns the total number of inventory slots in the GUI (rows × 9).
     *
     * @return inventory size
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the optional filler item used to fill empty GUI slots.
     *
     * @return filler item wrapped in an {@link Optional}; empty if no filler is configured
     */
    public Optional<ItemStack> getFillerItem() {
        return Optional.ofNullable(fillerItem);
    }

    /**
     * Returns the immutable list of world options shown in the GUI.
     *
     * @return world option list (may be empty)
     */
    public List<GuiWorldOption> getWorldOptions() {
        return worldOptions;
    }

    /**
     * Returns the immutable list of server options shown in the GUI.
     *
     * @return server option list (may be empty)
     */
    public List<GuiServerOption> getServerOptions() {
        return serverOptions;
    }

    /**
     * Returns the no-permission message if one has been configured.
     *
     * @return {@link Optional} containing the message component, or empty
     */
    public Optional<Component> noPermissionMessage() {
        return hasNoPermissionMessage ? Optional.of(noPermissionMessage) : Optional.empty();
    }

    /**
     * Returns the informational message shown when cache filtering is active.
     *
     * @return cache filter info component
     */
    public Component getCacheFilterInfo() {
        return cacheFilterInfo;
    }

    /**
     * Returns the message shown when no teleport destinations are available.
     *
     * @return no-destinations component
     */
    public Component getNoDestinations() {
        return noDestinations;
    }

    /**
     * Returns {@code true} if cache-based filtering of GUI options is disabled.
     *
     * @return {@code true} to skip cache filtering
     */
    public boolean isDisableCacheFiltering() {
        return disableCacheFiltering;
    }

    /**
     * Returns {@code true} if the cache availability info should only be shown to administrators.
     *
     * @return {@code true} for admin-only cache info
     */
    public boolean isAdminOnlyCacheInfo() {
        return adminOnlyCacheInfo;
    }

    // -------------------------------------------------------------------------
    // Factory methods (package-private so EzRtpConfiguration can call them)
    // -------------------------------------------------------------------------

    /**
     * Parses a GUI configuration section and assembles a fully populated {@code GuiSettings}.
     *
     * @param section              the {@code gui} configuration section, or {@code null}
     * @param defaultSettings      default teleport settings used as fallback for per-world overrides
     * @param networkConfiguration current network configuration (for server options)
     * @param rareBiomeOptimization rare-biome optimisation settings (used to seed rare-biome list)
     * @param logger               logger for warnings during parsing
     * @return a configured {@code GuiSettings} instance, or {@link #disabled()} if {@code section}
     *         is {@code null}
     */
    public static GuiSettings fromConfiguration(
            ConfigurationSection section,
            RandomTeleportSettings defaultSettings,
            NetworkConfiguration networkConfiguration,
            RareBiomeOptimizationSettings rareBiomeOptimization,
            Logger logger) {
        if (section == null) {
            return disabled();
        }

        boolean enabled = section.getBoolean("enabled", false);
        String titleRaw = section.getString("title", "<gold>Select a destination</gold>");
        Component title =
                MessageUtil.parseMiniMessage(
                        titleRaw == null ? "<gold>Select a destination</gold>" : titleRaw);

        int rows = Math.max(1, Math.min(MAX_ROWS, section.getInt("rows", 1)));
        int size = rows * 9;

        ItemStack filler = parseFiller(section.getConfigurationSection("filler"));

        Component noPermission = null;
        boolean hasNoPermission = false;
        if (section.isString("no-permission-message")) {
            String messageRaw = section.getString("no-permission-message", "");
            if (messageRaw != null && !messageRaw.isBlank()) {
                noPermission = MessageUtil.parseMiniMessage(messageRaw);
                hasNoPermission = true;
            }
        }

        String cacheFilterInfoRaw =
                section.getString(
                        "cache-filter-info",
                        "<yellow>Only showing options with cached RTP locations available.</yellow>");
        Component cacheFilterInfo = MessageUtil.parseMiniMessage(cacheFilterInfoRaw);

        String noDestinationsRaw =
                section.getString(
                        "no-destinations",
                        "<red>No teleport destinations are currently available.</red>");
        Component noDestinations = MessageUtil.parseMiniMessage(noDestinationsRaw);

        boolean disableCacheFiltering = section.getBoolean("disable-cache-filtering", false);
        boolean adminOnlyCacheInfo = section.getBoolean("admin-only-cache-info", false);

        RareBiomesConfig rareBiomesConfig =
                parseRareBiomesConfig(
                        section.getConfigurationSection("rare_biomes"), rareBiomeOptimization);

        Map<Integer, GuiWorldOption> worldOptionMap =
                parseWorldOptions(
                        section.getConfigurationSection("worlds"),
                        size,
                        defaultSettings,
                        rareBiomesConfig,
                        logger);

        List<GuiWorldOption> options = new ArrayList<>(worldOptionMap.values());
        options.sort((left, right) -> Integer.compare(left.getSlot(), right.getSlot()));

        List<GuiServerOption> serverOptions =
                parseServerOptions(size, worldOptionMap, networkConfiguration, logger);

        return new GuiSettings(
                enabled,
                title,
                size,
                filler,
                Collections.unmodifiableList(options),
                Collections.unmodifiableList(serverOptions),
                hasNoPermission ? noPermission : Component.empty(),
                hasNoPermission,
                cacheFilterInfo,
                noDestinations,
                disableCacheFiltering,
                adminOnlyCacheInfo);
    }

    /**
     * Returns a disabled {@code GuiSettings} instance with safe defaults and empty option lists.
     *
     * @return disabled GUI settings
     */
    public static GuiSettings disabled() {
        return new GuiSettings(
                false,
                Component.text("Select a destination"),
                9,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Component.empty(),
                false,
                Component.text("Only showing options with cached RTP locations available."),
                Component.text("No teleport destinations are currently available."),
                false,
                false);
    }

    // -------------------------------------------------------------------------
    // Private parsing helpers
    // -------------------------------------------------------------------------

    private static RareBiomesConfig parseRareBiomesConfig(
            ConfigurationSection guiSection,
            RareBiomeOptimizationSettings rareBiomeOptimization) {
        Set<String> rareBiomes = new HashSet<>();
        int defaultMinimum = 1;

        if (rareBiomeOptimization != null && rareBiomeOptimization.getRareBiomes() != null) {
            for (Biome biome : rareBiomeOptimization.getRareBiomes()) {
                rareBiomes.add(biome.name().toUpperCase(Locale.ROOT));
            }
        }

        if (guiSection == null) {
            return new RareBiomesConfig(false, rareBiomes, defaultMinimum);
        }

        boolean enabled = guiSection.getBoolean("enabled", false);

        if (guiSection.isList("list")) {
            List<String> additionalBiomes = guiSection.getStringList("list");
            for (String biome : additionalBiomes) {
                if (biome != null && !biome.isBlank()) {
                    rareBiomes.add(biome.toUpperCase(Locale.ROOT));
                }
            }
        }

        ConfigurationSection requireCache = guiSection.getConfigurationSection("require-cache");
        if (requireCache != null && requireCache.contains("minimum-cached")) {
            defaultMinimum = Math.max(0, requireCache.getInt("minimum-cached", defaultMinimum));
        }

        return new RareBiomesConfig(enabled, rareBiomes, defaultMinimum);
    }

    private static ItemStack parseFiller(ConfigurationSection section) {
        if (section == null || !section.getBoolean("enabled", false)) {
            return null;
        }
        String materialKey = section.getString("material", "GRAY_STAINED_GLASS_PANE");
        Material material = parseMaterial(materialKey, "GRAY_STAINED_GLASS_PANE");
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        String nameRaw = section.getString("name", "<gray> </gray>");
        if (nameRaw != null && !nameRaw.isBlank()) {
            Component component = MessageUtil.parseMiniMessage(nameRaw);
            com.skyblockexp.ezrtp.util.compat.ItemMetaCompat.setDisplayName(meta, component);
        }
        if (section.contains("custom-model-data")) {
            meta.setCustomModelData(section.getInt("custom-model-data"));
        }
        ItemFlagUtil.applyStandardHideFlags(meta);
        ItemFlagUtil.setItemMetaCompatibly(itemStack, meta);
        return itemStack;
    }

    private static Map<Integer, GuiWorldOption> parseWorldOptions(
            ConfigurationSection section,
            int size,
            RandomTeleportSettings defaultSettings,
            RareBiomesConfig rareBiomesConfig,
            Logger logger) {
        Map<Integer, GuiWorldOption> options = new LinkedHashMap<>();
        if (section == null) {
            return options;
        }
        int nextSlot = 0;
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            ConfigurationSection settingsSection = entry.getConfigurationSection("settings");
            if (settingsSection == null) {
                settingsSection = entry;
            }
            RandomTeleportSettings settings =
                    RandomTeleportSettings.fromConfiguration(settingsSection, logger, defaultSettings);
            if (settings == null) {
                continue;
            }

            int slot;
            if (entry.contains("slot")) {
                int configuredSlot = entry.getInt("slot");
                if (configuredSlot < 0 || configuredSlot >= size) {
                    logger.warning(
                            String.format(
                                    "GUI world option '%s' uses an invalid slot '%d'. Skipping entry.",
                                    key, configuredSlot));
                    continue;
                }
                slot = configuredSlot;
            } else {
                slot = nextSlot;
            }
            nextSlot = Math.min(size - 1, slot + 1);
            if (options.containsKey(slot)) {
                logger.warning(
                        String.format(
                                "Duplicate GUI slot '%d' detected for world option '%s'. Skipping entry.",
                                slot, key));
                continue;
            }

            boolean requireCacheEnabled = false;
            int minimumCached = 1;

            ConfigurationSection requireCacheSection =
                    settingsSection.getConfigurationSection("require-cache");
            if (requireCacheSection != null) {
                requireCacheEnabled = requireCacheSection.getBoolean("enabled", false);
                minimumCached = Math.max(0, requireCacheSection.getInt("minimum-cached", 1));
            } else if (rareBiomesConfig.isEnabled() && containsRareBiome(settings, rareBiomesConfig)) {
                requireCacheEnabled = true;
                minimumCached = rareBiomesConfig.getDefaultMinimumCached();
            }

            ParsedIcon parsedIcon = parseIcon(entry.getConfigurationSection("icon"), settings, logger);
            GuiWorldOption option =
                    new GuiWorldOption(
                            settings,
                            parsedIcon.template,
                            slot,
                            entry.getString("permission", ""),
                            requireCacheEnabled,
                            minimumCached,
                            parsedIcon.rawName,
                            parsedIcon.rawLore);
            options.put(slot, option);
        }
        return options;
    }

    private static boolean containsRareBiome(
            RandomTeleportSettings settings, RareBiomesConfig rareBiomesConfig) {
        Set<Biome> includeBiomes = settings.getBiomeInclude();
        if (includeBiomes.isEmpty()) {
            return false;
        }
        return includeBiomes.stream().anyMatch(biome -> rareBiomesConfig.isRareBiome(biome.name()));
    }

    private static List<GuiServerOption> parseServerOptions(
            int size,
            Map<Integer, GuiWorldOption> worldOptions,
            NetworkConfiguration networkConfiguration,
            Logger logger) {
        if (networkConfiguration == null
                || !networkConfiguration.isEnabled()
                || !networkConfiguration.isLobbyServer()) {
            return Collections.emptyList();
        }
        List<GuiServerOption> serverOptions = new ArrayList<>();
        Set<Integer> occupiedSlots = new HashSet<>(worldOptions.keySet());
        for (NetworkConfiguration.NetworkServer server : networkConfiguration.getServers()) {
            int slot = server.getSlot();
            if (slot < 0 || slot >= size) {
                logger.warning(
                        String.format(
                                "Network server '%s' uses an invalid slot '%d'. Skipping entry.",
                                server.getId(), slot));
                continue;
            }
            if (!occupiedSlots.add(slot)) {
                logger.warning(
                        String.format(
                                "Network server '%s' cannot use GUI slot '%d' because it is already"
                                        + " occupied. Skipping entry.",
                                server.getId(), slot));
                continue;
            }
            serverOptions.add(new GuiServerOption(server));
        }
        return serverOptions;
    }

    private static ParsedIcon parseIcon(
            ConfigurationSection section, RandomTeleportSettings settings, Logger logger) {
        String defaultMaterial =
                settings.getWorldName().equalsIgnoreCase("world_nether")
                        ? "NETHERRACK"
                        : settings.getWorldName().equalsIgnoreCase("world_the_end")
                                ? "END_STONE"
                                : "GRASS_BLOCK";
        Material material =
                parseMaterial(section != null ? section.getString("material") : null, defaultMaterial);
        int amount = 1;
        if (section != null) {
            amount = Math.max(1, section.getInt("amount", 1));
        }
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        String defaultName = "<green>" + settings.getWorldName() + "</green>";
        String nameRaw = section != null ? section.getString("name", defaultName) : defaultName;
        com.skyblockexp.ezrtp.util.LoreUtil.validateMiniMessage(
                nameRaw, "GUI world option name for " + settings.getWorldName(), logger);
        if (nameRaw != null && !nameRaw.isBlank()) {
            Component component = MessageUtil.parseMiniMessage(nameRaw);
            com.skyblockexp.ezrtp.util.compat.ItemMetaCompat.setDisplayName(meta, component);
            try {
                String legacy = MessageUtil.componentToLegacy(component);
                if (legacy == null) legacy = "";
                try {
                    java.lang.reflect.Method setStr =
                            meta.getClass().getMethod("setDisplayName", String.class);
                    setStr.invoke(meta, legacy);
                } catch (NoSuchMethodException nsme) {
                    try {
                        meta.setDisplayName(legacy);
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        List<String> rawLore = new ArrayList<>();
        if (section != null && section.isList("lore")) {
            rawLore = section.getStringList("lore");
            for (String line : rawLore) {
                com.skyblockexp.ezrtp.util.LoreUtil.validateMiniMessage(
                        line, "GUI world option lore for " + settings.getWorldName(), logger);
            }
        }
        if (section != null && section.contains("custom-model-data")) {
            meta.setCustomModelData(section.getInt("custom-model-data"));
        }
        ItemFlagUtil.applyStandardHideFlags(meta);
        ItemFlagUtil.setItemMetaCompatibly(stack, meta);
        return new ParsedIcon(stack, nameRaw, rawLore);
    }

    /**
     * Resolves a material name with a sequence of fallback candidates, ensuring compatibility
     * across Minecraft versions that may not have newer material enum constants.
     *
     * @param key         configured material name (may be {@code null})
     * @param defaultName fallback material name to try if {@code key} is absent
     * @return the first matching {@link Material}, or {@link Material#STONE} as ultimate fallback
     */
    static Material parseMaterial(String key, String defaultName) {
        List<String> candidates = new ArrayList<>();
        if (key != null && !key.isBlank()) {
            candidates.add(key.trim().toUpperCase(Locale.ROOT));
        }
        if (defaultName != null && !defaultName.isBlank()) {
            candidates.add(defaultName.trim().toUpperCase(Locale.ROOT));
        }

        List<String> derived = new ArrayList<>();
        for (String name : new ArrayList<>(candidates)) {
            if (name.endsWith("_STAINED_GLASS_PANE")) {
                derived.add("STAINED_GLASS_PANE");
                derived.add("GLASS_PANE");
            }
            if (name.endsWith("_BLOCK")) {
                derived.add(name.replace("_BLOCK", ""));
            }
        }
        derived.add("STAINED_GLASS_PANE");
        derived.add("GLASS_PANE");
        derived.add("GRASS");
        derived.add("STONE");

        candidates.addAll(derived);

        for (String candidate : candidates) {
            try {
                Material m = Material.matchMaterial(candidate);
                if (m != null) {
                    return m;
                }
            } catch (Throwable t) {
                // ignore and try next candidate
            }
        }

        return Material.STONE;
    }

    // -------------------------------------------------------------------------
    // Private inner helpers
    // -------------------------------------------------------------------------

    /**
     * Internal configuration for rare-biome display/filtering in the GUI.
     * Populated from both {@code rtp.yml} (via RareBiomeOptimizationSettings) and {@code gui.yml}.
     */
    private static final class RareBiomesConfig {
        private final boolean enabled;
        private final Set<String> rareBiomeList;
        private final int defaultMinimumCached;

        private RareBiomesConfig(boolean enabled, Set<String> rareBiomeList, int defaultMinimumCached) {
            this.enabled = enabled;
            this.rareBiomeList = rareBiomeList;
            this.defaultMinimumCached = defaultMinimumCached;
        }

        boolean isEnabled() {
            return enabled;
        }

        boolean isRareBiome(String biomeName) {
            return rareBiomeList.contains(biomeName.toUpperCase(Locale.ROOT));
        }

        int getDefaultMinimumCached() {
            return defaultMinimumCached;
        }
    }

    /**
     * Internal holder for icon data produced during per-world option parsing.
     * Stores both the pre-built {@link ItemStack} template and the raw MiniMessage strings
     * required for runtime placeholder resolution.
     */
    private static final class ParsedIcon {
        final ItemStack template;
        final String rawName;
        final List<String> rawLore;

        ParsedIcon(ItemStack template, String rawName, List<String> rawLore) {
            this.template = template;
            this.rawName = rawName;
            this.rawLore = rawLore != null ? rawLore : Collections.emptyList();
        }
    }
}
