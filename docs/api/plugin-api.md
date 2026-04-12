# EzRTP Plugin API

Purpose: Expose a small helper API for other plugins to trigger random teleports (RTP) programmatically.

Usage:

- Simple RTP: Calls the plugin's default RTP flow (uses configured settings, costs, cooldowns, etc.)

Example:

```java
import com.skyblockexp.ezrtp.api.EzRtpAPI;

// RTP with default settings
EzRtpAPI.rtpPlayer(player);
```

- RTP with custom settings: Use an instance of `RandomTeleportSettings`.

Example:

```java
import com.skyblockexp.ezrtp.api.EzRtpAPI;
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;

RandomTeleportSettings settings = ...; // build or obtain from config
EzRtpAPI.rtpPlayer(player, settings, success -> {
    if (success) {
        // do something on success
    }
});
```

Notes:

- If EzRTP is not present or not enabled, the API will log a warning and the call will be ignored.
- The API helpers default to using `TeleportReason.COMMAND` for cost/cooldown resolution.

Where to find the code:

- API helper: [ezrtp-common/src/main/java/com/skyblockexp/ezrtp/api/EzRtpAPI.java](ezrtp-common/src/main/java/com/skyblockexp/ezrtp/api/EzRtpAPI.java#L1-L200)
- Plugin accessor: [ezrtp-common/src/main/java/com/skyblockexp/ezrtp/EzRtpPlugin.java](ezrtp-common/src/main/java/com/skyblockexp/ezrtp/EzRtpPlugin.java#L1-L200)

This repository includes a lightweight API helper designed for ease-of-use by other plugins. It does not introduce a formal service registry; instead it locates the running EzRTP plugin via the server plugin manager and delegates to the running `RandomTeleportService`.

If you need a more advanced integration (for example, to run teleports using a specific teleport reason, or to integrate with economy providers in a custom way), obtain the `RandomTeleportService` directly:

```java
import com.skyblockexp.ezrtp.api.EzRtpAPI;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;

RandomTeleportService service = EzRtpAPI.getTeleportService();
if (service != null) {
    // call service.teleportPlayer(...) variants directly
}
```

The API is intentionally minimal to avoid tight coupling; it is safe to call from other plugins during runtime, but callers should handle the case where EzRTP is not present or not yet enabled.
