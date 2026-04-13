package com.skyblockexp.ezrtp.config.gui;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single selectable world entry displayed in the optional teleport GUI.
 *
 * <p>Instances are created exclusively by {@link GuiSettings} during configuration parsing and
 * are immutable after construction.
 */
public final class GuiWorldOption {

    private final RandomTeleportSettings settings;
    private final ItemStack iconTemplate;
    private final int slot;
    private final String permission;
    private final boolean requireCacheEnabled;
    private final int minimumCached;
    private final String rawDisplayName;
    private final List<String> rawLore;

    /**
     * Package-private constructor — instances are created by {@link GuiSettings} during parsing.
     *
     * @param settings           teleport settings for this world option
     * @param iconTemplate       pre-built icon {@link ItemStack} template (cloned on each access)
     * @param slot               GUI inventory slot (0-based)
     * @param permission         required permission node, or empty string for no restriction
     * @param requireCacheEnabled whether a minimum cache fill must be met before the option is shown
     * @param minimumCached      minimum number of cached locations required when cache gating is enabled
     * @param rawDisplayName     raw MiniMessage string used as the icon display name (for placeholder resolution)
     * @param rawLore            raw MiniMessage strings for each lore line (for placeholder resolution)
     */
    GuiWorldOption(
            RandomTeleportSettings settings,
            ItemStack iconTemplate,
            int slot,
            String permission,
            boolean requireCacheEnabled,
            int minimumCached,
            String rawDisplayName,
            List<String> rawLore) {
        this.settings = settings;
        this.iconTemplate = iconTemplate;
        this.slot = slot;
        this.permission = permission == null ? "" : permission;
        this.requireCacheEnabled = requireCacheEnabled;
        this.minimumCached = minimumCached;
        this.rawDisplayName = rawDisplayName;
        this.rawLore =
                rawLore != null
                        ? Collections.unmodifiableList(new ArrayList<>(rawLore))
                        : Collections.emptyList();
    }

    /**
     * Returns the {@link RandomTeleportSettings} associated with this GUI option.
     *
     * @return settings for this world entry
     */
    public RandomTeleportSettings getSettings() {
        return settings;
    }

    /**
     * Returns a clone of the pre-built icon template.
     *
     * @return a fresh copy of the icon {@link ItemStack}
     */
    public ItemStack createIcon() {
        return iconTemplate.clone();
    }

    /**
     * Returns the GUI inventory slot (0-based) where this option should be placed.
     *
     * @return inventory slot index
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Returns the permission node required to see and use this option.
     *
     * @return permission string, or empty string if no restriction applies
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Returns {@code true} if a minimum cache fill must be met before showing this option.
     *
     * @return {@code true} when cache gating is active for this option
     */
    public boolean isRequireCacheEnabled() {
        return requireCacheEnabled;
    }

    /**
     * Returns the minimum number of cached locations required when cache gating is active.
     *
     * @return minimum cached location count
     */
    public int getMinimumCached() {
        return minimumCached;
    }

    /**
     * Returns the raw MiniMessage display name string used for placeholder resolution at runtime.
     *
     * @return raw display name template
     */
    public String getRawDisplayName() {
        return rawDisplayName;
    }

    /**
     * Returns the raw MiniMessage lore lines used for placeholder resolution at runtime.
     *
     * @return immutable list of raw lore line templates
     */
    public List<String> getRawLore() {
        return rawLore;
    }
}
