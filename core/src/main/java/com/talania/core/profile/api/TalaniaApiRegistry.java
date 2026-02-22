package com.talania.core.profile.api;

/**
 * Global registry for the active Talania API implementation.
 */
public final class TalaniaApiRegistry {
    private static volatile TalaniaApi api;

    private TalaniaApiRegistry() {
    }

    public static void register(TalaniaApi impl) {
        api = impl;
    }

    public static TalaniaApi get() {
        return api;
    }
}
