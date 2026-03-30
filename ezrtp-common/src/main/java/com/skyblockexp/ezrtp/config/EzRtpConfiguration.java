package com.skyblockexp.ezrtp.config;

import net.kyori.adventure.text.Component;
import com.skyblockexp.ezrtp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.block.Biome;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import com.skyblockexp.ezrtp.util.ItemFlagUtil;

/**
 * Aggregates the configuration used by the EzRTP plugin including the optional GUI definition.
 */
public final class EzRtpConfiguration {

    /**
     * Holds parsed RTP cooldown and usage limit settings.
     */
    public static class RtpLimitSettings {
        public final int cooldownSeconds;
        public final int dailyLimit;
        public final int weeklyLimit;
        public final Double cost;
        public final boolean disableDailyLimit;
        public RtpLimitSettings(int cooldownSeconds, int dailyLimit, int weeklyLimit, Double cost) {
            this(cooldownSeconds, dailyLimit, weeklyLimit, cost, false);
        }
        public RtpLimitSettings(int cooldownSeconds, int dailyLimit, int weeklyLimit, Double cost, boolean disableDailyLimit) {
            this.cooldownSeconds = cooldownSeconds;
            this.dailyLimit = dailyLimit;
            this.weeklyLimit = weeklyLimit;
            this.cost = cost;
            this.disableDailyLimit = disableDailyLimit;
        }
    }

    private final Map<String, Map<String, RtpLimitSettings>> worldGroupLimits = new LinkedHashMap<>();
    private final RtpLimitSettings defaultLimitSettings;
    private final java.util.List<String> bypassPermissions;

    

    private final RandomTeleportSettings defaultSettings;
    private final GuiSettings guiSettings;
    private final TeleportQueueSettings queueSettings;
    private final NetworkConfiguration networkConfiguration;
    private final boolean allowGuiDuringCooldown;
    private final boolean humanReadableCooldown;
    private final Map<String, NamedCenter> namedCenters;

    private EzRtpConfiguration(RandomTeleportSettings defaultSettings, GuiSettings guiSettings,
                               TeleportQueueSettings queueSettings, NetworkConfiguration networkConfiguration,
                               boolean allowGuiDuringCooldown, boolean humanReadableCooldown,
                               Map<String, NamedCenter> namedCenters) {
        this.defaultSettings = defaultSettings;
        this.guiSettings = guiSettings;
        this.queueSettings = queueSettings;
        this.networkConfiguration = networkConfiguration;
        this.allowGuiDuringCooldown = allowGuiDuringCooldown;
        this.humanReadableCooldown = humanReadableCooldown;
        this.namedCenters = namedCenters;
            // Parse rtp-limits from config
            ConfigurationSection config = defaultSettings.getConfigSection().getRoot();
            ConfigurationSection rtpLimits = config.getConfigurationSection("rtp-limits");
            if (rtpLimits != null) {
                // Default
                ConfigurationSection def = rtpLimits.getConfigurationSection("default");
                this.defaultLimitSettings = parseLimitSection(def);
                // Per-world
                ConfigurationSection worlds = rtpLimits.getConfigurationSection("worlds");
                if (worlds != null) {
                    for (String world : worlds.getKeys(false)) {
                        ConfigurationSection worldSec = worlds.getConfigurationSection(world);
                        Map<String, RtpLimitSettings> groupMap = new LinkedHashMap<>();
                        for (String group : worldSec.getKeys(false)) {
                            ConfigurationSection groupSec = worldSec.getConfigurationSection(group);
                            groupMap.put(group, parseLimitSection(groupSec));
                        }
                        worldGroupLimits.put(world, groupMap);
                    }
                }
                // Bypass permissions
                this.bypassPermissions = rtpLimits.getStringList("bypass-permissions");
            } else {
                this.defaultLimitSettings = new RtpLimitSettings(300, 10, 50, null);
                this.bypassPermissions = java.util.Collections.emptyList();
            }
        }

        private static RtpLimitSettings parseLimitSection(ConfigurationSection section) {
            if (section == null) return new RtpLimitSettings(300, 10, 50, null);
            int cooldown = section.getInt("cooldown-seconds", 300);
            int daily = section.getInt("daily-limit", 10);
            int weekly = section.getInt("weekly-limit", 50);
            Double cost = section.contains("cost") ? section.getDouble("cost") : null;
            return new RtpLimitSettings(cooldown, daily, weekly, cost);
        }

        /**
         * Get the RTP limit settings for a world and group (or "default").
         */
        public RtpLimitSettings getLimitSettings(String world, String group) {
            Map<String, RtpLimitSettings> groupMap = worldGroupLimits.get(world);
            if (groupMap != null) {
                if (group != null && groupMap.containsKey(group)) {
                    return groupMap.get(group);
                }
                if (groupMap.containsKey("default")) {
                    return groupMap.get("default");
                }
            }
            return defaultLimitSettings;
        }

        public java.util.List<String> getBypassPermissions() {
	    return bypassPermissions;
        }

        public String resolveGroup(org.bukkit.entity.Player player, String world) {
            if (player == null || world == null) {
                return null;
            }
            Map<String, RtpLimitSettings> groupMap = worldGroupLimits.get(world);
            if (groupMap == null) {
                return null;
            }
            for (String groupKey : groupMap.keySet()) {
                if (!groupKey.startsWith("group.")) {
                    continue;
                }
                String groupName = groupKey.substring("group.".length());
                if (groupName.isEmpty()) {
                    continue;
                }
                if (player.hasPermission("ezrtp.group." + groupName)) {
                    return groupKey;
                }
            }
            return null;
        }

        public double resolveTeleportCost(org.bukkit.entity.Player player, RandomTeleportSettings settings) {
            if (settings == null) {
                return 0.0D;
            }
            double fallbackCost = settings.getTeleportCost();
            if (player == null) {
                return fallbackCost;
            }
            String world = settings.getWorldName();
            String group = resolveGroup(player, world);
            Map<String, RtpLimitSettings> groupMap = worldGroupLimits.get(world);
            if (groupMap != null) {
                if (group != null) {
                    RtpLimitSettings groupSettings = groupMap.get(group);
                    if (groupSettings != null && groupSettings.cost != null) {
                        return groupSettings.cost;
                    }
                }
                RtpLimitSettings worldDefault = groupMap.get("default");
                if (worldDefault != null && worldDefault.cost != null) {
                    return worldDefault.cost;
                }
            }
            if (defaultLimitSettings != null && defaultLimitSettings.cost != null) {
                return defaultLimitSettings.cost;
            }
            return fallbackCost;
        }

    public RandomTeleportSettings getDefaultSettings() {
        return defaultSettings;
    }

    /**
     * Gets the teleport settings for a specific world.
     * If the world is not found in GUI configuration, returns default settings.
     *
     * @param worldName The name of the world
     * @return RandomTeleportSettings for the world, or default settings if not found
     */
    public RandomTeleportSettings getSettingsForWorld(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return defaultSettings;
        }

        // Look through GUI world options for matching world
        for (GuiWorldOption option : guiSettings.getWorldOptions()) {
            if (worldName.equals(option.getSettings().getWorldName())) {
                return option.getSettings();
            }
        }

        // Fall back to default settings if world not found
        return defaultSettings;
    }

    public GuiSettings getGuiSettings() {
        return guiSettings;
    }

    public TeleportQueueSettings getQueueSettings() {
        return queueSettings;
    }

    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    /**
     * Checks if GUI can be opened during cooldown period.
     * @return true if GUI should be allowed during cooldown
     */
    public boolean isAllowGuiDuringCooldown() {
        return allowGuiDuringCooldown;
    }

    /**
     * Checks if cooldown messages should use human-readable time format.
     * @return true if human-readable format should be used
     */
    public boolean isHumanReadableCooldown() {
        return humanReadableCooldown;
    }

    public List<String> getNamedCenterNames() {
        return new ArrayList<>(namedCenters.keySet());
    }

    public Optional<NamedCenter> getNamedCenter(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(namedCenters.get(name.toLowerCase(Locale.ROOT)));
    }

    public static EzRtpConfiguration fromConfiguration(FileConfiguration configuration, Logger logger) {
        return fromConfigurations(configuration,
                configuration.getConfigurationSection("messages"),
                configuration.getConfigurationSection("gui"),
                configuration.getConfigurationSection("queue"),
                configuration.getConfigurationSection("network"),
                logger);
    }

    public static EzRtpConfiguration fromConfigurations(ConfigurationSection baseConfiguration,
                                                        ConfigurationSection messagesConfiguration,
                                                        ConfigurationSection guiConfiguration,
                                                        ConfigurationSection queueConfiguration,
                                                        ConfigurationSection networkConfiguration,
                                                        Logger logger) {
        MemoryConfiguration mergedConfiguration = new MemoryConfiguration();
        if (baseConfiguration != null) {
            copySection(baseConfiguration, mergedConfiguration);
        }
        if (isSectionPopulated(messagesConfiguration)) {
            mergedConfiguration.set("messages", null);
            copySection(messagesConfiguration, mergedConfiguration.createSection("messages"));
        }
        RandomTeleportSettings defaultSettings = RandomTeleportSettings.fromConfiguration(mergedConfiguration, logger);
        NetworkConfiguration networkConfig = NetworkConfiguration.fromConfiguration(networkConfiguration, logger);
        GuiSettings guiSettings = GuiSettings.fromConfiguration(guiConfiguration, defaultSettings, networkConfig, 
                defaultSettings.getRareBiomeOptimizationSettings(), logger);
        TeleportQueueSettings queueSettings = TeleportQueueSettings.fromConfiguration(queueConfiguration);
        
        // Parse additional configuration options
        boolean allowGuiDuringCooldown = baseConfiguration != null && baseConfiguration.getBoolean("rtp-limits.allow-gui-during-cooldown", true);
        boolean humanReadableCooldown = baseConfiguration != null && baseConfiguration.getBoolean("rtp.human-readable-cooldown", true);
        Map<String, NamedCenter> namedCenters = parseNamedCenters(baseConfiguration, logger);
        
        return new EzRtpConfiguration(defaultSettings, guiSettings, queueSettings, networkConfig,
                allowGuiDuringCooldown, humanReadableCooldown, namedCenters);
    }

    private static void copySection(ConfigurationSection source, ConfigurationSection target) {
        if (source == null || target == null) {
            return;
        }
        for (String key : source.getKeys(false)) {
            Object value = source.get(key);
            if (value instanceof ConfigurationSection) {
                ConfigurationSection existing = target.getConfigurationSection(key);
                if (existing == null) {
                    existing = target.createSection(key);
                }
                copySection((ConfigurationSection) value, existing);
            } else {
                target.set(key, value);
            }
        }
    }

    // Legacy-friendly compatibility constructor used by tests that expect the older 4-arg signature.
    private EzRtpConfiguration(RandomTeleportSettings defaultSettings,
                               GuiSettings guiSettings,
                               TeleportQueueSettings queueSettings,
                               NetworkConfiguration networkConfiguration) {
        this(defaultSettings, guiSettings, queueSettings, networkConfiguration, true, true,
                Collections.emptyMap());
    }

    private static boolean isSectionPopulated(ConfigurationSection section) {
        return section != null && !section.getKeys(true).isEmpty();
    }

    private static Map<String, NamedCenter> parseNamedCenters(ConfigurationSection baseConfiguration, Logger logger) {
        if (baseConfiguration == null) {
            return Collections.emptyMap();
        }
        ConfigurationSection centersSection = baseConfiguration.getConfigurationSection("centers.named");
        if (centersSection == null) {
            return Collections.emptyMap();
        }

        Map<String, NamedCenter> parsed = new LinkedHashMap<>();
        for (String rawName : centersSection.getKeys(false)) {
            ConfigurationSection entry = centersSection.getConfigurationSection(rawName);
            if (entry == null) {
                continue;
            }

            String normalizedName = rawName.toLowerCase(Locale.ROOT);
            String world = entry.getString("world", baseConfiguration.getString("world", ""));
            if (world == null || world.isBlank()) {
                logger.warning("Named center '" + rawName + "' is missing a world and was skipped.");
                continue;
            }
            int x = entry.getInt("center.x", 0);
            int z = entry.getInt("center.z", 0);
            parsed.put(normalizedName, new NamedCenter(rawName, world, x, z));
        }
        return Collections.unmodifiableMap(parsed);
    }

    public record NamedCenter(String name, String world, int x, int z) {
    }

    /**
     * Configuration for rare biomes behavior in GUI.
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
     * Holds the parsed configuration for the optional world selection GUI.
     */
    public static final class GuiSettings {

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

        private GuiSettings(boolean enabled, Component title, int size, ItemStack fillerItem,
                            List<GuiWorldOption> worldOptions, List<GuiServerOption> serverOptions,
                            Component noPermissionMessage, boolean hasNoPermissionMessage,
                            Component cacheFilterInfo, Component noDestinations,
                            boolean disableCacheFiltering, boolean adminOnlyCacheInfo) {
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

        // Legacy-friendly constructor used by some tests/reflection-based callers.
        private GuiSettings(boolean enabled, Component title, int size, ItemStack fillerItem,
                    List<GuiWorldOption> worldOptions, List<GuiServerOption> serverOptions,
                    Component noPermissionMessage, boolean hasNoPermissionMessage) {
            this(enabled, title, size, fillerItem, worldOptions, serverOptions,
                noPermissionMessage, hasNoPermissionMessage,
                Component.empty(), com.skyblockexp.ezrtp.util.MessageUtil.parseMiniMessage("<red>No teleport destinations are currently available.</red>"), false, false);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Component getTitle() {
            return title;
        }

        public int getSize() {
            return size;
        }

        public Optional<ItemStack> getFillerItem() {
            return Optional.ofNullable(fillerItem);
        }

        public List<GuiWorldOption> getWorldOptions() {
            return worldOptions;
        }

        public List<GuiServerOption> getServerOptions() {
            return serverOptions;
        }

        public Optional<Component> noPermissionMessage() {
            return hasNoPermissionMessage ? Optional.of(noPermissionMessage) : Optional.empty();
        }
        
        public Component getCacheFilterInfo() {
            return cacheFilterInfo;
        }
        
        public Component getNoDestinations() {
            return noDestinations;
        }

        /**
         * Checks if cache filtering should be disabled in the GUI.
         * @return true if cache filtering is disabled
         */
        public boolean isDisableCacheFiltering() {
            return disableCacheFiltering;
        }

        /**
         * Checks if cache info should only be shown to admins.
         * @return true if cache info is admin-only
         */
        public boolean isAdminOnlyCacheInfo() {
            return adminOnlyCacheInfo;
        }

        private static GuiSettings fromConfiguration(ConfigurationSection section,
                                                      RandomTeleportSettings defaultSettings,
                                                      NetworkConfiguration networkConfiguration,
                                                      RareBiomeOptimizationSettings rareBiomeOptimization,
                                                      Logger logger) {
            if (section == null) {
                return disabled();
            }

            boolean enabled = section.getBoolean("enabled", false);
            String titleRaw = section.getString("title", "<gold>Select a destination</gold>");
            Component title = com.skyblockexp.ezrtp.util.MessageUtil.parseMiniMessage(titleRaw == null ? "<gold>Select a destination</gold>" : titleRaw);

            int rows = Math.max(1, Math.min(MAX_ROWS, section.getInt("rows", 1)));
            int size = rows * 9;

            ItemStack filler = parseFiller(section.getConfigurationSection("filler"));

            Component noPermission = null;
            boolean hasNoPermission = false;
            if (section.isString("no-permission-message")) {
                String messageRaw = section.getString("no-permission-message", "");
                if (messageRaw != null && !messageRaw.isBlank()) {
                    noPermission = com.skyblockexp.ezrtp.util.MessageUtil.parseMiniMessage(messageRaw);
                    hasNoPermission = true;
                }
            }
            
            // Parse GUI-specific messages
            String cacheFilterInfoRaw = section.getString("cache-filter-info", 
                    "<yellow>Only showing options with cached RTP locations available.</yellow>");
            Component cacheFilterInfo = com.skyblockexp.ezrtp.util.MessageUtil.parseMiniMessage(cacheFilterInfoRaw);
            
            String noDestinationsRaw = section.getString("no-destinations", 
                    "<red>No teleport destinations are currently available.</red>");
            Component noDestinations = com.skyblockexp.ezrtp.util.MessageUtil.parseMiniMessage(noDestinationsRaw);

            // Parse cache filtering configuration options
            boolean disableCacheFiltering = section.getBoolean("disable-cache-filtering", false);
            boolean adminOnlyCacheInfo = section.getBoolean("admin-only-cache-info", false);

            // Parse rare_biomes configuration from gui.yml using existing RareBiomeOptimizationSettings
            RareBiomesConfig rareBiomesConfig = parseRareBiomesConfig(
                    section.getConfigurationSection("rare_biomes"),
                    rareBiomeOptimization);

            Map<Integer, GuiWorldOption> worldOptionMap = parseWorldOptions(
                    section.getConfigurationSection("worlds"), size,
                    defaultSettings, rareBiomesConfig, logger);

            List<GuiWorldOption> options = new ArrayList<>(worldOptionMap.values());
            options.sort((left, right) -> Integer.compare(left.getSlot(), right.getSlot()));

            List<GuiServerOption> serverOptions = parseServerOptions(size, worldOptionMap,
                    networkConfiguration, logger);

            return new GuiSettings(enabled, title, size, filler, Collections.unmodifiableList(options),
                    Collections.unmodifiableList(serverOptions),
                    hasNoPermission ? noPermission : Component.empty(), hasNoPermission,
                    cacheFilterInfo, noDestinations, disableCacheFiltering, adminOnlyCacheInfo);
        }

        private static GuiSettings disabled() {
            return new GuiSettings(false, Component.text("Select a destination"), 9, null,
                    Collections.emptyList(), Collections.emptyList(), Component.empty(), false,
                    Component.text("Only showing options with cached RTP locations available."),
                    Component.text("No teleport destinations are currently available."), false, false);
        }

        private static RareBiomesConfig parseRareBiomesConfig(ConfigurationSection guiSection, 
                                                              RareBiomeOptimizationSettings rareBiomeOptimization) {
            // Get rare biomes from the existing RareBiomeOptimizationSettings (from config.yml)
            Set<String> rareBiomes = new HashSet<>();
            int defaultMinimum = 1;
            
            if (rareBiomeOptimization != null && rareBiomeOptimization.getRareBiomes() != null) {
                for (Biome biome : rareBiomeOptimization.getRareBiomes()) {
                    rareBiomes.add(biome.name().toUpperCase(Locale.ROOT));
                }
            }
            
            // Check gui.yml settings for enable/disable and minimum-cached override
            if (guiSection == null) {
                return new RareBiomesConfig(false, rareBiomes, defaultMinimum);
            }

            boolean enabled = guiSection.getBoolean("enabled", false);
            
            // Allow gui.yml to add additional rare biomes to the list (if present)
            if (guiSection.isList("list")) {
                List<String> additionalBiomes = guiSection.getStringList("list");
                for (String biome : additionalBiomes) {
                    if (biome != null && !biome.isBlank()) {
                        rareBiomes.add(biome.toUpperCase(Locale.ROOT));
                    }
                }
            }

            // Allow gui.yml to override minimum-cached
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
                Component component = com.skyblockexp.ezrtp.util.MessageUtil.parseMiniMessage(nameRaw);
                com.skyblockexp.ezrtp.util.compat.ItemMetaCompat.setDisplayName(meta, component);
            }
            if (section.contains("custom-model-data")) {
                meta.setCustomModelData(section.getInt("custom-model-data"));
            }
            ItemFlagUtil.applyStandardHideFlags(meta);
            ItemFlagUtil.setItemMetaCompatibly(itemStack, meta);
            return itemStack;
        }

        private static Map<Integer, GuiWorldOption> parseWorldOptions(ConfigurationSection section, int size,
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
                RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(settingsSection, logger,
                        defaultSettings);
                if (settings == null) {
                    continue;
                }

                int slot;
                if (entry.contains("slot")) {
                    int configuredSlot = entry.getInt("slot");
                    if (configuredSlot < 0 || configuredSlot >= size) {
                        logger.warning(String.format("GUI world option '%s' uses an invalid slot '%d'. Skipping entry.",
                                key, configuredSlot));
                        continue;
                    }
                    slot = configuredSlot;
                } else {
                    slot = nextSlot;
                }
                nextSlot = Math.min(size - 1, slot + 1);
                if (options.containsKey(slot)) {
                    logger.warning(String.format("Duplicate GUI slot '%d' detected for world option '%s'. Skipping entry.",
                            slot, key));
                    continue;
                }

                // Parse require-cache settings
                boolean requireCacheEnabled = false;
                int minimumCached = 1;

                // Check if settings has require-cache configuration
                ConfigurationSection requireCacheSection = settingsSection.getConfigurationSection("require-cache");
                if (requireCacheSection != null) {
                    requireCacheEnabled = requireCacheSection.getBoolean("enabled", false);
                    minimumCached = Math.max(0, requireCacheSection.getInt("minimum-cached", 1));
                } else if (rareBiomesConfig.isEnabled() && containsRareBiome(settings, rareBiomesConfig)) {
                    // Apply default rare biome settings if this option includes rare biomes
                    requireCacheEnabled = true;
                    minimumCached = rareBiomesConfig.getDefaultMinimumCached();
                }

                ParsedIcon parsedIcon = parseIcon(entry.getConfigurationSection("icon"), settings, logger);
                GuiWorldOption option = new GuiWorldOption(settings,
                        parsedIcon.template, slot,
                        entry.getString("permission", ""), requireCacheEnabled, minimumCached,
                        parsedIcon.rawName, parsedIcon.rawLore);
                options.put(slot, option);
            }
            return options;
        }
        
        /**
         * Checks if the given settings include any rare biomes.
         * Uses stream.anyMatch() which short-circuits on first match for efficiency.
         */
        private static boolean containsRareBiome(RandomTeleportSettings settings, RareBiomesConfig rareBiomesConfig) {
            Set<org.bukkit.block.Biome> includeBiomes = settings.getBiomeInclude();
            if (includeBiomes.isEmpty()) {
                return false;
            }
            return includeBiomes.stream()
                    .anyMatch(biome -> rareBiomesConfig.isRareBiome(biome.name()));
        }

        private static List<GuiServerOption> parseServerOptions(int size,
                                                                Map<Integer, GuiWorldOption> worldOptions,
                                                                NetworkConfiguration networkConfiguration,
                                                                Logger logger) {
            if (networkConfiguration == null || !networkConfiguration.isEnabled()
                    || !networkConfiguration.isLobbyServer()) {
                return Collections.emptyList();
            }
            List<GuiServerOption> serverOptions = new ArrayList<>();
            Set<Integer> occupiedSlots = new HashSet<>(worldOptions.keySet());
            for (NetworkConfiguration.NetworkServer server : networkConfiguration.getServers()) {
                int slot = server.getSlot();
                if (slot < 0 || slot >= size) {
                    logger.warning(String.format("Network server '%s' uses an invalid slot '%d'. Skipping entry.",
                            server.getId(), slot));
                    continue;
                }
                if (!occupiedSlots.add(slot)) {
                    logger.warning(String.format("Network server '%s' cannot use GUI slot '%d' because it is already occupied. Skipping entry.",
                            server.getId(), slot));
                    continue;
                }
                serverOptions.add(new GuiServerOption(server));
            }
            return serverOptions;
        }

        /**
         * Helper class to hold parsed icon data including raw strings for placeholder resolution.
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

        private static ParsedIcon parseIcon(ConfigurationSection section, RandomTeleportSettings settings, Logger logger) {
            String defaultMaterial = settings.getWorldName().equalsIgnoreCase("world_nether") ? "NETHERRACK"
                : settings.getWorldName().equalsIgnoreCase("world_the_end") ? "END_STONE" : "GRASS_BLOCK";
            Material material = parseMaterial(section != null ? section.getString("material") : null, defaultMaterial);
            int amount = 1;
            if (section != null) {
                amount = Math.max(1, section.getInt("amount", 1));
            }
            ItemStack stack = new ItemStack(material, amount);
            ItemMeta meta = stack.getItemMeta();
            String defaultName = "<green>" + settings.getWorldName() + "</green>";
            String nameRaw = section != null ? section.getString("name", defaultName) : defaultName;
            com.skyblockexp.ezrtp.util.LoreUtil.validateMiniMessage(nameRaw, "GUI world option name for " + settings.getWorldName(), logger);
            if (nameRaw != null && !nameRaw.isBlank()) {
                Component component = MessageUtil.parseMiniMessage(nameRaw);
                com.skyblockexp.ezrtp.util.compat.ItemMetaCompat.setDisplayName(meta, component);
                // Defensive fallback: also attempt to set a legacy-string display name so
                // runtimes that don't accept the Component form still receive a properly
                // serialized legacy string (with § codes) and don't show raw MiniMessage tags.
                try {
                    String legacy = com.skyblockexp.ezrtp.util.MessageUtil.componentToLegacy(component);
                    if (legacy == null) legacy = "";
                    try {
                        java.lang.reflect.Method setStr = meta.getClass().getMethod("setDisplayName", String.class);
                        setStr.invoke(meta, legacy);
                    } catch (NoSuchMethodException nsme) {
                        try { meta.setDisplayName(legacy); } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}
            }
            List<String> rawLore = new ArrayList<>();
            if (section != null && section.isList("lore")) {
                rawLore = section.getStringList("lore");
                for (String line : rawLore) {
                    com.skyblockexp.ezrtp.util.LoreUtil.validateMiniMessage(line, "GUI world option lore for " + settings.getWorldName(), logger);
                }
            }
            if (section != null && section.contains("custom-model-data")) {
                meta.setCustomModelData(section.getInt("custom-model-data"));
            }
            ItemFlagUtil.applyStandardHideFlags(meta);
            ItemFlagUtil.setItemMetaCompatibly(stack, meta);
            return new ParsedIcon(stack, nameRaw, rawLore);
        }

        private static Material parseMaterial(String key, String defaultName) {
            // Build candidate names to try, avoiding direct references to newer enum constants
            List<String> candidates = new ArrayList<>();
            if (key != null && !key.isBlank()) {
                candidates.add(key.trim().toUpperCase(Locale.ROOT));
            }
            if (defaultName != null && !defaultName.isBlank()) {
                candidates.add(defaultName.trim().toUpperCase(Locale.ROOT));
            }

            // Derived fallbacks for newer-style names (e.g., GRAY_STAINED_GLASS_PANE -> STAINED_GLASS_PANE -> GLASS_PANE)
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
            // Add some sensible universal fallbacks
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

            // Ultimate safe fallback
            return Material.STONE;
        }
    }

    /**
     * Represents a single selectable world in the GUI.
     */
    public static final class GuiWorldOption {

        private final RandomTeleportSettings settings;
        private final ItemStack iconTemplate;
        private final int slot;
        private final String permission;
        private final boolean requireCacheEnabled;
        private final int minimumCached;
        private final String rawDisplayName;
        private final List<String> rawLore;

        private GuiWorldOption(RandomTeleportSettings settings, ItemStack iconTemplate, int slot, String permission,
                              boolean requireCacheEnabled, int minimumCached, String rawDisplayName, List<String> rawLore) {
            this.settings = settings;
            this.iconTemplate = iconTemplate;
            this.slot = slot;
            this.permission = permission == null ? "" : permission;
            this.requireCacheEnabled = requireCacheEnabled;
            this.minimumCached = minimumCached;
            this.rawDisplayName = rawDisplayName;
            this.rawLore = rawLore != null ? Collections.unmodifiableList(new ArrayList<>(rawLore)) : Collections.emptyList();
        }

        public RandomTeleportSettings getSettings() {
            return settings;
        }

        public ItemStack createIcon() {
            return iconTemplate.clone();
        }
        
        /**
         * Creates an icon with PlaceholderAPI placeholders resolved for the given player.
         * 
         * @param player The player context for placeholder resolution
         * @param logger Logger for warnings
         * @return ItemStack with resolved placeholders
         */
        public int getSlot() {
            return slot;
        }

        public String getPermission() {
            return permission;
        }
        
        public boolean isRequireCacheEnabled() {
            return requireCacheEnabled;
        }
        
        public int getMinimumCached() {
            return minimumCached;
        }

        public String getRawDisplayName() {
            return rawDisplayName;
        }

        public List<String> getRawLore() {
            return rawLore;
        }
    }

    /**
     * Represents a selectable network server entry in the GUI.
     */
    public static final class GuiServerOption {

        private final NetworkConfiguration.NetworkServer server;

        private GuiServerOption(NetworkConfiguration.NetworkServer server) {
            this.server = server;
        }

        public NetworkConfiguration.NetworkServer getServer() {
            return server;
        }
    }
}
