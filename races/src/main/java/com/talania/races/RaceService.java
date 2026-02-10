package com.talania.races;

import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatModifier;
import com.talania.core.stats.StatsManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies race stat modifiers to entities and tracks the assigned race.
 *
 * <p>This service does not persist data; it is a lightweight helper for
 * race assignment during runtime. Persistence should be handled by the
 * gameplay layer that owns player profiles.</p>
 */
public final class RaceService {
    private final Map<UUID, RaceType> assigned = new ConcurrentHashMap<>();

    /**
     * Get the assigned race for a player, if present.
     */
    public RaceType getRace(UUID entityId) {
        return entityId != null ? assigned.get(entityId) : null;
    }

    /**
     * Assign a race to a player and apply the race's base stat modifiers.
     */
    public void setRace(UUID entityId, RaceType race) {
        if (entityId == null || race == null) {
            return;
        }
        RaceType previous = assigned.put(entityId, race);
        EntityStats stats = StatsManager.getOrCreate(entityId);
        if (previous != null) {
            stats.removeModifiersBySource(previous.sourceKey());
        }
        List<StatModifier> modifiers = race.createBaseModifiers();
        for (StatModifier modifier : modifiers) {
            stats.addModifier(modifier);
        }
    }

    /**
     * Clear a player's race assignment and remove its modifiers.
     */
    public void clearRace(UUID entityId) {
        if (entityId == null) {
            return;
        }
        RaceType previous = assigned.remove(entityId);
        if (previous != null) {
            EntityStats stats = StatsManager.getOrCreate(entityId);
            stats.removeModifiersBySource(previous.sourceKey());
        }
    }
}
