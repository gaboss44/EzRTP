package com.skyblockexp.ezrtp.config.safety;

public final class MonitoringSettings {
    private final boolean enabled;

    public MonitoringSettings(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
