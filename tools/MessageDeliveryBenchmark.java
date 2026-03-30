import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.platform.PlatformMessageServiceRegistry;
import com.skyblockexp.ezrtp.platform.PlatformMessageService;
import net.kyori.adventure.text.Component;

import java.util.logging.Logger;

/**
 * Simple micro-benchmark for message delivery and placeholder resolution.
 * Run from project root with `java -cp target/classes:ezrtp-common/target/ezrtp-common-1.6.2.jar tools.MessageDeliveryBenchmark`
 */
public class MessageDeliveryBenchmark {
    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger("benchmark");
        String template = "Hello <#2fff7a>user</#2fff7a> %player_name%! This is a long message to test performance.";
        Object fakePlayer = new Object();

        // Warmup
        for (int i = 0; i < 200; i++) {
            MessageUtil.serialize(template);
        }

        // Measure MessageUtil path
        long t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            String resolved = null;
            try {
                var svc = PlatformMessageServiceRegistry.get();
                if (svc != null) resolved = svc.resolvePlaceholders(fakePlayer, template, logger);
                else resolved = template;
            } catch (Throwable ignored) { resolved = template; }
            Component comp = MessageUtil.parseMiniMessage(resolved);
            String out = MessageUtil.serializeComponent(comp);
            if (out == null) throw new RuntimeException("null");
        }
        long t1 = System.nanoTime();

        System.out.println("MessageUtil path time ms: " + ((t1 - t0) / 1_000_000.0));

        // Measure PlatformMessageService path (mock)
        PlatformMessageService mockSvc = new PlatformMessageService() {
            @Override
            public String resolvePlaceholders(Object player, String text, Logger logger) {
                return text.replace("%player_name%", "benchmark");
            }

            @Override
            public boolean sendToSender(Object sender, Component component) {
                // simulate quick send
                return true;
            }
        };
        PlatformMessageServiceRegistry.register(mockSvc);

        long t2 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            String replaced = PlatformMessageServiceRegistry.get().resolvePlaceholders(fakePlayer, template, logger);
            // simulate send
            PlatformMessageServiceRegistry.get().sendToSender(fakePlayer, MessageUtil.parseMiniMessage(replaced));
        }
        long t3 = System.nanoTime();

        System.out.println("PlatformService path time ms: " + ((t3 - t2) / 1_000_000.0));

        PlatformMessageServiceRegistry.unregister();
    }
}
