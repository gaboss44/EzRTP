package com.skyblockexp.ezrtp.paper;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EzRtpPaperModulePluginTest {

    private static final Logger LOGGER = Logger.getLogger("EzRtpPaperModulePluginTest");

    @Test
    void registerProviderReturnsTrueWhenRegistryAndProviderAreAvailable() {
        TestRegistry.reset();

        boolean result = EzRtpPaperModulePlugin.registerProvider(
                LOGGER,
                TestRegistry.class.getName(),
                TestProvider.class.getName()
        );

        assertTrue(result);
        assertTrue(TestRegistry.registeredProvider instanceof TestProvider);
    }

    @Test
    void registerProviderReturnsFalseWhenRegistryIsMissing() {
        boolean result = EzRtpPaperModulePlugin.registerProvider(
                LOGGER,
                "missing.Registry",
                TestProvider.class.getName()
        );

        assertFalse(result);
    }

    public interface Marker {}

    public static final class TestProvider implements Marker {}

    public static final class TestRegistry {
        static Object registeredProvider;

        private TestRegistry() {}

        public static void registerProvider(Marker provider) {
            registeredProvider = provider;
        }

        static void reset() {
            registeredProvider = null;
        }
    }
}
