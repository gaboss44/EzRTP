package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;

/**
 * Classpath-based detector for runtime capabilities.
 */
public final class PlatformRuntimeCapabilitiesDetector {

    private PlatformRuntimeCapabilitiesDetector() {}

    public static PlatformRuntimeCapabilities detect(Plugin plugin) {
        ClassLoader classLoader = plugin != null ? plugin.getClass().getClassLoader() : PlatformRuntimeCapabilitiesDetector.class.getClassLoader();
        return detect(classLoader);
    }

    public static PlatformRuntimeCapabilities detect(ClassLoader classLoader) {
        boolean purpurApi = hasClass("org.purpurmc.purpur.PurpurConfig", classLoader)
                || hasClass("org.purpurmc.purpur.PurpurConfigServer", classLoader);
        boolean regionizedRuntime = hasClass("io.papermc.paper.threadedregions.RegionizedServer", classLoader);
        boolean paperApi = purpurApi
                || hasClass("com.destroystokyo.paper.PaperConfig", classLoader)
                || hasClass("io.papermc.paper.configuration.Configuration", classLoader)
                || regionizedRuntime;
        return new PlatformRuntimeCapabilities(paperApi, purpurApi, regionizedRuntime);
    }

    /**
     * Returns {@code true} when the server is Paper (or a Paper fork) running Minecraft 1.21 or
     * later. On these servers, {@code World.getChunkAtAsync()} is truly non-blocking and the
     * tick-based throttle queue is counterproductive.
     *
     * <p>The method guards against any runtime failure — if the Paper classpath indicator is
     * absent or the version string cannot be parsed, it returns {@code false}.
     *
     * @param classLoader the class loader to probe for the Paper API indicator
     */
    public static boolean isPaper121PlusAsync(ClassLoader classLoader) {
        // Confirm Paper API is present first.
        boolean paperPresent = hasClass("com.destroystokyo.paper.PaperConfig", classLoader)
                || hasClass("io.papermc.paper.configuration.Configuration", classLoader)
                || hasClass("org.purpurmc.purpur.PurpurConfig", classLoader)
                || hasClass("org.purpurmc.purpur.PurpurConfigServer", classLoader)
                || hasClass("io.papermc.paper.threadedregions.RegionizedServer", classLoader);
        if (!paperPresent) {
            return false;
        }
        try {
            String versionString = org.bukkit.Bukkit.getMinecraftVersion();
            return isAtLeast121(versionString);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Parses a Minecraft version string (e.g. {@code "1.21.1"}, {@code "1.21"}) and returns
     * {@code true} if the version is 1.21 or higher.
     *
     * <p>Package-private for testing.
     */
    static boolean isAtLeast121(String versionString) {
        if (versionString == null || versionString.isBlank()) return false;
        // Strip any snapshot/build suffix (e.g. "1.21-pre1", "1.21-rc1").
        // replaceFirst is safe if no match is found — it leaves the string unchanged.
        String clean = versionString.replaceFirst("[^0-9.]+.*$", "").trim();
        if (clean.isEmpty()) return false;
        String[] parts = clean.split("\\.");
        if (parts.length < 2) return false;
        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return major > 1 || (major == 1 && minor >= 21);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean hasClass(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
