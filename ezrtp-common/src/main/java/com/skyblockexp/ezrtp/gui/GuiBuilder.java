package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.network.NetworkService;
import com.skyblockexp.ezrtp.platform.PlatformGuiBridgeRegistry;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Builder class responsible for creating and populating RTP GUI inventories.
 */
public class GuiBuilder {

    private final EzRtpConfiguration configuration;
    private final RtpUsageStorage usageStorage;
    private final BiomeLocationCache biomeCache;
    private final NetworkService networkService;
    private final Logger logger;

    private static final org.bukkit.inventory.InventoryHolder DUMMY_HOLDER = new org.bukkit.inventory.InventoryHolder() {
        @Override
        public org.bukkit.inventory.Inventory getInventory() {
            return null;
        }
    };

    public GuiBuilder(EzRtpConfiguration configuration,
                      RtpUsageStorage usageStorage,
                      BiomeLocationCache biomeCache,
                      NetworkService networkService,
                      Logger logger) {
        this.configuration = configuration;
        this.usageStorage = usageStorage;
        this.biomeCache = biomeCache;
        this.networkService = networkService;
        this.logger = logger;
    }

    /**
     * Builds a GUI inventory for the given player.
     *
     * @param player The player to build the GUI for
     * @return A GuiBuildResult containing the inventory and option map
     */
    public GuiBuildResult buildGui(Player player) {
        EzRtpConfiguration.GuiSettings guiSettings = configuration.getGuiSettings();

        Inventory inventory = createInventory(guiSettings);

        GuiOptionsBuilder optionsBuilder = new GuiOptionsBuilder(
            configuration, usageStorage, biomeCache, networkService, player, logger
        );

        Map<Integer, GuiOption> optionMap = optionsBuilder.buildOptions(inventory);

        return new GuiBuildResult(inventory, optionMap, guiSettings);
    }

    private Inventory createInventory(EzRtpConfiguration.GuiSettings guiSettings) {
        return PlatformGuiBridgeRegistry.get().createInventory(DUMMY_HOLDER, guiSettings.getSize(), guiSettings.getTitle());
    }

    /**
     * Result of building a GUI inventory.
     */
    public static class GuiBuildResult {
        private final Inventory inventory;
        private final Map<Integer, GuiOption> optionMap;
        private final EzRtpConfiguration.GuiSettings settings;

        public GuiBuildResult(Inventory inventory, Map<Integer, GuiOption> optionMap,
                             EzRtpConfiguration.GuiSettings settings) {
            this.inventory = inventory;
            this.optionMap = optionMap;
            this.settings = settings;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public Map<Integer, GuiOption> getOptionMap() {
            return optionMap;
        }

        public EzRtpConfiguration.GuiSettings getSettings() {
            return settings;
        }
    }
}
