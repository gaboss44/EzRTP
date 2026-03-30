package org.bukkit;

/**
 * Minimal Registry stub for tests. Provides a STRUCTURE_TYPE holder and a safe get method.
 */
public final class Registry<T> {
    // Keep as non-final to avoid class initialization ordering issues in tests
    public static Registry<Object> STRUCTURE_TYPE = new Registry<>();

    public Registry() {}

    public T get(NamespacedKey key) {
        return null;
    }
}
