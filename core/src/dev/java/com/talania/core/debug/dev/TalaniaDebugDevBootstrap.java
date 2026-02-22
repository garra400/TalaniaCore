package com.talania.core.debug.dev;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;

/**
 * Dev-only bootstrap for Talania debug tools.
 */
public final class TalaniaDebugDevBootstrap {
    private TalaniaDebugDevBootstrap() {}

    public static void register(JavaPlugin plugin) {
        if (plugin == null) {
            return;
        }
        plugin.getCommandRegistry().registerCommand(
                new TalaniaDebugCommand("talania", "Talania debug commands"));
    }
}
