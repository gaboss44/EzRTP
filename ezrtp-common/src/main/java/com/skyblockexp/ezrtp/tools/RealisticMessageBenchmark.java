package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.util.MessageUtil;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Synthetic benchmark that simulates many players receiving per-player
 * placeholder-resolved messages concurrently. Compares the code path with
 * and without a registered PlatformMessageService.
 */
public final class RealisticMessageBenchmark {

    private static final Logger LOG = Logger.getLogger("RealisticBenchmark");

    public static void main(String[] args) throws Exception {
        // Create a provider with default messages
        var provider = MessageProvider.createDefault("en", LOG);

        // Create simulated players
        int players = 1000; // number of distinct players
        int perPlayerMessages = 50; // messages per player
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

        List<MockPlayer> mockPlayers = new ArrayList<>(players);
        for (int i = 0; i < players; i++) mockPlayers.add(new MockPlayer("player" + i));

        System.out.println("Benchmark: players=" + players + " messagesPerPlayer=" + perPlayerMessages + " threads=" + threads);

        // Warmup
        runTrial(provider, mockPlayers, perPlayerMessages, threads, false);

        // Trial A: no platform service
        PlatformMessageServiceRegistry.unregister();
        long noService = runTrial(provider, mockPlayers, perPlayerMessages, threads, false);
        System.out.println("No service time ms: " + noService);

        // Trial B: with a simple platform service that handles resolve+send
        DummyPlatformService svc = new DummyPlatformService();
        PlatformMessageServiceRegistry.register(svc);
        long withService = runTrial(provider, mockPlayers, perPlayerMessages, threads, false);
        System.out.println("With service time ms: " + withService);

        PlatformMessageServiceRegistry.unregister();

        System.out.println("Done. Service send count: " + svc.sendCount);
    }

    private static long runTrial(MessageProvider provider, List<MockPlayer> players,
                                 int perPlayerMessages, int threads, boolean verbose) throws InterruptedException, ExecutionException {
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        List<Callable<Long>> tasks = new ArrayList<>();
        for (MockPlayer p : players) {
            tasks.add(() -> {
                long t0 = System.nanoTime();
                for (int i = 0; i < perPlayerMessages; i++) {
                    // Make a template with a placeholder that varies per player
                    Component comp = provider.format(MessageKey.TELEPORTING, Map.of("player", p.getName()), p);
                    // Send via MessageUtil (which will use platform service if available)
                    MessageUtil.send(p, comp);
                }
                long t1 = System.nanoTime();
                return TimeUnit.NANOSECONDS.toMillis(t1 - t0);
            });
        }

        long start = System.nanoTime();
        List<Future<Long>> res = ex.invokeAll(tasks);
        long total = 0;
        for (Future<Long> f : res) total += f.get();
        ex.shutdown();
        ex.awaitTermination(1, TimeUnit.MINUTES);
        long end = System.nanoTime();
        long wallMs = TimeUnit.NANOSECONDS.toMillis(end - start);
        return wallMs;
    }

    private static final class MockPlayer {
        private final String name;
        private final UUID id = UUID.randomUUID();
        MockPlayer(String name) { this.name = name; }
        public UUID getUniqueId() { return id; }
        public String getName() { return name; }
        // mimic Bukkit's sendMessage for reflection-based paths
        public void sendMessage(String s) { /* no-op */ }
        // mimic Adventure-based send
        public void sendMessage(net.kyori.adventure.text.Component c) { /* no-op */ }
    }

    private static final class DummyPlatformService implements PlatformMessageService {
        volatile long sendCount = 0;
        @Override
        public String resolvePlaceholders(Object player, String text, Logger logger) {
            // Very cheap per-player replacement: replace <player> with name
            try {
                java.lang.reflect.Method m = player.getClass().getMethod("getName");
                Object name = m.invoke(player);
                if (name != null) return text.replace("<player>", name.toString());
            } catch (Throwable ignored) {}
            return text;
        }

        @Override
        public boolean sendToSender(Object sender, Component component) {
            sendCount++;
            // pretend we delivered component directly
            return true;
        }
    }
}
