package com.talania.races;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.api.TalaniaApiRegistry;
import com.talania.core.runtime.TalaniaCoreRuntime;
import com.talania.core.module.ModuleHooks;
import com.talania.core.module.TalaniaModuleRegistry;
import com.talania.races.api.TalaniaApiImpl;
import com.talania.races.system.RaceConditionalEffectSystem;

import javax.annotation.Nonnull;

/**
 * Races module plugin. Registers the Talania API implementation for races.
 */
public final class TalaniaRacesPlugin extends JavaPlugin {
    private final RaceService raceService = new RaceService();
    private TalaniaApiImpl api;

    public TalaniaRacesPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.api = new TalaniaApiImpl(raceService);
        TalaniaApiRegistry.register(api);
        getEntityStoreRegistry().registerSystem(new RaceConditionalEffectSystem(raceService));
        TalaniaModuleRegistry.get().register("races", new ModuleHooks() {
            @Override
            public void onPlayerReady(PlayerRef playerRef, TalaniaPlayerProfile profile,
                                      com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref,
                                      com.hypixel.hytale.component.Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store) {
                handlePlayerReady(playerRef, profile);
            }

            @Override
            public void onPlayerDisconnect(PlayerRef playerRef) {
                handlePlayerDisconnect(playerRef);
            }

            @Override
            public void registerDebug(com.talania.core.debug.DebugRegistry registry) {
                registry.registerModule("races", "Races", builder -> builder.section("main", "Races"));
            }

            @Override
            public void openDebugSection(String sectionId, PlayerRef playerRef,
                                         com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref,
                                         com.hypixel.hytale.component.Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store) {
                if (!"main".equalsIgnoreCase(sectionId)) {
                    return;
                }
                tryOpenRacesDebugUi(playerRef, ref, store);
            }
        });
    }

    private void tryOpenRacesDebugUi(PlayerRef playerRef,
                                     com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref,
                                     com.hypixel.hytale.component.Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store) {
        if (playerRef == null || ref == null || store == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("com.talania.races.debug.dev.TalaniaRacesDebugPage");
            java.lang.reflect.Method method = clazz.getDeclaredMethod("open",
                    com.hypixel.hytale.server.core.universe.PlayerRef.class,
                    com.hypixel.hytale.component.Ref.class,
                    com.hypixel.hytale.component.Store.class,
                    TalaniaRacesPlugin.class);
            method.invoke(null, playerRef, ref, store, this);
        } catch (ClassNotFoundException ignored) {
            // Dev-only classes not present in release build.
        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.WARNING)
                    .log("Failed to open races debug UI: %s", e.getMessage());
        }
    }

    public RaceService raceService() {
        return raceService;
    }

    public TalaniaApiImpl api() {
        return api;
    }

    /**
     * Assign a race and persist it to the player's profile.
     */
    public void setRace(java.util.UUID playerId, RaceType race) {
        if (playerId == null || race == null) {
            return;
        }
        TalaniaCoreRuntime core = TalaniaCoreRuntime.get();
        if (core == null) {
            return;
        }
        TalaniaPlayerProfile profile = core.profileRuntime().load(playerId);
        if (profile == null) {
            return;
        }
        profile.setRaceId(race.id());
        raceService.setRace(playerId, race);
        core.profileRuntime().save(playerId);
    }

    private void handlePlayerReady(PlayerRef playerRef, TalaniaPlayerProfile profile) {
        if (playerRef == null || profile == null) {
            return;
        }
        TalaniaCoreRuntime core = TalaniaCoreRuntime.get();
        if (core == null) {
            return;
        }
        java.util.UUID playerId = playerRef.getUuid();
        RaceType race = RaceType.fromId(profile.raceId());
        if (race == null) {
            race = RaceType.HUMAN;
            profile.setRaceId(race.id());
            core.profileRuntime().save(playerId);
        }
        raceService.setRace(playerId, race);
    }

    private void handlePlayerDisconnect(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }
        raceService.clearRace(playerRef.getUuid());
    }
}
