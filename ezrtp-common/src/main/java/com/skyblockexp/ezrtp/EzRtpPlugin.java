package com.skyblockexp.ezrtp;

import com.skyblockexp.ezrtp.bootstrap.EzRtpPluginBootstrap;
import com.skyblockexp.ezrtp.message.MessageProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the EzRTP standalone plugin which offers configurable random teleportation
 * via the {@code /rtp} command and optional join handling.
 */
public final class EzRtpPlugin extends JavaPlugin {

    private EzRtpPluginBootstrap bootstrap;

    @Override
    public void onEnable() {
        bootstrap = new EzRtpPluginBootstrap(this);
        bootstrap.enable();
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
        }
    }

    /**
     * Reloads the configuration file and refreshes the teleport service with any updated settings.
     */
    public void reloadPluginConfiguration() {
        if (bootstrap != null) {
            bootstrap.reloadPluginConfiguration();
        }
    }

    /**
     * Gets the message provider for this plugin.
     *
     * @return The message provider
     */
    public MessageProvider getMessageProvider() {
        return bootstrap != null ? bootstrap.getMessageProvider() : null;
    }
}
