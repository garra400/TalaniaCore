package com.talania.core.runtime;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.hytale.stats.EntityStatModifierRegistry;
import com.talania.core.hytale.stats.EntityStatModifierService;
import com.talania.core.hytale.stats.EntityStatSyncService;
import com.talania.core.input.InputPatternTracker;
import com.talania.core.combat.shield.EnergyShieldService;
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.module.TalaniaModuleRegistry;
import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.TalaniaProfileRuntime;
import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Central runtime wiring for TalaniaCore systems.
 */
public final class TalaniaCoreRuntime {
    private static volatile TalaniaCoreRuntime instance;

    private final TalaniaProfileRuntime profileRuntime;
    private final EntityStatModifierRegistry statModifierRegistry;
    private final EntityStatModifierService statModifierService;
    private final EntityStatSyncService statSyncService;
    private final InputPatternTracker inputPatternTracker;

    private TalaniaCoreRuntime(Path dataDirectory) {
        TalaniaDebug.init(dataDirectory);
        this.profileRuntime = new TalaniaProfileRuntime(dataDirectory);
        this.statModifierRegistry = new EntityStatModifierRegistry();
        this.statModifierRegistry.registerDefaults();
        this.statModifierService = new EntityStatModifierService(statModifierRegistry);
        this.statSyncService = new EntityStatSyncService(statModifierService);
        this.inputPatternTracker = new InputPatternTracker();
    }

    /**
     * Initialize TalaniaCore runtime services.
     */
    public static TalaniaCoreRuntime init(Path dataDirectory) {
        if (instance == null) {
            instance = new TalaniaCoreRuntime(dataDirectory);
        }
        return instance;
    }

    /**
     * Get the active runtime instance, or null if not initialized.
     */
    public static TalaniaCoreRuntime get() {
        return instance;
    }

    public TalaniaProfileRuntime profileRuntime() {
        return profileRuntime;
    }

    public EntityStatSyncService statSyncService() {
        return statSyncService;
    }

    public InputPatternTracker inputPatternTracker() {
        return inputPatternTracker;
    }

    /**
     * Load profile and apply base stats when a player is ready.
     */
    public void handlePlayerReady(PlayerReadyEvent event) {
        if (event == null) {
            return;
        }
        Ref<EntityStore> ref = event.getPlayerRef();
        if (ref == null || !ref.isValid()) {
            return;
        }
        Store<EntityStore> store = ref.getStore();
        if (store == null) {
            return;
        }
        com.hypixel.hytale.server.core.entity.UUIDComponent uuidComponent =
                store.getComponent(ref, com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }
        UUID playerId = uuidComponent.getUuid();
        TalaniaDebug.handlePlayerReady(playerId);
        TalaniaPlayerProfile profile = profileRuntime.load(playerId);
        if (profile == null) {
            return;
        }
        EntityStats stats = StatsManager.getOrCreate(playerId);
        for (StatType stat : StatType.values()) {
            stats.setBase(stat, profile.getBaseStat(stat, stat.getDefaultValue()));
        }
        stats.recalculate();
        TalaniaDebug.statModifiers().applyToStats(playerId, stats);
        statSyncService.applyAll(ref, store, playerId, stats);

        com.hypixel.hytale.server.core.universe.PlayerRef playerRef =
                com.talania.core.utils.PlayerRefUtil.resolve(ref, store);
        if (playerRef != null) {
            TalaniaModuleRegistry.get().handlePlayerReady(playerRef, profile, ref, store);
        }
    }

    /**
     * Save/unload profile and clear caches when a player disconnects.
     */
    public void handlePlayerDisconnect(PlayerDisconnectEvent event) {
        if (event == null) {
            return;
        }
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef == null) {
            return;
        }
        UUID playerId = playerRef.getUuid();
        TalaniaDebug.handlePlayerDisconnect(playerId);
        EnergyShieldService.clear(playerId);
        profileRuntime.unload(playerId, true);
        StatsManager.unregister(playerId);
        inputPatternTracker.clear(playerId);
        TalaniaModuleRegistry.get().handlePlayerDisconnect(playerRef);
    }

    /**
     * Forward mouse button events into the input pattern tracker.
     */
    public void handleMouseButton(PlayerMouseButtonEvent event) {
        if (event == null) {
            return;
        }
        PlayerRef playerRef = event.getPlayerRefComponent();
        if (playerRef == null) {
            return;
        }
        Ref<EntityStore> ref = event.getPlayerRef();
        if (ref == null || !ref.isValid()) {
            return;
        }
        Store<EntityStore> store = ref.getStore();
        if (store == null) {
            return;
        }
        inputPatternTracker.handleMouseButton(ref, store, event.getMouseButton(), event.getItemInHand());
    }
}
