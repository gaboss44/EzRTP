package com.skyblockexp.ezrtp.config.safety;

public final class CausesSettings {
    private final boolean track;

    public CausesSettings(boolean track) {
        this.track = track;
    }

    public boolean isTrack() {
        return track;
    }
}
