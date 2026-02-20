package com.talania.core.debug;

import com.talania.core.config.ConfigManager;
import com.talania.core.debug.combat.CombatLogManager;
import com.talania.core.debug.events.CombatLogEvent;
import com.talania.core.events.EventBus;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Central access for Talania debug services.
 */
public final class TalaniaDebug {
    private static final DebugRegistry REGISTRY = new DebugRegistry();
    private static final DebugLogService LOG_SERVICE = new DebugLogService();
    private static final CombatLogManager COMBAT_LOG = new CombatLogManager(LOG_SERVICE);
    private static final DebugStatModifierService STAT_MODIFIERS = new DebugStatModifierService();
    private static DebugSettings SETTINGS = new DebugSettings();
    private static boolean initialized = false;

    private TalaniaDebug() {}

    public static void init(Path dataDirectory) {
        if (initialized) {
            return;
        }
        if (!ConfigManager.isInitialized() && dataDirectory != null) {
            ConfigManager.initialize(dataDirectory.resolve("config"));
        }
        if (ConfigManager.isInitialized()) {
            DebugSettings loaded = ConfigManager.load("debug_settings.json", DebugSettings.class, TalaniaDebug.class);
            if (loaded != null) {
                SETTINGS = loaded;
            }
        }
        LOG_SERVICE.setSettings(SETTINGS);
        EventBus.subscribe(CombatLogEvent.class, COMBAT_LOG::handleEvent);
        registerCoreModule();
        initialized = true;
    }

    public static DebugRegistry registry() {
        return REGISTRY;
    }

    public static DebugModule registerModule(String moduleId, String displayName,
                                             Consumer<DebugRegistry.DebugModuleBuilder> builder) {
        return REGISTRY.registerModule(moduleId, displayName, builder);
    }

    public static DebugLogService logs() {
        return LOG_SERVICE;
    }

    public static DebugStatModifierService statModifiers() {
        return STAT_MODIFIERS;
    }

    public static CombatLogManager combatLog() {
        return COMBAT_LOG;
    }

    public static DebugSettings settings() {
        return SETTINGS;
    }

    public static void handlePlayerReady(UUID playerId) {
        LOG_SERVICE.ensurePlayer(playerId);
        STAT_MODIFIERS.ensurePlayer(playerId);
    }

    public static void handlePlayerDisconnect(UUID playerId) {
        LOG_SERVICE.clearPlayer(playerId);
        COMBAT_LOG.clear(playerId);
    }

    public static void tryRegisterDev(JavaPlugin plugin) {
        if (plugin == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("com.talania.core.debug.dev.TalaniaDebugDevBootstrap");
            Method method = clazz.getDeclaredMethod("register", JavaPlugin.class);
            method.invoke(null, plugin);
        } catch (ClassNotFoundException ignored) {
            // Dev-only classes not present in release build.
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.WARNING)
                    .log("Failed to register Talania debug dev tools: %s", e.getMessage());
        }
    }

    private static void registerCoreModule() {
        REGISTRY.registerModule("core", "Core", builder -> {
            builder.section("core-log", "Logging");
            builder.section("core-combat", "Combat");
        });
    }
}
