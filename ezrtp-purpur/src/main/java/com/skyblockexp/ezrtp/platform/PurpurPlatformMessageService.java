package com.skyblockexp.ezrtp.platform;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Purpur-specific messaging implementation using direct API calls.
 */
public final class PurpurPlatformMessageService implements PlatformMessageService {
    private static final String PLACEHOLDER_API_CLASS = "me.clip.placeholderapi.PlaceholderAPI";
    private static final Class<?>[] PLACEHOLDER_SIGNATURE = new Class<?>[] {Player.class, String.class};
    private static volatile Method setPlaceholdersMethod;


    public PurpurPlatformMessageService(Plugin plugin) {
        // Purpur exposes Adventure sendMessage(Component) directly on CommandSender.
        // Keep constructor for interface compatibility.
    }

    @Override
    public String resolvePlaceholders(Object player, String text, Logger logger) {
        if (text == null) {
            return null;
        }
        try {
            if (player instanceof Player bukkitPlayer) {
                return invokeSetPlaceholders(bukkitPlayer, text);
            }
            return invokeSetPlaceholders(null, text);
        } catch (Throwable t) {
            logger.fine("PlaceholderAPI resolution failed: " + t.getMessage());
            return text;
        }
    }

    private static String invokeSetPlaceholders(Player player, String text) throws ReflectiveOperationException {
        Method method = setPlaceholdersMethod;
        if (method == null) {
            synchronized (PurpurPlatformMessageService.class) {
                method = setPlaceholdersMethod;
                if (method == null) {
                    Class<?> placeholderApi = Class.forName(PLACEHOLDER_API_CLASS);
                    method = placeholderApi.getMethod("setPlaceholders", PLACEHOLDER_SIGNATURE);
                    setPlaceholdersMethod = method;
                }
            }
        }
        return (String) method.invoke(null, player, text);
    }

    @Override
    public boolean sendToSender(Object sender, Component component) {
        if (!(sender instanceof CommandSender commandSender) || component == null) {
            return false;
        }
        try {
            commandSender.getClass().getMethod("sendMessage", Component.class).invoke(commandSender, component);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Override
    public void close() {
        // no-op
    }
}
