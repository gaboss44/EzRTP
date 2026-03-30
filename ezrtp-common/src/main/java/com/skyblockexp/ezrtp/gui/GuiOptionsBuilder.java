package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.NetworkConfiguration;
import com.skyblockexp.ezrtp.gui.GuiIconFactory.CooldownInfo;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Builder for GUI options that populates the inventory with clickable items.
 */
public class GuiOptionsBuilder {

    private final EzRtpConfiguration configuration;
    private final RtpUsageStorage usageStorage;
    private final BiomeLocationCache biomeCache;
    private final NetworkService networkService;
    private final Player player;
    private final GuiIconFactory iconFactory;

    public GuiOptionsBuilder(EzRtpConfiguration configuration,
                             RtpUsageStorage usageStorage,
                             BiomeLocationCache biomeCache,
                             NetworkService networkService,
                             Player player,
                             Logger logger) {
        this.configuration = configuration;
        this.usageStorage = usageStorage;
        this.biomeCache = biomeCache;
        this.networkService = networkService;
        this.player = player;
        this.iconFactory = new GuiIconFactory(logger);
    }

    /**
     * Builds the options map and populates the inventory.
     *
     * @param inventory The inventory to populate
     * @return Map of slot to GuiOption
     */
    public Map<Integer, GuiOption> buildOptions(Inventory inventory) {
        Map<Integer, GuiOption> optionMap = new HashMap<>();
        EzRtpConfiguration.GuiSettings guiSettings = configuration.getGuiSettings();

        addWorldOptions(inventory, optionMap, guiSettings);
        addServerOptions(inventory, optionMap, guiSettings);

        return optionMap;
    }

    private void addWorldOptions(Inventory inventory,
                                 Map<Integer, GuiOption> optionMap,
                                 EzRtpConfiguration.GuiSettings guiSettings) {
        for (EzRtpConfiguration.GuiWorldOption option : guiSettings.getWorldOptions()) {
            if (!option.getPermission().isEmpty() && !player.hasPermission(option.getPermission())) {
                continue;
            }

            boolean bypass = hasBypass(player, option.getSettings().getWorldName());

            CooldownInfo cooldownInfo = bypass ? CooldownInfo.inactive() : evaluateCooldown(option);
            if (cooldownInfo.active() && !configuration.isAllowGuiDuringCooldown()) {
                continue;
            }

            if (!guiSettings.isDisableCacheFiltering()
                    && option.isRequireCacheEnabled()
                    && !meetsCacheRequirements(option)) {
                continue;
            }

            int slot = option.getSlot();
            if (slot < 0 || slot >= inventory.getSize() || optionMap.containsKey(slot)) {
                continue;
            }

                ItemStack icon = iconFactory.buildWorldIcon(player, option, cooldownInfo,
                    configuration.isHumanReadableCooldown());
            inventory.setItem(slot, icon);
            optionMap.put(slot, GuiOption.world(option));
        }
    }

    private CooldownInfo evaluateCooldown(EzRtpConfiguration.GuiWorldOption option) {
        EzRtpConfiguration.RtpLimitSettings limit = configuration.getLimitSettings(
                option.getSettings().getWorldName(), null);
        if (limit == null || limit.cooldownSeconds <= 0) {
            return CooldownInfo.inactive();
        }

        long lastRtp = usageStorage.getLastRtpTime(player.getUniqueId(), option.getSettings().getWorldName());
        if (lastRtp <= 0) {
            return CooldownInfo.inactive();
        }

        long elapsed = System.currentTimeMillis() - lastRtp;
        long remainingMillis = limit.cooldownSeconds * 1000L - elapsed;
        if (remainingMillis <= 0) {
            return CooldownInfo.inactive();
        }

        long remainingSeconds = (long) Math.ceil(remainingMillis / 1000.0);
        return new CooldownInfo(true, remainingSeconds);
    }

    private boolean hasBypass(Player player, String worldName) {
        if (player.isOp()) {
            return true;
        }
        for (String perm : configuration.getBypassPermissions()) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }
        String group = configuration.resolveGroup(player, worldName);
        EzRtpConfiguration.RtpLimitSettings limit = configuration.getLimitSettings(worldName, group);
        return limit != null && limit.disableDailyLimit;
    }

    private boolean meetsCacheRequirements(EzRtpConfiguration.GuiWorldOption option) {
        if (biomeCache == null) {
            return false;
        }

        return biomeCache.getCachedLocationCount(
                org.bukkit.Bukkit.getWorld(option.getSettings().getWorldName()),
                option.getSettings().getBiomeInclude()) >= option.getMinimumCached();
    }

    private void addServerOptions(Inventory inventory,
                                  Map<Integer, GuiOption> optionMap,
                                  EzRtpConfiguration.GuiSettings guiSettings) {
        if (networkService == null || !configuration.getNetworkConfiguration().isLobbyServer()) {
            return;
        }

        for (EzRtpConfiguration.GuiServerOption serverOption : guiSettings.getServerOptions()) {
            NetworkConfiguration.NetworkServer server = serverOption.getServer();

            if (!server.getPermission().isEmpty() && !player.hasPermission(server.getPermission())) {
                continue;
            }

            int slot = server.getSlot();
            if (slot < 0 || slot >= inventory.getSize() || optionMap.containsKey(slot)) {
                continue;
            }

            NetworkConfiguration.ServerStatusSnapshot status = networkService.getStatus(server);
            if (server.hideWhenOffline() && status.isOffline()) {
                continue;
            }

            ItemStack icon = iconFactory.buildServerIcon(serverOption, status, networkService);
            inventory.setItem(slot, icon);
            optionMap.put(slot, GuiOption.server(serverOption));
        }
    }
}
