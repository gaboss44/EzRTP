package com.skyblockexp.ezrtp.packaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies packaging/resource layout for message files.
 */
public class MessagesPackagingTest {

    @Test
    public void packagedResourcesUseLocalizedMessages() {
        ClassLoader loader = getClass().getClassLoader();

        // Expect the localized default to be present
        assertNotNull(loader.getResource("messages/en.yml"), "messages/en.yml should be on the classpath");

        // Expect the legacy top-level messages.yml to be absent
        assertNull(loader.getResource("messages.yml"), "legacy top-level messages.yml should not be packaged");
    }
}
