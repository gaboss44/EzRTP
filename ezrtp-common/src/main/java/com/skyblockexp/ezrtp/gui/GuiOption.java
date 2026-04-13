package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.gui.GuiServerOption;
import com.skyblockexp.ezrtp.config.gui.GuiWorldOption;

/**
 * Represents a GUI option that can be either a world option or a server option.
 */
public record GuiOption(GuiWorldOption worldOption,
                        GuiServerOption serverOption) {

    public static GuiOption world(GuiWorldOption option) {
        return new GuiOption(option, null);
    }

    public static GuiOption server(GuiServerOption option) {
        return new GuiOption(null, option);
    }

    public boolean isWorldOption() {
        return worldOption != null;
    }
}