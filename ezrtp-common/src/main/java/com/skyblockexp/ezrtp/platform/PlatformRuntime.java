package com.skyblockexp.ezrtp.platform;

/**
 * Runtime-level platform hooks exposed by platform modules.
 */
public interface PlatformRuntime {

    PlatformRuntimeCapabilities capabilities();

    default boolean isPaper() {
        return capabilities().paperApi();
    }

    default boolean isPurpur() {
        return capabilities().purpurApi();
    }

    default boolean isRegionizedRuntime() {
        return capabilities().regionizedRuntime();
    }

    PlatformWorldAccess worldAccess();

    PlatformScheduler scheduler();
}
