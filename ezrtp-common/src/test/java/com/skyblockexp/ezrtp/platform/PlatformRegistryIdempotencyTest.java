package com.skyblockexp.ezrtp.platform;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlatformRegistryIdempotencyTest {

    @Test
    void repeatedProviderClassRegistrationsAreDedupedAcrossRegistries() {
        ChunkLoadStrategyRegistry.clearProviders();
        ChunkLoadStrategyRegistry.registerProvider(new TestChunkLoadStrategyProvider());
        ChunkLoadStrategyRegistry.registerProvider(new TestChunkLoadStrategyProvider());
        assertEquals(1, ChunkLoadStrategyRegistry.providerCount());

        PlatformRuntimeRegistry.clearProviders();
        PlatformRuntimeRegistry.registerProvider(new TestPlatformRuntimeProvider());
        PlatformRuntimeRegistry.registerProvider(new TestPlatformRuntimeProvider());
        assertEquals(1, PlatformRuntimeRegistry.providerCount());

        PlatformGuiBridgeRegistry.clearProviders();
        PlatformGuiBridgeRegistry.registerProvider(new TestPlatformGuiBridgeProvider());
        PlatformGuiBridgeRegistry.registerProvider(new TestPlatformGuiBridgeProvider());
        assertEquals(1, PlatformGuiBridgeRegistry.providerCount());

        PlatformMessageServiceRegistry.clearProviders();
        PlatformMessageServiceRegistry.registerProvider(new TestPlatformMessageServiceProvider());
        PlatformMessageServiceRegistry.registerProvider(new TestPlatformMessageServiceProvider());
        assertEquals(1, PlatformMessageServiceRegistry.providerCount());

        PlatformSenderBridgeRegistry.clearProviders();
        PlatformSenderBridgeRegistry.registerProvider(new TestPlatformSenderBridgeProvider());
        PlatformSenderBridgeRegistry.registerProvider(new TestPlatformSenderBridgeProvider());
        assertEquals(1, PlatformSenderBridgeRegistry.providerCount());
    }

    private static final class TestChunkLoadStrategyProvider implements ChunkLoadStrategyProvider {
        @Override
        public boolean supports(Plugin plugin) {
            return false;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public ChunkLoadStrategy create(Plugin plugin) {
            return null;
        }
    }

    private static final class TestPlatformRuntimeProvider implements PlatformRuntimeProvider {
        @Override
        public boolean supports(Plugin plugin) {
            return false;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public PlatformRuntime create(Plugin plugin) {
            return null;
        }
    }

    private static final class TestPlatformGuiBridgeProvider implements PlatformGuiBridgeProvider {
        @Override
        public boolean supports(Plugin plugin) {
            return false;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public PlatformGuiBridge create(Plugin plugin) {
            return null;
        }
    }

    private static final class TestPlatformMessageServiceProvider implements PlatformMessageServiceProvider {
        @Override
        public boolean supports(Plugin plugin) {
            return false;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public PlatformMessageService create(Plugin plugin) {
            return null;
        }
    }

    private static final class TestPlatformSenderBridgeProvider implements PlatformSenderBridgeProvider {
        @Override
        public boolean supports(Plugin plugin) {
            return false;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public PlatformSenderBridge create(Plugin plugin) {
            return null;
        }
    }
}
