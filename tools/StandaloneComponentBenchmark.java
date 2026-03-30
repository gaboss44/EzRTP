import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class StandaloneComponentBenchmark {
    public static void main(String[] args) throws Exception {
        MiniMessage mm = MiniMessage.miniMessage();
        int players = 1000;
        int messagesPer = 50;
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < players; i++) names.add("player" + i);

        String template = "<gray>Teleporting player <white><player></white>...</gray>";
        Component base = mm.deserialize(template);

        System.out.println("StandaloneComponentBenchmark: players=" + players + " messagesPer=" + messagesPer + " threads=" + threads);

        // Warmup
        runBenchmark(base, names, messagesPer, threads, mm, false);

        long slow = runBenchmark(base, names, messagesPer, threads, mm, false);
        System.out.println("Slow (serialize/parse) ms: " + slow);

        long fast = runBenchmark(base, names, messagesPer, threads, mm, true);
        System.out.println("Fast (replaceText) ms: " + fast);
    }

    private static long runBenchmark(Component base, List<String> names, int messagesPer, int threads, MiniMessage mm, boolean useFast) throws InterruptedException, ExecutionException {
        ExecutorService ex = Executors.newFixedThreadPool(threads);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (String name : names) {
            tasks.add(() -> {
                for (int i = 0; i < messagesPer; i++) {
                    if (useFast) {
                        // component-level replace
                        Component c = fastReplace(base, name);
                        c.hashCode();
                    } else {
                        // serialize -> string replace -> parse
                        String s = mm.serialize(base);
                        s = s.replace("<player>", name);
                        Component c = mm.deserialize(s);
                        c.hashCode();
                    }
                }
                return null;
            });
        }
        long start = System.nanoTime();
        List<Future<Void>> res = ex.invokeAll(tasks);
        for (Future<Void> f : res) f.get();
        ex.shutdown();
        ex.awaitTermination(1, TimeUnit.MINUTES);
        long end = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(end - start);
    }

    private static Component fastReplace(Component c, String name) {
        if (c instanceof TextComponent tc) {
            String content = tc.content();
            String replaced = content.replace("<player>", name);
            // Preserve only the replaced text; keep children attached if any
            Component base = Component.text(replaced);
            if (!c.children().isEmpty()) base = base.toBuilder().append(c.children().toArray(new Component[0])).build();
            return base;
        }
        // fallback: try to replace in children only
        List<Component> newChildren = new ArrayList<>();
        boolean changed = false;
        for (Component child : c.children()) {
            Component nc = fastReplace(child, name);
            newChildren.add(nc);
            if (nc != child) changed = true;
        }
        if (changed) {
            try {
                return c.toBuilder().append(newChildren.toArray(new Component[0])).build();
            } catch (Throwable ignored) {
                return c;
            }
        }
        return c;
    }
}
