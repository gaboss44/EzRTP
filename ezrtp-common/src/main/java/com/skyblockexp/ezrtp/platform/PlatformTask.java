package com.skyblockexp.ezrtp.platform;

/**
 * Lightweight task handle abstraction used by platform schedulers.
 */
@FunctionalInterface
public interface PlatformTask {

    void cancel();
}
