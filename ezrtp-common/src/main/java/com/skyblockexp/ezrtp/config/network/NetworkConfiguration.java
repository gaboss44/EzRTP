package com.skyblockexp.ezrtp.config.network;

import net.kyori.adventure.text.Component;
import com.skyblockexp.ezrtp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import com.skyblockexp.ezrtp.util.ItemFlagUtil;

/**
 * Configuration model describing how EzRTP should behave when connected to a proxy network.
 */
public final class NetworkConfiguration {

    

    private final boolean enabled;
    private final boolean lobbyServer;
    private final long pingIntervalTicks;
    private final int pingTimeoutMillis;
    private final List<NetworkServer> servers;

    private NetworkConfiguration(boolean enabled, boolean lobbyServer, long pingIntervalTicks,
                                 int pingTimeoutMillis, List<NetworkServer> servers) {
        this.enabled = enabled;
        this.lobbyServer = lobbyServer;
        this.pingIntervalTicks = pingIntervalTicks;
        this.pingTimeoutMillis = pingTimeoutMillis;
        this.servers = servers;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLobbyServer() {
        return lobbyServer;
    }

    public long getPingIntervalTicks() {
        return pingIntervalTicks;
    }

    public int getPingTimeoutMillis() {
        return pingTimeoutMillis;
    }

    public List<NetworkServer> getServers() {
        return servers;
    }

    public static NetworkConfiguration disabled() {
        return new NetworkConfiguration(false, false, 200L, 1500, Collections.emptyList());
    }

    public static NetworkConfiguration fromConfiguration(ConfigurationSection section, Logger logger) {
        if (section == null) {
            return disabled();
        }

        boolean enabled = section.getBoolean("enabled", false);
        boolean lobbyServer = section.getBoolean("lobby", false);
        long pingInterval = Math.max(20L, section.getLong("ping-interval-ticks", 200L));
        int pingTimeout = Math.max(250, section.getInt("ping-timeout-millis", 1500));

        List<NetworkServer> servers = parseServers(section.getConfigurationSection("servers"), logger);

        return new NetworkConfiguration(enabled, lobbyServer, pingInterval, pingTimeout,
                Collections.unmodifiableList(servers));
    }

    private static List<NetworkServer> parseServers(ConfigurationSection section, Logger logger) {
        if (section == null) {
            return Collections.emptyList();
        }
        List<NetworkServer> servers = new ArrayList<>();
        int nextSlot = 0;
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }

            String bungeeName = entry.getString("bungee-server", key);
            if (bungeeName == null || bungeeName.isBlank()) {
                logger.warning(String.format("Network server '%s' is missing a valid 'bungee-server' identifier.", key));
                continue;
            }

            String host = entry.getString("host", "127.0.0.1");
            int port = entry.getInt("port", 25565);
            if (port <= 0 || port > 65535) {
                logger.warning(String.format("Network server '%s' specifies an invalid port '%d'.", key, port));
                continue;
            }

            int slot;
            if (entry.contains("slot")) {
                slot = entry.getInt("slot");
            } else {
                slot = nextSlot;
            }
            nextSlot = Math.min(53, slot + 1);

            String permission = entry.getString("permission", "");
            String displayName = entry.getString("display-name", key);
            boolean hideWhenOffline = entry.getBoolean("hide-when-offline", false);
            boolean allowWhenOffline = entry.getBoolean("allow-when-offline", false);

                String connectMessageRaw = entry.getString("connect-message",
                    "<gray>Connecting you to <white><server></white>...</gray>");
                Component connectMessage = Component.empty();
                if (connectMessageRaw != null && !connectMessageRaw.isBlank()) {
                String replaced = connectMessageRaw.replace("<server>", displayName);
                connectMessage = MessageUtil.parseMiniMessage(replaced);
                }

                String offlineMessageRaw = entry.getString("offline-message",
                    "<red><server></red> is currently unavailable.");
                Component offlineMessage = Component.empty();
                if (offlineMessageRaw != null && !offlineMessageRaw.isBlank()) {
                String replaced = offlineMessageRaw.replace("<server>", displayName);
                offlineMessage = MessageUtil.parseMiniMessage(replaced);
                }

            IconTemplate iconTemplate = IconTemplate.fromConfiguration(entry.getConfigurationSection("icon"),
                    displayName);
            if (iconTemplate == null) {
                logger.warning(String.format("Network server '%s' has an invalid icon configuration.", key));
                continue;
            }

            NetworkServer server = new NetworkServer(key, bungeeName, host, port, permission == null ? "" : permission,
                    slot, displayName, hideWhenOffline, allowWhenOffline,
                    connectMessage, offlineMessage, iconTemplate);
            servers.add(server);
        }
        return servers;
    }

    /**
     * Represents a configured server that can be selected from the EzRTP GUI while in a lobby.
     */
    public static final class NetworkServer {

        private final String id;
        private final String bungeeServer;
        private final String host;
        private final int port;
        private final String permission;
        private final int slot;
        private final String displayName;
        private final boolean hideWhenOffline;
        private final boolean allowWhenOffline;
        private final Component connectMessage;
        private final Component offlineMessage;
        private final IconTemplate iconTemplate;

        private NetworkServer(String id, String bungeeServer, String host, int port, String permission, int slot,
                              String displayName, boolean hideWhenOffline, boolean allowWhenOffline,
                              Component connectMessage, Component offlineMessage, IconTemplate iconTemplate) {
            this.id = id;
            this.bungeeServer = bungeeServer;
            this.host = host;
            this.port = port;
            this.permission = permission;
            this.slot = slot;
            this.displayName = displayName;
            this.hideWhenOffline = hideWhenOffline;
            this.allowWhenOffline = allowWhenOffline;
            this.connectMessage = connectMessage;
            this.offlineMessage = offlineMessage;
            this.iconTemplate = iconTemplate;
        }

        public String getId() {
            return id;
        }

        public String getBungeeServer() {
            return bungeeServer;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getPermission() {
            return permission;
        }

        public int getSlot() {
            return slot;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean hideWhenOffline() {
            return hideWhenOffline;
        }

        public boolean allowWhenOffline() {
            return allowWhenOffline;
        }

        public Component connectMessage() {
            return connectMessage;
        }

        public Component offlineMessage() {
            return offlineMessage;
        }

        public IconTemplate getIconTemplate() {
            return iconTemplate;
        }
    }

    /**
     * Template used to construct inventory icons with dynamic status placeholders.
     */
    public static final class IconTemplate {

        private final Material material;
        private final int amount;
        private final Integer customModelData;
        private final String displayName;
        private final List<String> loreLines;

        private IconTemplate(Material material, int amount, Integer customModelData,
                             String displayName, List<String> loreLines) {
            this.material = material;
            this.amount = amount;
            this.customModelData = customModelData;
            this.displayName = displayName;
            this.loreLines = loreLines;
        }

        public ItemStack createIcon(ServerStatusSnapshot status, String displayName) {
            ItemStack itemStack = new ItemStack(material, amount);
            ItemMeta meta = itemStack.getItemMeta();

            String providedDisplayName = displayName != null ? displayName : "";
            String templateDisplayName = this.displayName != null ? this.displayName : "";

                // Build a simple string-replacement resolver for placeholders

            boolean hasTemplateName = !templateDisplayName.isBlank();
            boolean hasProvidedName = !providedDisplayName.isBlank();
            if (hasTemplateName || hasProvidedName) {
                String name = hasTemplateName ? templateDisplayName : providedDisplayName;
                String resolvedName = name
                        .replace("<server>", providedDisplayName)
                        .replace("<ping>", status.pingDisplay())
                        .replace("<status>", status.statusMiniMessage());
                Component component;
                // Prefer MiniMessage when available to correctly parse tags like <gold>...</gold>
                component = MessageUtil.parseMiniMessage(resolvedName);
                com.skyblockexp.ezrtp.util.compat.ItemMetaCompat.setDisplayName(meta, component);
            }
            if (!loreLines.isEmpty()) {
                List<Component> lore = new ArrayList<>(loreLines.size());
                for (String line : loreLines) {
                    String resolvedLine = line
                            .replace("<server>", providedDisplayName)
                            .replace("<ping>", status.pingDisplay())
                            .replace("<status>", status.statusMiniMessage());
                    lore.add(MessageUtil.parseMiniMessage(resolvedLine));
                }
                com.skyblockexp.ezrtp.util.compat.ItemMetaCompat.setLore(meta, lore);
            }
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            ItemFlagUtil.applyStandardHideFlags(meta);
            ItemFlagUtil.setItemMetaCompatibly(itemStack, meta);
            return itemStack;
        }

        private static IconTemplate fromConfiguration(ConfigurationSection section, String displayName) {
            if (section == null) {
                return null;
            }
            String materialKey = section.getString("material", "ENDER_PEARL");
            Material material = parseMaterial(materialKey, Material.ENDER_PEARL);
            int amount = Math.max(1, section.getInt("amount", 1));
            Integer customModelData = section.contains("custom-model-data")
                    ? section.getInt("custom-model-data") : null;
            String name = section.getString("name", "<green><server></green>");
            List<String> lore = section.isList("lore") ? section.getStringList("lore") : Collections.emptyList();
            return new IconTemplate(material, amount, customModelData, name, lore);
        }

        private static Material parseMaterial(String key, Material defaultMaterial) {
            if (key == null || key.isBlank()) {
                return defaultMaterial;
            }
            Material material = Material.matchMaterial(key.trim().toUpperCase(Locale.ROOT));
            return material != null ? material : defaultMaterial;
        }
    }

    /**
     * Immutable snapshot describing the connectivity status of a configured server.
     */
    public static final class ServerStatusSnapshot {

        public enum Connectivity {
            ONLINE,
            OFFLINE,
            UNKNOWN
        }

        private final Connectivity connectivity;
        private final long pingMillis;

        private ServerStatusSnapshot(Connectivity connectivity, long pingMillis) {
            this.connectivity = connectivity;
            this.pingMillis = pingMillis;
        }

        public static ServerStatusSnapshot online(long pingMillis) {
            return new ServerStatusSnapshot(Connectivity.ONLINE, Math.max(1L, pingMillis));
        }

        public static ServerStatusSnapshot offline() {
            return new ServerStatusSnapshot(Connectivity.OFFLINE, -1L);
        }

        public static ServerStatusSnapshot unknown() {
            return new ServerStatusSnapshot(Connectivity.UNKNOWN, -1L);
        }

        public Connectivity getConnectivity() {
            return connectivity;
        }

        public String pingDisplay() {
            return connectivity == Connectivity.ONLINE ? Long.toString(pingMillis) : "N/A";
        }

        public String statusMiniMessage() {
            return switch (connectivity) {
                case ONLINE -> "<green>Online</green>";
                case OFFLINE -> "<red>Offline</red>";
                case UNKNOWN -> "<gray>Unknown</gray>";
            };
        }

        public boolean isOnline() {
            return connectivity == Connectivity.ONLINE;
        }

        public boolean isOffline() {
            return connectivity == Connectivity.OFFLINE;
        }

        public boolean isUnknown() {
            return connectivity == Connectivity.UNKNOWN;
        }
    }
}
