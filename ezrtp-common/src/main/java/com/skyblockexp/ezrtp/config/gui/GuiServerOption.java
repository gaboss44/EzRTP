package com.skyblockexp.ezrtp.config.gui;
import com.skyblockexp.ezrtp.config.network.NetworkConfiguration;

/**
 * Represents a selectable network-server entry displayed in the optional teleport GUI
 * when the plugin operates behind a BungeeCord/Velocity proxy.
 *
 * <p>Instances are created exclusively by {@link GuiSettings} during configuration parsing and
 * are immutable after construction.
 */
public final class GuiServerOption {

    private final NetworkConfiguration.NetworkServer server;

    /**
     * Package-private constructor — instances are created by {@link GuiSettings} during parsing.
     *
     * @param server the underlying network-server definition
     */
    GuiServerOption(NetworkConfiguration.NetworkServer server) {
        this.server = server;
    }

    /**
     * Returns the underlying {@link NetworkConfiguration.NetworkServer} definition for this
     * GUI entry.
     *
     * @return the server definition
     */
    public NetworkConfiguration.NetworkServer getServer() {
        return server;
    }
}
