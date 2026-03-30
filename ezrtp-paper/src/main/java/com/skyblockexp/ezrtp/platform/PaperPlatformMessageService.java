package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.util.MessageUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Paper-specific implementation of PlatformMessageService. Uses PlaceholderAPI
 * for placeholder resolution and attempts to use BukkitAudiences for Component delivery
 * when the adventure-platform-bukkit library is available.
 */
public final class PaperPlatformMessageService implements PlatformMessageService {

    private static final String BUKKIT_AUDIENCES_CLASS = "net.kyori.adventure.platform.bukkit.BukkitAudiences";

    private final Object audiences;
    private final PlaceholderResolver placeholderResolver;
    private final AudienceMessenger audienceMessenger;
    private final AudienceCloser audienceCloser;

    public PaperPlatformMessageService(Plugin plugin) {
        this(createAudiences(plugin), new PlaceholderApiResolver(), new ReflectiveAudienceMessenger(), new ReflectiveAudienceCloser());
    }

    PaperPlatformMessageService(
            Object audiences,
            PlaceholderResolver placeholderResolver,
            AudienceMessenger audienceMessenger,
            AudienceCloser audienceCloser
    ) {
        this.audiences = audiences;
        this.placeholderResolver = placeholderResolver;
        this.audienceMessenger = audienceMessenger;
        this.audienceCloser = audienceCloser;
    }

    private static Object createAudiences(Plugin plugin) {
        try {
            Class<?> audiencesClass = Class.forName(BUKKIT_AUDIENCES_CLASS);
            Method createMethod = audiencesClass.getMethod("create", Plugin.class);
            return createMethod.invoke(null, plugin);
        } catch (Throwable ex) {
            return null;
        }
    }

    @Override
    public String resolvePlaceholders(Object player, String text, Logger logger) {
        if (text == null) {
            return null;
        }
        Player target = player instanceof Player typedPlayer ? typedPlayer : null;
        try {
            return placeholderResolver.resolve(target, text);
        } catch (RuntimeException | LinkageError ex) {
            logger.fine("PlaceholderAPI resolution failed: " + ex.getMessage());
            return text;
        }
    }

    @Override
    public boolean sendToSender(Object sender, Component component) {
        if (!(sender instanceof CommandSender commandSender) || component == null) {
            return false;
        }

        if (audiences != null) {
            try {
                if (audienceMessenger.send(audiences, commandSender, component)) {
                    return true;
                }
            } catch (RuntimeException ex) {
                // fallback below
            }
        }

        commandSender.sendMessage(MessageUtil.serializeComponent(component));
        return true;
    }

    @Override
    public void close() {
        if (audiences == null) {
            return;
        }
        try {
            audienceCloser.close(audiences);
        } catch (RuntimeException ignored) {
        }
    }

    @FunctionalInterface
    interface PlaceholderResolver {
        String resolve(Player player, String text);
    }

    @FunctionalInterface
    interface AudienceMessenger {
        boolean send(Object audiences, CommandSender sender, Component component);
    }

    @FunctionalInterface
    interface AudienceCloser {
        void close(Object audiences);
    }

    private static final class PlaceholderApiResolver implements PlaceholderResolver {
        @Override
        public String resolve(Player player, String text) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
    }

    private static final class ReflectiveAudienceMessenger implements AudienceMessenger {
        @Override
        public boolean send(Object audiences, CommandSender sender, Component component) {
            try {
                Method senderMethod = audiences.getClass().getMethod("sender", CommandSender.class);
                Object audience = senderMethod.invoke(audiences, sender);
                Method sendMessageMethod = audience.getClass().getMethod("sendMessage", Component.class);
                sendMessageMethod.invoke(audience, component);
                return true;
            } catch (Throwable ex) {
                return false;
            }
        }
    }

    private static final class ReflectiveAudienceCloser implements AudienceCloser {
        @Override
        public void close(Object audiences) {
            try {
                Method closeMethod = audiences.getClass().getMethod("close");
                closeMethod.invoke(audiences);
            } catch (Throwable ignored) {
            }
        }
    }
}
