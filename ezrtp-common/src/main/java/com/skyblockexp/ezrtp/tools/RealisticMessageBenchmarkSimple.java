package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Lightweight benchmark that avoids depending on MessageProvider/MessageKey.
 */
public final class RealisticMessageBenchmarkSimple {
    private static final Logger LOG = Logger.getLogger("RealisticBenchmarkSimple");

    public static void main(String[] args) throws Exception {
        int players = 1000;
        int perPlayerMessages = 50;
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

        List<MockPlayer> mockPlayers = new ArrayList<>(players);
        for (int i = 0; i < players; i++) mockPlayers.add(new MockPlayer("player" + i));

        System.out.println("BenchmarkSimple: players=" + players + " messagesPerPlayer=" + perPlayerMessages + " threads=" + threads);

        // Warmup
        runTrial(mockPlayers, perPlayerMessages, threads);

        PlatformMessageServiceRegistry.unregister();
        long noSvc = runTrial(mockPlayers, perPlayerMessages, threads);
        System.out.println("No service ms: " + noSvc);

        DummyService svc = new DummyService();
        PlatformMessageServiceRegistry.register(svc);
        long withSvc = runTrial(mockPlayers, perPlayerMessages, threads);
        System.out.println("With service ms: " + withSvc + " sendCount=" + svc.sendCount);

        PlatformMessageServiceRegistry.unregister();

        // Trial C: fast component-level resolver (simulates optimized Bukkit implementation)
        FastService fast = new FastService();
        PlatformMessageServiceRegistry.register(fast);
        long withFast = runTrial(mockPlayers, perPlayerMessages, threads);
        System.out.println("With fast service ms: " + withFast + " sendCount=" + fast.sendCount);
        PlatformMessageServiceRegistry.unregister();
    }

    private static long runTrial(List<MockPlayer> players, int perPlayerMessages, int threads) throws InterruptedException, ExecutionException {
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        List<Callable<Long>> tasks = new ArrayList<>();
        String template = "<gray>Teleporting player <white><player></white>...</gray>";
        for (MockPlayer p : players) {
            tasks.add(() -> {
                long t0 = System.nanoTime();
                for (int i = 0; i < perPlayerMessages; i++) {
                    // Without platform service, we do manual placeholder replace
                    Component comp;
                    var svc = PlatformMessageServiceRegistry.get();
                    if (svc != null) {
                        // let service handle component-level resolution
                        comp = MessageUtil.parseMiniMessage(template);
                        comp = svc.resolvePlaceholdersComponent(p, comp, LOG);
                    } else {
                        String replaced = template.replace("<player>", p.getName());
                        comp = MessageUtil.parseMiniMessage(replaced);
                    }
                    MessageUtil.send(p, comp);
                }
                long t1 = System.nanoTime();
                return TimeUnit.NANOSECONDS.toMillis(t1 - t0);
            });
        }
        long start = System.nanoTime();
        List<Future<Long>> res = ex.invokeAll(tasks);
        ex.shutdown();
        ex.awaitTermination(1, TimeUnit.MINUTES);
        long end = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(end - start);
    }

    private static final class MockPlayer {
        private final String name;
        private final UUID id = UUID.randomUUID();
        MockPlayer(String name) { this.name = name; }
        public UUID getUniqueId() { return id; }
        public String getName() { return name; }
        public void sendMessage(String s) {}
        public void sendMessage(net.kyori.adventure.text.Component c) {}
    }

    private static final class DummyService implements PlatformMessageService {
        volatile long sendCount = 0;
        @Override
        public String resolvePlaceholders(Object player, String text, Logger logger) {
            try {
                var m = player.getClass().getMethod("getName");
                var name = m.invoke(player);
                return text.replace("<player>", name == null ? "" : name.toString());
            } catch (Throwable t) { return text; }
        }

        @Override
        public boolean sendToSender(Object sender, Component component) {
            sendCount++;
            return true;
        }
    }

    private static final class FastService implements PlatformMessageService {
        volatile long sendCount = 0;
        @Override
        public String resolvePlaceholders(Object player, String text, Logger logger) {
            // Keep a cheap string replacement fallback
            try {
                var m = player.getClass().getMethod("getName");
                var name = m.invoke(player);
                return text.replace("<player>", name == null ? "" : name.toString());
            } catch (Throwable t) { return text; }
        }

        @Override
        public boolean sendToSender(Object sender, Component component) {
            sendCount++;
            return true;
        }

        @Override
        public Component resolvePlaceholdersComponent(Object player, Component component, Logger logger) {
            try {
                String playerName = null;
                try {
                    var m = player.getClass().getMethod("getName");
                    var nm = m.invoke(player);
                    if (nm != null) playerName = nm.toString();
                } catch (Throwable ignored) {}
                if (playerName == null) return component;
                // Fallback: serialize -> replace -> parse so this file doesn't require
                // the adventure.text.replace module to compile in all environments.
                String serialized = MessageUtil.serializeToMiniMessage(component);
                String replaced = serialized.replace("<player>", playerName);
                return MessageUtil.parseMiniMessage(replaced);
            } catch (Throwable t) {
                return component;
            }
        }
    }
}
