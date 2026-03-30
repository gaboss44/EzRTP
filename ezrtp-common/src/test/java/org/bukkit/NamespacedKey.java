package org.bukkit;

/** Minimal NamespacedKey stub for tests. */
public final class NamespacedKey {
    private final String key;

    public NamespacedKey(String key) {
        this.key = key;
    }

    public static NamespacedKey minecraft(String value) {
        return new NamespacedKey("minecraft:" + value);
    }

    public String getKey() {
        return key;
    }
    @Override
    public String toString() {
        return key;
    }
}
