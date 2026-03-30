package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import java.util.logging.Logger;
import com.skyblockexp.ezrtp.util.MessageUtil;

/**
 * Platform-level messaging service. Platform modules may provide an
 * implementation that performs optimized placeholder resolution and direct
 * Component delivery using platform APIs.
 */
public interface PlatformMessageService {

    /**
     * Resolve placeholders in the provided text using platform integrations
     * (e.g., PlaceholderAPI). Return the resolved text (non-null).
     */
    String resolvePlaceholders(Object player, String text, Logger logger);

    /**
     * Deliver a Component to a sender object. Return true if the platform
     * implementation handled delivery.
     */
    boolean sendToSender(Object sender, Component component);

    /**
     * Allow platform implementations to release resources during plugin disable.
     */
    default void close() {
        // no-op by default
    }

    /**
     * Resolve placeholders in a Component. Default implementation serializes
     * the Component to MiniMessage, calls {@link #resolvePlaceholders}, and
     * reparses the result. Platform implementations may override this for
     * faster, more direct resolution.
     */
    default Component resolvePlaceholdersComponent(Object player, Component component, Logger logger) {
        if (component == null) return Component.empty();
        try {
            String mm = MessageUtil.serializeToMiniMessage(component);
            String replaced = resolvePlaceholders(player, mm, logger);
            return MessageUtil.parseMiniMessage(replaced);
        } catch (Throwable t) {
            return component;
        }
    }

}
