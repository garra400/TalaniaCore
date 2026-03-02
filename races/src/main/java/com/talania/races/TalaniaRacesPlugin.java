package com.talania.races;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.profile.api.TalaniaApiRegistry;
import com.talania.core.runtime.TalaniaCoreRuntime;
import com.talania.core.TalaniaDevMode;
import com.talania.core.events.EventBus;
import com.talania.core.events.player.PromptRaceSelectionEvent;
import com.talania.core.utils.PlayerRefUtil;
import com.talania.core.module.ModuleHooks;
import com.talania.core.module.TalaniaModuleRegistry;
import com.talania.races.api.TalaniaApiImpl;
import com.talania.races.system.RaceConditionalEffectSystem;
import com.talania.races.ui.TalaniaRaceSelectionPage;

import javax.annotation.Nonnull;

/**
 * Races module plugin. Registers the Talania API implementation for races.
 */
public final class TalaniaRacesPlugin extends JavaPlugin {
    private final RaceService raceService = new RaceService();
    private RaceConditionalEffectSystem conditionalEffectSystem;
    private TalaniaApiImpl api;

    public TalaniaRacesPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.api = new TalaniaApiImpl(raceService);
        TalaniaApiRegistry.register(api);
        this.conditionalEffectSystem = new RaceConditionalEffectSystem(raceService);
        getEntityStoreRegistry().registerSystem(conditionalEffectSystem);
        EventBus.subscribe(PromptRaceSelectionEvent.class, this::handleRaceSelectionPrompt);
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
                if (!TalaniaDevMode.isEnabled()) {
                    return;
                }
                registry.registerModule("races", "Races", builder -> builder.section("main", "Races"));
            }

            @Override
            public void openDebugSection(String sectionId, PlayerRef playerRef,
                                         com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref,
                                         com.hypixel.hytale.component.Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store) {
                if (!TalaniaDevMode.isEnabled()) {
                    return;
                }
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
        if (!TalaniaDevMode.isEnabled()) {
            return;
        }
        if (playerRef == null || ref == null || store == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("com.talania.races.debug.dev.TalaniaRacesDebugMenuPage");
            java.lang.reflect.Method method = clazz.getDeclaredMethod("open",
                    com.hypixel.hytale.component.Ref.class,
                    com.hypixel.hytale.component.Store.class,
                    TalaniaRacesPlugin.class);
            method.invoke(null, ref, store, this);
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
            raceService.clearRace(playerId);
            return;
        }
        raceService.setRace(playerId, race);
    }

    private void handlePlayerDisconnect(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }
        if (conditionalEffectSystem != null) {
            conditionalEffectSystem.clear(playerRef.getUuid());
        }
        raceService.clearRace(playerRef.getUuid());
    }

    private void handleRaceSelectionPrompt(PromptRaceSelectionEvent event) {
        if (event == null || event.playerEntityRef() == null) {
            return;
        }
        TalaniaCoreRuntime core = TalaniaCoreRuntime.get();
        if (core == null) {
            return;
        }
        com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref =
                event.playerEntityRef();
        com.hypixel.hytale.component.Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store =
                ref.getStore();
        if (store == null) {
            return;
        }
        PlayerRef playerRef = PlayerRefUtil.resolve(ref, store);
        if (playerRef == null) {
            return;
        }
        java.util.UUID playerId = playerRef.getUuid();
        TalaniaPlayerProfile profile = core.profileRuntime().load(playerId);
        if (profile == null) {
            return;
        }
        if (!event.respec()) {
            RaceType existing = RaceType.fromId(profile.raceId());
            if (existing != null) {
                return;
            }
        } else {
            clearRace(profile, playerId, core);
        }
        TalaniaRaceSelectionPage.open(ref, store, this, event.respec());
    }

    private void clearRace(TalaniaPlayerProfile profile, java.util.UUID playerId, TalaniaCoreRuntime core) {
        if (profile == null || playerId == null || core == null) {
            return;
        }
        profile.setRaceId(null);
        core.profileRuntime().save(playerId);
        raceService.clearRace(playerId);
    }
}
