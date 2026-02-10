package com.talania.core.profile.api;

/**
 * Static access point for the Talania profile API implementation.
 */
public final class TalaniaProfileApiProvider {
    private static volatile TalaniaProfileApi instance;

    private TalaniaProfileApiProvider() {}

    /**
     * Get the currently registered profile API instance.
     */
    public static TalaniaProfileApi get() {
        return instance;
    }

    /**
     * Set the active profile API instance. Call during plugin bootstrap.
     */
    public static void set(TalaniaProfileApi api) {
        instance = api;
    }
}
