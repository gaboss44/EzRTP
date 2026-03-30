
package com.skyblockexp.ezrtp.config;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.Set;
import java.util.Locale;
import net.kyori.adventure.text.Component;


public final class RandomTeleportSettings {
    private final ConfigurationSection configSection;
    private final String worldName;
    private final int centerX;
    private final int centerZ;
    private final int minimumRadius;
    private final int maximumRadius;
    private final int maxAttempts;
    private final boolean useWorldBorderRadius;
    private final Set<Material> unsafeBlocks;
    private final TeleportMessages messages;
    private final ParticleSettings particleSettings;
    private final OnJoinTeleportSettings onJoinTeleportSettings;
    private final CountdownBossBarSettings countdownBossBarSettings;
    private final CountdownParticleSettings countdownParticleSettings;
    private final double teleportCost;
    private final int countdownSeconds;
    private final boolean countdownChatMessagesEnabled;
    private final boolean debugRejectionLogging;
    private final Integer minY;
    private final Integer maxY;
    private final Set<Biome> biomeInclude;
    private final Set<Biome> biomeExclude;
    private final ProtectionSettings protectionSettings;
    private final BiomePreCacheSettings preCacheSettings;
    private final RareBiomeOptimizationSettings rareBiomeOptimizationSettings;
    private final ChunkLoadingSettings chunkLoadingSettings;
    private final boolean enableFallbackToCache;
    private final BiomeSearchSettings biomeSearchSettings;
    private final SafetySettings safetySettings;
    private final SearchPattern searchPattern;
    private final ChunkyIntegrationSettings chunkyIntegrationSettings;

    public RandomTeleportSettings(ConfigurationSection configSection,
                                 String worldName, int centerX, int centerZ, int minimumRadius, int maximumRadius,
                                 int maxAttempts, boolean useWorldBorderRadius, Set<Material> unsafeBlocks,
                                 TeleportMessages messages, ParticleSettings particleSettings,
                                 OnJoinTeleportSettings onJoinTeleportSettings,
                                 CountdownBossBarSettings countdownBossBarSettings,
                                 CountdownParticleSettings countdownParticleSettings,
                                 double teleportCost,
                                 int countdownSeconds, boolean countdownChatMessagesEnabled, boolean debugRejectionLogging,
                                 Integer minY, Integer maxY,
                                 Set<Biome> biomeInclude,
                                 Set<Biome> biomeExclude,
                                 ProtectionSettings protectionSettings,
                                 BiomePreCacheSettings preCacheSettings,
                                 RareBiomeOptimizationSettings rareBiomeOptimizationSettings,
                                 ChunkLoadingSettings chunkLoadingSettings,
                                 boolean enableFallbackToCache,
                                 BiomeSearchSettings biomeSearchSettings,
                                 SafetySettings safetySettings,
                                 SearchPattern searchPattern,
                                 ChunkyIntegrationSettings chunkyIntegrationSettings) {
        this.configSection = configSection;
        this.worldName = worldName;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.minimumRadius = minimumRadius;
        this.maximumRadius = maximumRadius;
        this.maxAttempts = maxAttempts;
        this.useWorldBorderRadius = useWorldBorderRadius;
        this.unsafeBlocks = unsafeBlocks;
        this.messages = messages;
        this.particleSettings = particleSettings;
        this.onJoinTeleportSettings = onJoinTeleportSettings;
        this.countdownBossBarSettings = countdownBossBarSettings;
        this.countdownParticleSettings = countdownParticleSettings;
        this.teleportCost = teleportCost;
        this.countdownSeconds = countdownSeconds;
        this.countdownChatMessagesEnabled = countdownChatMessagesEnabled;
        this.debugRejectionLogging = debugRejectionLogging;
        this.minY = minY;
        this.maxY = maxY;
        this.biomeInclude = biomeInclude != null ? biomeInclude : java.util.Collections.emptySet();
        this.biomeExclude = biomeExclude != null ? biomeExclude : java.util.Collections.emptySet();
        this.protectionSettings = protectionSettings != null ? protectionSettings : new ProtectionSettings(false, java.util.Collections.emptyList());
        this.preCacheSettings = preCacheSettings != null ? preCacheSettings : BiomePreCacheSettings.disabled();
        this.rareBiomeOptimizationSettings = rareBiomeOptimizationSettings != null ? rareBiomeOptimizationSettings : RareBiomeOptimizationSettings.disabled();
        this.chunkLoadingSettings = chunkLoadingSettings != null ? chunkLoadingSettings : ChunkLoadingSettings.defaults();
        this.enableFallbackToCache = enableFallbackToCache;
        this.biomeSearchSettings = biomeSearchSettings != null ? biomeSearchSettings : BiomeSearchSettings.defaults();
        this.safetySettings = safetySettings != null ? safetySettings : SafetySettings.defaults();
        this.searchPattern = searchPattern != null ? searchPattern : SearchPattern.RANDOM;
        this.chunkyIntegrationSettings = chunkyIntegrationSettings != null ? chunkyIntegrationSettings : ChunkyIntegrationSettings.defaults();
    }
        public Integer getMinY() { return minY; }
        public Integer getMaxY() { return maxY; }
    public boolean isDebugRejectionLoggingEnabled() { return debugRejectionLogging; }
    public Set<Biome> getBiomeInclude() { return biomeInclude; }
    public Set<Biome> getBiomeExclude() { return biomeExclude; }
    public ProtectionSettings getProtectionSettings() { return protectionSettings; }
    public BiomePreCacheSettings getPreCacheSettings() { return preCacheSettings; }
    public RareBiomeOptimizationSettings getRareBiomeOptimizationSettings() { return rareBiomeOptimizationSettings; }
    public ChunkLoadingSettings getChunkLoadingSettings() { return chunkLoadingSettings; }
    public boolean isEnableFallbackToCache() { return enableFallbackToCache; }
    public BiomeSearchSettings getBiomeSearchSettings() { return biomeSearchSettings; }
    public SafetySettings getSafetySettings() { return safetySettings; }
    public SearchPattern getSearchPattern() { return searchPattern; }

    public String getWorldName() { return worldName; }
    public int getCenterX() { return centerX; }
    public int getCenterZ() { return centerZ; }
    public int getMinimumRadius() { return minimumRadius; }
    public int getMaximumRadius() { return maximumRadius; }
    public int getMaxAttempts() { return maxAttempts; }
    public boolean useWorldBorderRadius() { return useWorldBorderRadius; }
    public Set<Material> getUnsafeBlocks() { return unsafeBlocks; }
    public TeleportMessages getMessages() { return messages; }
    public ParticleSettings getParticleSettings() { return particleSettings; }
    public OnJoinTeleportSettings getOnJoinTeleportSettings() { return onJoinTeleportSettings; }
    public CountdownBossBarSettings getCountdownBossBarSettings() { return countdownBossBarSettings; }
    public CountdownParticleSettings getCountdownParticleSettings() { return countdownParticleSettings; }

    public double getTeleportCost() { return teleportCost; }

    public ConfigurationSection getConfigSection() { return configSection; }

    public ChunkyIntegrationSettings getChunkyIntegrationSettings() { return chunkyIntegrationSettings; }

    public static RandomTeleportSettings fromConfiguration(ConfigurationSection section, java.util.logging.Logger logger) {
        if (section == null) {
            return new RandomTeleportSettings(null, "world", 0, 0, 100, 1000, 10, false, java.util.Collections.emptySet(),
                TeleportMessages.defaultMessages(),
                new ParticleSettings(false, org.bukkit.Particle.PORTAL, 40, 0.5D, 1.0D, 0.5D, 0.0D, false),
                new OnJoinTeleportSettings(false, false, "", 20L),
                CountdownBossBarSettings.disabled(),
                CountdownParticleSettings.disabled(),
                0.0, 0, true, false, null, null,
                java.util.Collections.<Biome>emptySet(),
                java.util.Collections.<Biome>emptySet(),
                new ProtectionSettings(false, java.util.Collections.emptyList()),
                BiomePreCacheSettings.disabled(),
                RareBiomeOptimizationSettings.disabled(),
                ChunkLoadingSettings.defaults(),
                true,
                BiomeSearchSettings.defaults(),
                SafetySettings.defaults(),
                SearchPattern.RANDOM,
                ChunkyIntegrationSettings.defaults());
        }

        String worldName = section.getString("world", "world");
        int centerX = 0;
        int centerZ = 0;
        if (section.isConfigurationSection("center")) {
            ConfigurationSection center = section.getConfigurationSection("center");
            centerX = center.getInt("x", 0);
            centerZ = center.getInt("z", 0);
        }
        int minRadius = 100;
        int maxRadius = 1000;
        boolean useWorldBorder = false;
        if (section.isConfigurationSection("radius")) {
            ConfigurationSection radius = section.getConfigurationSection("radius");
            minRadius = radius.getInt("min", 100);
            maxRadius = radius.contains("max") ? radius.getInt("max") : 1000;
            useWorldBorder = radius.getBoolean("use-world-border", false);
        }
        int maxAttempts = section.getInt("max-attempts", 10);
        double teleportCost = section.getDouble("cost", 0.0);
        ConfigurationSection countdownSection = section.getConfigurationSection("countdown");
        int countdownSeconds = countdownSection != null
                ? countdownSection.getInt("seconds", section.getInt("countdown-seconds", 0))
                : section.getInt("countdown-seconds", 0);
        boolean countdownChatMessagesEnabled = countdownSection != null
                ? countdownSection.getBoolean("chat-messages", true)
                : true;
        boolean debugRejectionLogging = section.getBoolean("debug-rejection-logging", false);
        Integer minY = section.contains("min-y") ? section.getInt("min-y") : null;
        Integer maxY = section.contains("max-y") ? section.getInt("max-y") : null;

        // Unsafe blocks
        java.util.Set<org.bukkit.Material> unsafeBlocks = new java.util.HashSet<>();
        if (section.isList("unsafe-blocks")) {
            for (Object o : section.getList("unsafe-blocks")) {
                if (o instanceof String s) {
                    try {
                        unsafeBlocks.add(org.bukkit.Material.valueOf(s.toUpperCase(java.util.Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }

        ConfigurationSection biomesSection = section.getConfigurationSection("biomes");
        java.util.Set<Biome> biomeInclude = parseBiomeList(biomesSection, "include");
        java.util.Set<Biome> biomeExclude = parseBiomeList(biomesSection, "exclude");

        // Messages, particles, on-join
        TeleportMessages messages = TeleportMessages.fromConfiguration(section.getConfigurationSection("messages"));
        ParticleSettings particleSettings = ParticleSettings.fromConfiguration(section.getConfigurationSection("particles"),
                logger, ParticleSettings.disabled());
        OnJoinTeleportSettings onJoinTeleportSettings = OnJoinTeleportSettings.fromConfiguration(section.getConfigurationSection("on-join"));
        CountdownBossBarSettings countdownBossBarSettings = CountdownBossBarSettings.fromConfiguration(
                countdownSection != null ? countdownSection.getConfigurationSection("bossbar") : null,
                logger);
        CountdownParticleSettings countdownParticleSettings = CountdownParticleSettings.fromConfiguration(
                countdownSection != null ? countdownSection.getConfigurationSection("particles") : null,
                logger);

        ProtectionSettings protectionSettings = ProtectionSettings.fromConfiguration(section.getConfigurationSection("protection"));
        
        BiomePreCacheSettings preCacheSettings = biomesSection != null 
            ? BiomePreCacheSettings.fromConfiguration(biomesSection.getConfigurationSection("pre-cache"))
            : BiomePreCacheSettings.disabled();
        
        RareBiomeOptimizationSettings rareBiomeSettings = biomesSection != null
            ? RareBiomeOptimizationSettings.fromConfiguration(biomesSection.getConfigurationSection("rare-biome-optimization"))
            : RareBiomeOptimizationSettings.disabled();

        ChunkLoadingSettings chunkLoadingSettings = ChunkLoadingSettings.fromConfiguration(
            section.getConfigurationSection("chunk-loading"), ChunkLoadingSettings.defaults());
        
        BiomeSearchSettings searchSettings = biomesSection != null
            ? BiomeSearchSettings.fromConfiguration(biomesSection.getConfigurationSection("search"), BiomeSearchSettings.defaults())
            : BiomeSearchSettings.defaults();

        SafetySettings safetySettings = SafetySettings.fromConfiguration(section.getConfigurationSection("safety"), SafetySettings.defaults());

        SearchPattern searchPattern = SearchPattern.fromConfig(section.getString("search-pattern"), SearchPattern.RANDOM);

        ChunkyIntegrationSettings chunkyIntegrationSettings = ChunkyIntegrationSettings.fromConfiguration(section.getConfigurationSection("chunky-integration"), ChunkyIntegrationSettings.defaults());
        
        // Parse RTP section for enable_fallback_to_cache
        ConfigurationSection rtpSection = section.getConfigurationSection("rtp");
        boolean enableFallbackToCache = rtpSection != null ? rtpSection.getBoolean("enable_fallback_to_cache", true) : true;

        return new RandomTeleportSettings(section, worldName, centerX, centerZ, minRadius, maxRadius, maxAttempts, useWorldBorder, unsafeBlocks,
            messages, particleSettings, onJoinTeleportSettings, countdownBossBarSettings, countdownParticleSettings,
            teleportCost, countdownSeconds, countdownChatMessagesEnabled, debugRejectionLogging, minY, maxY,
            biomeInclude, biomeExclude,
            protectionSettings,
            preCacheSettings,
            rareBiomeSettings,
            chunkLoadingSettings,
            enableFallbackToCache,
            searchSettings,
            safetySettings,
            searchPattern,
            chunkyIntegrationSettings);
    }

    public static RandomTeleportSettings fromConfiguration(ConfigurationSection section, java.util.logging.Logger logger, RandomTeleportSettings fallback) {
        if (section == null) {
            return fallback != null ? fallback : fromConfiguration(section, logger);
        }

        String worldName = section.getString("world", fallback != null ? fallback.getWorldName() : "world");
        int centerX = fallback != null ? fallback.getCenterX() : 0;
        int centerZ = fallback != null ? fallback.getCenterZ() : 0;
        if (section.isConfigurationSection("center")) {
            ConfigurationSection center = section.getConfigurationSection("center");
            centerX = center.getInt("x", centerX);
            centerZ = center.getInt("z", centerZ);
        }

        int minRadius = fallback != null ? fallback.getMinimumRadius() : 100;
        int maxRadius = fallback != null ? fallback.getMaximumRadius() : 1000;
        boolean useWorldBorder = fallback != null && fallback.useWorldBorderRadius();
        if (section.isConfigurationSection("radius")) {
            ConfigurationSection radius = section.getConfigurationSection("radius");
            minRadius = radius.getInt("min", minRadius);
            maxRadius = radius.contains("max") ? radius.getInt("max") : maxRadius;
            useWorldBorder = radius.getBoolean("use-world-border", useWorldBorder);
        }

        int maxAttempts = section.getInt("max-attempts", fallback != null ? fallback.getMaxAttempts() : 10);
        double teleportCost = section.getDouble("cost", fallback != null ? fallback.getTeleportCost() : 0.0D);
        ConfigurationSection countdownSection = section.getConfigurationSection("countdown");
        int countdownSeconds = countdownSection != null
                ? countdownSection.getInt("seconds", section.getInt("countdown-seconds",
                fallback != null ? fallback.getCountdownSeconds() : 0))
                : section.getInt("countdown-seconds", fallback != null ? fallback.getCountdownSeconds() : 0);
        boolean countdownChatMessagesEnabled = countdownSection != null
                ? countdownSection.getBoolean("chat-messages", fallback != null ? fallback.isCountdownChatMessagesEnabled() : true)
                : (fallback != null ? fallback.isCountdownChatMessagesEnabled() : true);
        boolean debugRejectionLogging = section.getBoolean("debug-rejection-logging",
                fallback != null && fallback.isDebugRejectionLoggingEnabled());
        Integer minY = section.contains("min-y")
                ? Integer.valueOf(section.getInt("min-y"))
                : (fallback != null ? fallback.getMinY() : null);
        Integer maxY = section.contains("max-y")
                ? Integer.valueOf(section.getInt("max-y"))
                : (fallback != null ? fallback.getMaxY() : null);

        java.util.Set<org.bukkit.Material> unsafeBlocks = new java.util.HashSet<>();
        if (section.isList("unsafe-blocks")) {
            for (Object o : section.getList("unsafe-blocks")) {
                if (o instanceof String s) {
                    try {
                        unsafeBlocks.add(org.bukkit.Material.valueOf(s.toUpperCase(java.util.Locale.ROOT)));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } else if (fallback != null) {
            unsafeBlocks.addAll(fallback.getUnsafeBlocks());
        }

        ConfigurationSection biomesSection = section.getConfigurationSection("biomes");
        java.util.Set<Biome> biomeInclude = !isBiomeSectionEmpty(biomesSection)
                ? parseBiomeList(biomesSection, "include")
                : (fallback != null ? fallback.getBiomeInclude() : java.util.Collections.emptySet());
        java.util.Set<Biome> biomeExclude = !isBiomeSectionEmpty(biomesSection)
                ? parseBiomeList(biomesSection, "exclude")
                : (fallback != null ? fallback.getBiomeExclude() : java.util.Collections.emptySet());

        TeleportMessages messages = section.isConfigurationSection("messages")
                ? TeleportMessages.fromConfiguration(section.getConfigurationSection("messages"))
                : (fallback != null ? fallback.getMessages() : TeleportMessages.defaultMessages());
        ParticleSettings particleSettings = ParticleSettings.fromConfiguration(section.getConfigurationSection("particles"),
                logger, fallback != null ? fallback.getParticleSettings() : ParticleSettings.disabled());
        OnJoinTeleportSettings onJoinTeleportSettings = section.isConfigurationSection("on-join")
                ? OnJoinTeleportSettings.fromConfiguration(section.getConfigurationSection("on-join"))
                : (fallback != null ? fallback.getOnJoinTeleportSettings() : OnJoinTeleportSettings.fromConfiguration(null));
        CountdownBossBarSettings countdownBossBarSettings = countdownSection != null
                ? CountdownBossBarSettings.fromConfiguration(countdownSection.getConfigurationSection("bossbar"), logger)
                : (fallback != null ? fallback.getCountdownBossBarSettings() : CountdownBossBarSettings.disabled());
        CountdownParticleSettings countdownParticleSettings = countdownSection != null
                ? CountdownParticleSettings.fromConfiguration(countdownSection.getConfigurationSection("particles"), logger)
                : (fallback != null ? fallback.getCountdownParticleSettings() : CountdownParticleSettings.disabled());

        ProtectionSettings protectionSettings = section.isConfigurationSection("protection")
                ? ProtectionSettings.fromConfiguration(section.getConfigurationSection("protection"),
                fallback != null ? fallback.getProtectionSettings() : null)
                : (fallback != null ? fallback.getProtectionSettings() : new ProtectionSettings(false, java.util.Collections.emptyList()));

        BiomePreCacheSettings preCacheSettings = biomesSection != null && biomesSection.isConfigurationSection("pre-cache")
                ? BiomePreCacheSettings.fromConfiguration(biomesSection.getConfigurationSection("pre-cache"))
                : (fallback != null ? fallback.getPreCacheSettings() : BiomePreCacheSettings.disabled());
        
        RareBiomeOptimizationSettings rareBiomeSettings = biomesSection != null && biomesSection.isConfigurationSection("rare-biome-optimization")
                ? RareBiomeOptimizationSettings.fromConfiguration(biomesSection.getConfigurationSection("rare-biome-optimization"))
                : (fallback != null ? fallback.getRareBiomeOptimizationSettings() : RareBiomeOptimizationSettings.disabled());

        ChunkLoadingSettings chunkLoadingSettings = ChunkLoadingSettings.fromConfiguration(
            section.getConfigurationSection("chunk-loading"),
            fallback != null ? fallback.getChunkLoadingSettings() : ChunkLoadingSettings.defaults());
        
        BiomeSearchSettings searchSettings = biomesSection != null && biomesSection.isConfigurationSection("search")
            ? BiomeSearchSettings.fromConfiguration(biomesSection.getConfigurationSection("search"),
                fallback != null ? fallback.getBiomeSearchSettings() : BiomeSearchSettings.defaults())
            : (fallback != null ? fallback.getBiomeSearchSettings() : BiomeSearchSettings.defaults());

        SafetySettings safetySettings = SafetySettings.fromConfiguration(section.getConfigurationSection("safety"),
            fallback != null ? fallback.getSafetySettings() : SafetySettings.defaults());

        String patternValue = section.getString("search-pattern",
            fallback != null ? fallback.getSearchPattern().getConfigKey() : SearchPattern.RANDOM.getConfigKey());
        SearchPattern searchPattern = SearchPattern.fromConfig(patternValue,
            fallback != null ? fallback.getSearchPattern() : SearchPattern.RANDOM);

        ChunkyIntegrationSettings chunkyIntegrationSettings = ChunkyIntegrationSettings.fromConfiguration(section.getConfigurationSection("chunky-integration"),
            fallback != null ? fallback.getChunkyIntegrationSettings() : ChunkyIntegrationSettings.defaults());
        
        // Parse RTP section for enable_fallback_to_cache
        ConfigurationSection rtpSection = section.getConfigurationSection("rtp");
        boolean enableFallbackToCache = rtpSection != null 
                ? rtpSection.getBoolean("enable_fallback_to_cache", fallback != null ? fallback.isEnableFallbackToCache() : true)
                : (fallback != null ? fallback.isEnableFallbackToCache() : true);

        return new RandomTeleportSettings(section, worldName, centerX, centerZ, minRadius, maxRadius, maxAttempts,
                useWorldBorder, unsafeBlocks, messages, particleSettings, onJoinTeleportSettings, countdownBossBarSettings,
                countdownParticleSettings, teleportCost, countdownSeconds, countdownChatMessagesEnabled, debugRejectionLogging, minY, maxY,
                biomeInclude, biomeExclude,
                protectionSettings,
                preCacheSettings,
                rareBiomeSettings,
                chunkLoadingSettings,
            enableFallbackToCache,
            searchSettings,
            safetySettings,
            searchPattern,
            chunkyIntegrationSettings);
    }
    public int getCountdownSeconds() {
        return countdownSeconds;
    }

    public boolean isCountdownChatMessagesEnabled() {
        return countdownChatMessagesEnabled;
    }

    private static boolean isBiomeSectionEmpty(ConfigurationSection section) {
        return section == null || (!section.isList("include") && !section.isList("exclude"));
    }

    private static java.util.Set<Biome> parseBiomeList(ConfigurationSection section, String key) {
        java.util.Set<Biome> biomes = new java.util.HashSet<>();
        if (section == null || !section.isList(key)) {
            return biomes;
        }
        for (Object o : section.getList(key)) {
            if (o instanceof String s) {
                try {
                    biomes.add(Biome.valueOf(s.toUpperCase(java.util.Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return biomes;
    }

    // Place the TeleportMessages class here, fully inside RandomTeleportSettings
    public static final class TeleportMessages {
        
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

        public TeleportMessages(String teleporting, String teleportSuccess, String teleportFailure,
                                String worldMissing, String joinSearching, String queueQueued,
                                String queueFull, String insufficientFunds,
                                String countdownStart, String countdownTick,
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

        public Component teleporting() {
            return MessageUtil.parseMiniMessage(teleporting);
        }

        public Component teleportSuccess(int x, int z, String world) {
            String processed = teleportSuccess
                .replace("<x>", Integer.toString(x))
                .replace("<z>", Integer.toString(z))
                .replace("<world>", world);
            return MessageUtil.parseMiniMessage(processed);
        }

        public Component teleportFailure() {
            return MessageUtil.parseMiniMessage(teleportFailure);
        }

        public Component teleportFailureBiome() {
            return MessageUtil.parseMiniMessage(teleportFailureBiome);
        }

        public Component worldMissing(String world) {
            String processed = worldMissing.replace("<world>", world);
            return MessageUtil.parseMiniMessage(processed);
        }

        public Component joinSearching() {
            return MessageUtil.parseMiniMessage(joinSearching);
        }

        public Component queued(int position) {
            String processed = queueQueued.replace("<position>", Integer.toString(Math.max(position, 1)));
            return MessageUtil.parseMiniMessage(processed);
        }

        public Component queueFull(int maxSize) {
            String processed = queueFull.replace("<size>", Integer.toString(Math.max(maxSize, 0)));
            return MessageUtil.parseMiniMessage(processed);
        }

        public Component insufficientFunds(double cost) {
            String processed = insufficientFunds.replace("<cost>", String.format(Locale.US, "%.2f", Math.max(cost, 0.0D)));
            return MessageUtil.parseMiniMessage(processed);
        }

        public Component countdownStart(int seconds) {
            String template = countdownStart != null ? countdownStart : "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>";
            String processed = template.replace("<seconds>", Integer.toString(seconds));
            return MessageUtil.parseMiniMessage(processed);
        }

        public Component countdownTick(int seconds) {
            String template = countdownTick != null ? countdownTick : "<gray><seconds>...</gray>";
            String processed = template.replace("<seconds>", Integer.toString(seconds));
            return MessageUtil.parseMiniMessage(processed);
        }
        
        public Component teleportFallbackSuccess(int x, int z) {
            String template = teleportFallbackSuccess != null ? teleportFallbackSuccess :
                "<yellow>No locations found through search. Falling back to a cached random location: (<white><x></white>, <white><z></white>).</yellow>";
            String processed = template.replace("<x>", Integer.toString(x)).replace("<z>", Integer.toString(z));
            return MessageUtil.parseMiniMessage(processed);
        }
        
        public Component teleportFallbackNoCache() {
            String template = teleportFallbackNoCache != null ? teleportFallbackNoCache :
                "<red>No cached locations are available for teleportation. Please wait for locations to be pre-cached.</red>";
            return MessageUtil.parseMiniMessage(template);
        }
        
        public Component teleportFailedSearch() {
            String template = teleportFailedSearch != null ? teleportFailedSearch :
                "<red>Search failed: No valid locations found.</red>";
            return MessageUtil.parseMiniMessage(template);
        }

        public static TeleportMessages fromConfiguration(ConfigurationSection section) {
            if (section == null) {
                return defaultMessages();
            }
            return new TeleportMessages(
                    section.getString("teleporting", "<gray>Searching for a safe location...</gray>"),
                    section.getString("teleport-success",
                            "<green>Teleported to <white><x></white>, <white><z></white> in <white><world></white>.</green>"),
                    section.getString("teleport-failed",
                            "<red>Unable to find a safe location. Please try again.</red>"),
                    section.getString("world-missing",
                            "<red>The configured world '<white><world></white>' is not available.</red>"),
                    section.getString("join-searching",
                            "<gray>Finding you a safe place to explore...</gray>"),
                    section.getString("queue-queued",
                            "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>"),
                    section.getString("queue-full",
                            "<red>The random teleport queue is currently full. Please try again soon.</red>"),
                    section.getString("insufficient-funds",
                            "<red>You need <white><cost></white> to use random teleport.</red>"),
                    section.getString("countdown-start", "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>"),
                    section.getString("countdown-tick", "<gray><seconds>...</gray>"),
                    section.getString("teleport-failed-biome",
                            "<red>No valid biome was found. Please try again.</red>"),
                    section.getString("teleport-fallback-success",
                            "<yellow>No locations found through search. Falling back to a cached random location: (<white><x></white>, <white><z></white>).</yellow>"),
                    section.getString("teleport-fallback-no-cache",
                            "<red>No cached locations are available for teleportation. Please wait for locations to be pre-cached.</red>"),
                    section.getString("teleport-failed-search",
                            "<red>Search failed: No valid locations found.</red>")
            );
        }

        public static TeleportMessages defaultMessages() {
            return new TeleportMessages(
                    "<gray>Searching for a safe location...</gray>",
                    "<green>Teleported to <white><x></white>, <white><z></white> in <white><world></white>.</green>",
                    "<red>Unable to find a safe location. Please try again.</red>",
                    "<red>The configured world '<white><world></white>' is not available.</red>",
                    "<gray>Finding you a safe place to explore...</gray>",
                    "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>",
                    "<red>The random teleport queue is currently full. Please try again soon.</red>",
                    "<red>You need <white><cost></white> to use random teleport.</red>",
                    "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>",
                    "<gray><seconds>...</gray>",
                    "<red>No valid biome was found. Please try again.</red>",
                    "<yellow>No locations found through search. Falling back to a cached random location: (<white><x></white>, <white><z></white>).</yellow>",
                    "<red>No cached locations are available for teleportation. Please wait for locations to be pre-cached.</red>",
                    "<red>Search failed: No valid locations found.</red>"
            );
        }
    }
}
