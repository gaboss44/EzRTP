package com.skyblockexp.ezrtp.util.compat;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WorldBorderCompatTest {

    @Test
    void getBorderRadiusWhenWorldDoesNotExposeBorderMethods() throws Exception {
        // Many test runtimes do not include World#getWorldBorder; ensure shim returns empty safely.
        World proxy = (World) Proxy.newProxyInstance(
            World.class.getClassLoader(),
            new Class[]{World.class},
            (p, method, args) -> {
                if ("getSpawnLocation".equals(method.getName())) return new Location((World) p, 64.0, 64.0, 64.0);
                throw new UnsupportedOperationException(method.getName());
            }
        );

        Optional<Double> r = WorldBorderCompat.getBorderRadius(proxy);
        assertTrue(r.isEmpty());
    }

    @Test
    void getCenterWhenWorldDoesNotExposeBorderMethods() throws Exception {
        World proxy = (World) Proxy.newProxyInstance(
            World.class.getClassLoader(),
            new Class[]{World.class},
            (p, method, args) -> {
                if ("getSpawnLocation".equals(method.getName())) return new Location((World) p, 64.0, 64.0, 64.0);
                throw new UnsupportedOperationException(method.getName());
            }
        );

        Optional<Location> c = WorldBorderCompat.getCenter(proxy);
        assertTrue(c.isEmpty());
    }

    @Test
    void isInsideWhenWorldDoesNotExposeBorderMethods() throws Exception {
        World proxy = (World) Proxy.newProxyInstance(
            World.class.getClassLoader(),
            new Class[]{World.class},
            (p, method, args) -> {
                if ("getSpawnLocation".equals(method.getName())) return new Location((World) p, 64.0, 64.0, 64.0);
                throw new UnsupportedOperationException(method.getName());
            }
        );

        Location check = new Location(proxy, 0.0, 64.0, 0.0);
        // When border methods are absent, shim should assume inside
        assertTrue(WorldBorderCompat.isInside(check));
    }
}
