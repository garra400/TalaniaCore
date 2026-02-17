package com.talania.core.module;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.debug.DebugRegistry;
import com.talania.core.profile.TalaniaPlayerProfile;

/**
 * Optional hooks for Talania modules to integrate with core lifecycle.
 */
public interface ModuleHooks {

    default void init(JavaPlugin plugin) {
    }

    default void onPlayerReady(PlayerRef playerRef, TalaniaPlayerProfile profile,
                               Ref<EntityStore> ref, Store<EntityStore> store) {
    }

    default void onPlayerDisconnect(PlayerRef playerRef) {
    }

    default void registerDebug(DebugRegistry registry) {
    }

    default void openDebugSection(String sectionId, PlayerRef playerRef,
                                  Ref<EntityStore> ref, Store<EntityStore> store) {
    }
}
