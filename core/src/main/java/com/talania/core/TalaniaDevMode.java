package com.talania.core;

/**
 * Tracks whether Talania dev/debug mode is enabled.
 */
public final class TalaniaDevMode {
    private static volatile boolean initialized = false;
    private static volatile boolean enabled = false;

    private TalaniaDevMode() {}

    public static void initialize(Class<?> anchor) {
        if (initialized) {
            return;
        }
        initialized = true;
        enabled = anchor != null && anchor.getResource("/talania-dev.flag") != null;
    }

    public static boolean isEnabled() {
        if (!initialized) {
            initialize(TalaniaDevMode.class);
        }
        return enabled;
    }
}
