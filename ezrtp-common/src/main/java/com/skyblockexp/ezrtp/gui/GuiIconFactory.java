package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.NetworkConfiguration;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridge;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry;
import com.skyblockexp.ezrtp.util.ItemFlagUtil;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Centralised factory responsible for composing GUI icons with consistent placeholder handling.
 */
public final class GuiIconFactory {

    private final Logger logger;

    public GuiIconFactory(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public ItemStack buildWorldIcon(Player player,
                                    EzRtpConfiguration.GuiWorldOption option,
                                    CooldownInfo cooldownInfo,
                                    boolean humanReadableCooldown) {
        ItemStack icon = safeClone(option.createIcon());
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }

        PlatformGuiBridge guiBridge = PlatformGuiBridgeRegistry.get();

        String rawDisplayName = option.getRawDisplayName();
        if (rawDisplayName != null && !rawDisplayName.isBlank()) {
            Component displayName = parseLine(rawDisplayName, player);
            if (displayName != null && !Component.empty().equals(displayName)) {
                guiBridge.setDisplayName(meta, displayName);
            }
        }

        List<Component> lore = new ArrayList<>();
        if (cooldownInfo != null && cooldownInfo.active()) {
            lore.add(buildCooldownComponent(cooldownInfo.remainingSeconds(), humanReadableCooldown));
        }
        for (String raw : option.getRawLore()) {
            Component line = parseLine(raw, player);
            if (line != null) {
                lore.add(line);
            }
        }

        if (!lore.isEmpty()) {
            guiBridge.setLore(meta, lore);
        }
        ItemFlagUtil.setItemMetaCompatibly(icon, meta);
        guiBridge.applyItemMeta(icon, meta);
        return icon;
    }

    public ItemStack buildServerIcon(EzRtpConfiguration.GuiServerOption serverOption,
                                     NetworkConfiguration.ServerStatusSnapshot status,
                                     NetworkService networkService) {
        NetworkConfiguration.NetworkServer server = serverOption.getServer();
        NetworkConfiguration.ServerStatusSnapshot safeStatus =
                status != null ? status : NetworkConfiguration.ServerStatusSnapshot.unknown();

        ItemStack icon = networkService != null
                ? safeClone(networkService.createIcon(server))
                : safeClone(server.getIconTemplate().createIcon(safeStatus, server.getDisplayName()));

        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }

        PlatformGuiBridge guiBridge = PlatformGuiBridgeRegistry.get();

        List<Component> lore = readLore(meta);
        Component badge = buildStatusBadge(server, safeStatus);
        if (badge != null) {
            lore.add(0, badge);
        }
        guiBridge.setLore(meta, lore);
        ItemFlagUtil.setItemMetaCompatibly(icon, meta);
        guiBridge.applyItemMeta(icon, meta);
        return icon;
    }

    private ItemStack safeClone(ItemStack original) {
        if (original != null) {
            return original.clone();
        }
        return new ItemStack(Material.BARRIER);
    }

    private Component parseLine(String raw, Player player) {
        if (raw == null || raw.isBlank()) {
            return Component.empty();
        }
        String resolved = PlaceholderUtil.resolvePlaceholders(player, raw, logger);
        if (resolved == null || resolved.isBlank()) {
            return Component.empty();
        }
        return MessageUtil.parseMiniMessage(resolved);
    }

    private Component buildCooldownComponent(long remainingSeconds, boolean humanReadable) {
        long safeSeconds = Math.max(0L, remainingSeconds);
        String text = humanReadable
                ? PlaceholderUtil.formatTime((int) Math.min(Integer.MAX_VALUE, safeSeconds))
                : safeSeconds + "s";
        return MessageUtil.parseMiniMessage("<red>⏰ On cooldown: " + text + "</red>");
    }

    private Component buildStatusBadge(NetworkConfiguration.NetworkServer server,
                                       NetworkConfiguration.ServerStatusSnapshot status) {
        if (status.isOffline()) {
            Component offlineMessage = server.offlineMessage();
            if (offlineMessage != null && !offlineMessage.equals(Component.empty())) {
                return offlineMessage;
            }
            return MessageUtil.parseMiniMessage("<red>Currently offline</red>");
        }
        if (status.isUnknown()) {
            return MessageUtil.parseMiniMessage("<yellow>Checking status...</yellow>");
        }
        return MessageUtil.parseMiniMessage("<gray>Latency: <green>" + status.pingDisplay() + " ms</green></gray>");
    }

    private List<Component> readLore(ItemMeta meta) {
        try {
            List<Component> lore = meta.lore();
            if (lore != null) {
                return new ArrayList<>(lore);
            }
        } catch (NoSuchMethodError | NoClassDefFoundError ignored) {
            // fallback below
        }

        List<String> legacyLore = meta.getLore();
        if (legacyLore == null || legacyLore.isEmpty()) {
            return new ArrayList<>();
        }

        List<Component> components = new ArrayList<>(legacyLore.size());
        for (String line : legacyLore) {
            components.add(MessageUtil.parseMiniMessage(line));
        }
        return components;
    }

    public record CooldownInfo(boolean active, long remainingSeconds) {
        public static CooldownInfo inactive() {
            return new CooldownInfo(false, 0L);
        }
    }
}
