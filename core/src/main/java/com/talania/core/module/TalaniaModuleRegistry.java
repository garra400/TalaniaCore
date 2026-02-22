package com.talania.core.module;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.debug.DebugRegistry;
import com.talania.core.profile.TalaniaPlayerProfile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for Talania module lifecycle hooks.
 */
public final class TalaniaModuleRegistry {
    private static final TalaniaModuleRegistry INSTANCE = new TalaniaModuleRegistry();

    private final Map<String, ModuleHooks> modules = new ConcurrentHashMap<>();

    private TalaniaModuleRegistry() {
    }

    public static TalaniaModuleRegistry get() {
        return INSTANCE;
    }

    public void register(String moduleId, ModuleHooks hooks) {
        if (moduleId == null || moduleId.isBlank() || hooks == null) {
            return;
        }
        modules.put(moduleId, hooks);
        hooks.registerDebug(com.talania.core.debug.TalaniaDebug.registry());
    }

    public void initModules(JavaPlugin plugin) {
        for (ModuleHooks hooks : modules.values()) {
            hooks.init(plugin);
        }
    }

    public void handlePlayerReady(PlayerRef playerRef, TalaniaPlayerProfile profile,
                                  Ref<EntityStore> ref, Store<EntityStore> store) {
        for (ModuleHooks hooks : modules.values()) {
            hooks.onPlayerReady(playerRef, profile, ref, store);
        }
    }

    public void handlePlayerDisconnect(PlayerRef playerRef) {
        for (ModuleHooks hooks : modules.values()) {
            hooks.onPlayerDisconnect(playerRef);
        }
    }

    public void registerDebug(DebugRegistry registry) {
        for (ModuleHooks hooks : modules.values()) {
            hooks.registerDebug(registry);
        }
    }

    public boolean openDebugSection(String moduleId, String sectionId, PlayerRef playerRef,
                                    Ref<EntityStore> ref, Store<EntityStore> store) {
        if (moduleId == null) {
            return false;
        }
        ModuleHooks hooks = modules.get(moduleId);
        if (hooks == null) {
            return false;
        }
        hooks.openDebugSection(sectionId, playerRef, ref, store);
        return true;
    }
}
