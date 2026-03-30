package com.skyblockexp.ezrtp.packaging;

import com.skyblockexp.ezrtp.platform.PlatformGuiBridge;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlatformGuiBridgeSignatureTest {

    @Test
    void createInventoryUsesUnrelocatedAdventureComponentType() throws Exception {
        Method method = PlatformGuiBridge.class.getMethod(
                "createInventory",
                org.bukkit.inventory.InventoryHolder.class,
                int.class,
                Component.class);

        assertEquals(Component.class, method.getParameterTypes()[2]);
    }
}
