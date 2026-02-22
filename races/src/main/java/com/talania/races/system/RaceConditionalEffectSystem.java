package com.talania.races.system;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatModifier;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;
import com.talania.core.utils.world.WorldContextUtil;
import com.talania.races.RaceService;
import com.talania.races.RaceType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies race conditional effects that depend on world state (day/night) or runtime toggles.
 */
public final class RaceConditionalEffectSystem extends EntityTickingSystem<EntityStore> {
    private static final String NIGHTWALKER_NIGHT_SOURCE = "race:nightwalker:night";
    private static final String NIGHTWALKER_SUN_SOURCE = "race:nightwalker:sun";
    private static final String STARBORN_HEAL_SOURCE = "race:starborn:healing";

    private final RaceService raceService;
    private final Map<UUID, RaceState> states = new HashMap<>();

    public RaceConditionalEffectSystem(RaceService raceService) {
        this.raceService = raceService;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Archetype.of(UUIDComponent.getComponentType()));
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        if (raceService == null) {
            return;
        }
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }
        UUID uuid = uuidComponent.getUuid();
        if (uuid == null) {
            return;
        }
        RaceType race = raceService.getRace(uuid);
        if (race == null) {
            RaceState existing = states.remove(uuid);
            if (existing != null) {
                clearConditionalModifiers(uuid, existing);
            }
            return;
        }
        RaceState state = states.computeIfAbsent(uuid, ignored -> new RaceState());
        if (state.race != race) {
            clearConditionalModifiers(uuid, state);
            state.race = race;
        }
        if (race == RaceType.NIGHTWALKER) {
            boolean isNight = WorldContextUtil.isNight(store);
            boolean isDay = !isNight;
            if (isNight && !state.nightSpeedApplied) {
                StatsManager.addModifier(uuid, new StatModifier(
                        NIGHTWALKER_NIGHT_SOURCE, StatType.MOVE_SPEED, 1.15f, StatModifier.Operation.MULTIPLY_TOTAL));
                state.nightSpeedApplied = true;
            } else if (!isNight && state.nightSpeedApplied) {
                StatsManager.getOrCreate(uuid).removeModifiersBySource(NIGHTWALKER_NIGHT_SOURCE);
                state.nightSpeedApplied = false;
            }

            if (isDay && !state.sunRegenApplied) {
                StatsManager.addModifier(uuid, new StatModifier(
                        NIGHTWALKER_SUN_SOURCE, StatType.HEALTH_REGEN, 0.90f, StatModifier.Operation.MULTIPLY_TOTAL));
                state.sunRegenApplied = true;
            } else if (!isDay && state.sunRegenApplied) {
                StatsManager.getOrCreate(uuid).removeModifiersBySource(NIGHTWALKER_SUN_SOURCE);
                state.sunRegenApplied = false;
            }
        } else {
            if (state.nightSpeedApplied) {
                StatsManager.getOrCreate(uuid).removeModifiersBySource(NIGHTWALKER_NIGHT_SOURCE);
                state.nightSpeedApplied = false;
            }
            if (state.sunRegenApplied) {
                StatsManager.getOrCreate(uuid).removeModifiersBySource(NIGHTWALKER_SUN_SOURCE);
                state.sunRegenApplied = false;
            }
        }

        if (race == RaceType.STARBORN) {
            if (!state.starbornHealingApplied) {
                StatsManager.addModifier(uuid, new StatModifier(
                        STARBORN_HEAL_SOURCE, StatType.HEALING_RECEIVED_MULT, 0.5f,
                        StatModifier.Operation.MULTIPLY_TOTAL));
                state.starbornHealingApplied = true;
            }
        } else if (state.starbornHealingApplied) {
            StatsManager.getOrCreate(uuid).removeModifiersBySource(STARBORN_HEAL_SOURCE);
            state.starbornHealingApplied = false;
        }
    }

    private void clearConditionalModifiers(UUID uuid, RaceState state) {
        if (uuid == null || state == null) {
            return;
        }
        EntityStats stats = StatsManager.getOrCreate(uuid);
        stats.removeModifiersBySource(NIGHTWALKER_NIGHT_SOURCE);
        stats.removeModifiersBySource(NIGHTWALKER_SUN_SOURCE);
        stats.removeModifiersBySource(STARBORN_HEAL_SOURCE);
        state.nightSpeedApplied = false;
        state.sunRegenApplied = false;
        state.starbornHealingApplied = false;
    }

    public void clear(UUID uuid) {
        if (uuid == null) {
            return;
        }
        RaceState state = states.remove(uuid);
        if (state != null) {
            clearConditionalModifiers(uuid, state);
        }
    }

    private static final class RaceState {
        private RaceType race;
        private boolean nightSpeedApplied;
        private boolean sunRegenApplied;
        private boolean starbornHealingApplied;
    }
}
