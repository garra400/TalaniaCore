package com.talania.core.profile.api;

/**
 * Static access point for the Talania API implementation.
 */
public final class TalaniaApiProvider {
    private static volatile TalaniaApi instance;

    private TalaniaApiProvider() {}

    /**
     * Get the currently registered API instance.
     */
    public static TalaniaApi get() {
        return instance;
    }

    /**
     * Set the active API instance. Call during plugin bootstrap.
     */
    public static void set(TalaniaApi api) {
        instance = api;
    }
}
