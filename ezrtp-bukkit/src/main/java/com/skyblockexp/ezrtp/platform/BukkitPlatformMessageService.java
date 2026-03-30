package com.skyblockexp.ezrtp.platform;

import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;
import java.util.List;

/**
 * Bukkit implementation of PlatformMessageService using PlaceholderAPI and
 * BukkitAudiences for efficient Component delivery.
 */
public final class BukkitPlatformMessageService implements PlatformMessageService {

    private final Plugin plugin;
    private final BukkitAudiences audiences;

    public BukkitPlatformMessageService(Plugin plugin) {
        this.plugin = plugin;
        BukkitAudiences tmp = null;
        try {
            tmp = BukkitAudiences.create(plugin);
        } catch (Throwable ignored) {}
        this.audiences = tmp;
    }

    @Override
    public String resolvePlaceholders(Object player, String text, Logger logger) {
        if (text == null) return null;
        try {
            if (player instanceof Player) {
                return PlaceholderAPI.setPlaceholders((Player) player, text);
            }
            // PlaceholderAPI supports null player for some server placeholders
            return PlaceholderAPI.setPlaceholders(null, text);
        } catch (Throwable t) {
            logger.fine("PlaceholderAPI resolution failed: " + t.getMessage());
            return text;
        }
    }

    @Override
    public net.kyori.adventure.text.Component resolvePlaceholdersComponent(Object player, net.kyori.adventure.text.Component component, Logger logger) {
        if (component == null) return net.kyori.adventure.text.Component.empty();
        try {
            String playerName = null;
            if (player instanceof Player) playerName = ((Player) player).getName();
            else {
                try {
                    java.lang.reflect.Method m = player.getClass().getMethod("getName");
                    Object nm = m.invoke(player);
                    if (nm != null) playerName = nm.toString();
                } catch (Throwable ignored) {}
            }
            if (playerName == null) return component;
            // Prefer Adventure's replaceText when available (reflective), fallback to local fastReplace.
            try {
                Class<?> trcClass = Class.forName("net.kyori.adventure.text.replace.TextReplacementConfig");
                java.lang.reflect.Method builderMeth = trcClass.getMethod("builder");
                Object builder = builderMeth.invoke(null);
                builder.getClass().getMethod("matchLiteral", String.class).invoke(builder, "<player>");
                builder.getClass().getMethod("replacement", net.kyori.adventure.text.Component.class)
                        .invoke(builder, net.kyori.adventure.text.Component.text(playerName));
                Object trc = builder.getClass().getMethod("build").invoke(builder);
                java.lang.reflect.Method replaceMeth = component.getClass().getMethod("replaceText", trcClass);
                Object replaced = replaceMeth.invoke(component, trc);
                if (replaced instanceof net.kyori.adventure.text.Component) return (net.kyori.adventure.text.Component) replaced;
            } catch (Throwable ignored) {
                // fallback to local traversal
            }
            return fastReplace(component, playerName);
        } catch (Throwable t) {
            logger.fine("Component placeholder resolution failed: " + t.getMessage());
            return component;
        }
    }

    private Component fastReplace(Component c, String name) {
        if (c instanceof TextComponent tc) {
            String content = tc.content();
            if (!content.contains("<player>")) {
                // still process children
                List<Component> newChildren = new java.util.ArrayList<>();
                boolean changed = false;
                for (Component child : c.children()) {
                    Component nc = fastReplace(child, name);
                    newChildren.add(nc);
                    if (nc != child) changed = true;
                }
                if (changed) {
                    try {
                        var b = tc.toBuilder();
                        for (Component nch : newChildren) b.append(nch);
                        return b.build();
                    } catch (Throwable ignored) { return c; }
                }
                return c;
            }
            String replaced = content.replace("<player>", name);
            try {
                var b = tc.toBuilder().content(replaced);
                // process and attach children preserving styles
                for (Component child : c.children()) b.append(fastReplace(child, name));
                return b.build();
            } catch (Throwable ignored) {
                Component base = Component.text(replaced);
                if (!c.children().isEmpty()) base = base.toBuilder().append(c.children().toArray(new Component[0])).build();
                return base;
            }
        }
        List<Component> newChildren = new java.util.ArrayList<>();
        boolean changed = false;
        for (Component child : c.children()) {
            Component nc = fastReplace(child, name);
            newChildren.add(nc);
            if (nc != child) changed = true;
        }
        if (changed) {
            try {
                var b = c.toBuilder();
                for (Component nch : newChildren) b.append(nch);
                return b.build();
            } catch (Throwable ignored) {
                return c;
            }
        }
        return c;
    }

    @Override
    public boolean sendToSender(Object sender, Component component) {
        if (sender == null || component == null) return false;
        try {
            if (sender instanceof CommandSender commandSender) {
                if (audiences != null) {
                    audiences.sender((CommandSender) sender).sendMessage(component);
                    return true;
                }
                commandSender.sendMessage(MessageUtil.serializeComponent(component));
                return true;
            }
            // Fallback to reflective sendMessage for non-CommandSender objects only.
            try {
                sender.getClass().getMethod("sendMessage", String.class)
                        .invoke(sender, MessageUtil.serializeComponent(component));
                return true;
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public void close() {
        try { if (audiences != null) audiences.close(); } catch (Throwable ignored) {}
    }
}
