package com.skyblockexp.ezrtp.protection;

import org.bukkit.Location;

public interface ProtectionProvider {
    String getId();

    boolean isAvailable();

    boolean isLocationProtected(Location location);
}
