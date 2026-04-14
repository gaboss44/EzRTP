package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.gui.GuiServerOption;
import com.skyblockexp.ezrtp.config.gui.GuiSettings;
import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;
import com.skyblockexp.ezrtp.config.teleport.RtpLimitSettings;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;
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
        GuiSettings guiSettings = configuration.getGuiSettings();

        addWorldOptions(inventory, optionMap, guiSettings);
        addServerOptions(inventory, optionMap, guiSettings);

        return optionMap;
    }

    private void addWorldOptions(Inventory inventory,
                                 Map<Integer, GuiOption> optionMap,
                                 GuiSettings guiSettings) {
        for (GuiWorldOption option : guiSettings.getWorldOptions()) {
            if (!option.getPermission().isEmpty() && !player.hasPermission(option.getPermission())) {
                continue;
            }

            // Resolve "auto" to the player's current world for permission and cooldown checks
            String worldForChecks =
                    option.getSettings().isAutoWorld()
                            ? player.getWorld().getName()
                            : option.getSettings().getWorldName();
            boolean bypass = hasBypass(player, worldForChecks);

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

    private CooldownInfo evaluateCooldown(GuiWorldOption option) {
        // Resolve "auto" to the player's current world so storage lookups use the real world key
        String worldName =
                option.getSettings().isAutoWorld()
                        ? player.getWorld().getName()
                        : option.getSettings().getWorldName();
        RtpLimitSettings limit = configuration.getLimitSettings(worldName, null);
        if (limit == null || limit.getCooldownSeconds() <= 0) {
            return CooldownInfo.inactive();
        }

        long lastRtp = usageStorage.getLastRtpTime(player.getUniqueId(), worldName);
        if (lastRtp <= 0) {
            return CooldownInfo.inactive();
        }

        long elapsed = System.currentTimeMillis() - lastRtp;
        long remainingMillis = limit.getCooldownSeconds() * 1000L - elapsed;
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
        RtpLimitSettings limit = configuration.getLimitSettings(worldName, group);
        return limit != null && limit.isDisableDailyLimit();
    }

    private boolean meetsCacheRequirements(GuiWorldOption option) {
        if (biomeCache == null) {
            return false;
        }

        // Resolve "auto" to the player's current world for cache lookup
        String worldName =
                option.getSettings().isAutoWorld()
                        ? player.getWorld().getName()
                        : option.getSettings().getWorldName();
        return biomeCache.getCachedLocationCount(
                org.bukkit.Bukkit.getWorld(worldName),
                option.getSettings().getBiomeInclude()) >= option.getMinimumCached();
    }

    private void addServerOptions(Inventory inventory,
                                  Map<Integer, GuiOption> optionMap,
                                  GuiSettings guiSettings) {
        if (networkService == null || !configuration.getNetworkConfiguration().isLobbyServer()) {
            return;
        }

        for (GuiServerOption serverOption : guiSettings.getServerOptions()) {
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
