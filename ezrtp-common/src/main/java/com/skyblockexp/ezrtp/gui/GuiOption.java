package com.skyblockexp.ezrtp.gui;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;

/**
 * Represents a GUI option that can be either a world option or a server option.
 */
public record GuiOption(EzRtpConfiguration.GuiWorldOption worldOption,
                        EzRtpConfiguration.GuiServerOption serverOption) {

    public static GuiOption world(EzRtpConfiguration.GuiWorldOption option) {
        return new GuiOption(option, null);
    }

    public static GuiOption server(EzRtpConfiguration.GuiServerOption option) {
        return new GuiOption(null, option);
    }

    public boolean isWorldOption() {
        return worldOption != null;
    }
}