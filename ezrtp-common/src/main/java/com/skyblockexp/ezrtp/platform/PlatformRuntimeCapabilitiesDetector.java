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

    private static boolean hasClass(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
