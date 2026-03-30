package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.config.EzRtpConfiguration;
import com.skyblockexp.ezrtp.config.NetworkConfiguration;
import com.skyblockexp.ezrtp.network.NetworkService;

/**
 * Manages lifecycle of the optional network service integration (BungeeCord relay).
 */
public final class NetworkCoordinator {

    private final EzRtpPlugin plugin;
    private NetworkService networkService;

    public NetworkCoordinator(EzRtpPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(EzRtpConfiguration configuration) {
        shutdown();
        NetworkConfiguration networkConfiguration = configuration.getNetworkConfiguration();
        if (networkConfiguration != null
                && networkConfiguration.isEnabled()
                && networkConfiguration.isLobbyServer()) {
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
            networkService = new NetworkService(plugin, networkConfiguration, plugin.getLogger());
        } else {
            plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
        }
    }

    public void shutdown() {
        if (networkService != null) {
            networkService.shutdown();
            networkService = null;
        }
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
    }

    public NetworkService getNetworkService() {
        return networkService;
    }
}
