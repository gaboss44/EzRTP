package com.skyblockexp.ezrtp.util.compat;

import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

/**
 * Runtime-safe BossBar wrapper. If the server provides the BossBar API this
 * will create and proxy calls; otherwise it becomes a safe no-op wrapper.
 */
public final class BossBarCompat {

    private BossBarCompat() {}

    public static Wrapper create(String title, BarColor color, BarStyle style) {
        try {
            BossBar bossBar = Bukkit.createBossBar(title, color, style);
            return new Wrapper(bossBar, true);
        } catch (NoSuchMethodError | Exception e) {
            // Fallback to reflection for older versions
            try {
                Method create = Bukkit.class.getMethod("createBossBar", String.class, BarColor.class, BarStyle.class);
                Object bossBar = create.invoke(null, title, color, style);
                return new Wrapper(bossBar, true);
            } catch (Exception ex) {
                return new Wrapper(null, false);
            }
        }
    }

    public static final class Wrapper {
        private final Object bossBarInstance;
        private final BossBar directBossBar;
        private final boolean supported;

        private Wrapper(Object bossBarInstance, boolean supported) {
            this.bossBarInstance = bossBarInstance;
            this.directBossBar = bossBarInstance instanceof BossBar ? (BossBar) bossBarInstance : null;
            this.supported = supported && bossBarInstance != null;
        }

        public boolean isSupported() { return supported && bossBarInstance != null; }

        public void setTitle(Component component) {
            if (!isSupported()) return;
            if (directBossBar != null) {
                // Use direct API
                directBossBar.setTitle(MessageUtil.serializeComponent(component));
            } else {
                // Fallback to reflection
                try {
                    Method setTitleStr = bossBarInstance.getClass().getMethod("setTitle", String.class);
                    setTitleStr.invoke(bossBarInstance, MessageUtil.serializeComponent(component));
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        public void addPlayer(Player player) {
            if (!isSupported()) return;
            if (directBossBar != null) {
                directBossBar.addPlayer(player);
            } else {
                try {
                    Method add = bossBarInstance.getClass().getMethod("addPlayer", Player.class);
                    add.invoke(bossBarInstance, player);
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        public void removeAll() {
            if (!isSupported()) return;
            if (directBossBar != null) {
                directBossBar.removeAll();
            } else {
                try {
                    Method rem = bossBarInstance.getClass().getMethod("removeAll");
                    rem.invoke(bossBarInstance);
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        public void setProgress(double progress) {
            if (!isSupported()) return;
            if (directBossBar != null) {
                directBossBar.setProgress(progress);
            } else {
                try {
                    Method set = bossBarInstance.getClass().getMethod("setProgress", double.class);
                    set.invoke(bossBarInstance, progress);
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        public void setColor(BarColor color) {
            if (!isSupported()) return;
            if (directBossBar != null) {
                directBossBar.setColor(color);
            } else {
                try {
                    Method set = bossBarInstance.getClass().getMethod("setColor", BarColor.class);
                    set.invoke(bossBarInstance, color);
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        public void setStyle(BarStyle style) {
            if (!isSupported()) return;
            if (directBossBar != null) {
                directBossBar.setStyle(style);
            } else {
                try {
                    Method set = bossBarInstance.getClass().getMethod("setStyle", BarStyle.class);
                    set.invoke(bossBarInstance, style);
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        public void setVisible(boolean visible) {
            if (!isSupported()) return;
            if (directBossBar != null) {
                directBossBar.setVisible(visible);
            } else {
                try {
                    Method set = bossBarInstance.getClass().getMethod("setVisible", boolean.class);
                    set.invoke(bossBarInstance, visible);
                } catch (ReflectiveOperationException ignored) {}
            }
        }

        @SuppressWarnings("unchecked")
        public Collection<Player> getPlayers() {
            if (!isSupported()) return Collections.emptyList();
            if (directBossBar != null) {
                return directBossBar.getPlayers();
            } else {
                try {
                    Method get = bossBarInstance.getClass().getMethod("getPlayers");
                    Object out = get.invoke(bossBarInstance);
                    return (Collection<Player>) out;
                } catch (ReflectiveOperationException | ClassCastException e) {
                    return Collections.emptyList();
                }
            }
        }
    }
}
