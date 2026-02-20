package com.talania.core.debug;

import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatModifier;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dev-only stat modifier storage for per-player debug adjustments.
 */
public final class DebugStatModifierService {
    private static final String SOURCE_PREFIX = "debug:stat:";
    private static final float MULTIPLIER_EPS = 0.0001f;
    private final Map<UUID, PlayerState> states = new ConcurrentHashMap<>();

    private static final class PlayerState {
        private boolean enabled = true;
        private final EnumMap<StatType, Float> addDeltas = new EnumMap<>(StatType.class);
        private final EnumMap<StatType, Float> multipliers = new EnumMap<>(StatType.class);
    }

    public void ensurePlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }
        states.computeIfAbsent(playerId, id -> new PlayerState()).enabled = true;
    }

    public boolean isEnabled(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        return states.computeIfAbsent(playerId, id -> new PlayerState()).enabled;
    }

    public boolean toggle(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        state.enabled = true;
        return true;
    }

    public void setEnabled(UUID playerId, boolean enabled) {
        if (playerId == null) {
            return;
        }
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        state.enabled = true;
    }

    public float getDelta(UUID playerId, StatType stat) {
        if (playerId == null || stat == null) {
            return 0.0f;
        }
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        return state.addDeltas.getOrDefault(stat, 0.0f);
    }

    public void setDelta(UUID playerId, StatType stat, float value) {
        if (playerId == null || stat == null) {
            return;
        }
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        if (Math.abs(value) <= 0.0001f) {
            state.addDeltas.remove(stat);
        } else {
            state.addDeltas.put(stat, value);
        }
    }

    public void addDelta(UUID playerId, StatType stat, float delta) {
        if (playerId == null || stat == null) {
            return;
        }
        float next = getDelta(playerId, stat) + delta;
        setDelta(playerId, stat, next);
    }

    public float getMultiplier(UUID playerId, StatType stat) {
        if (playerId == null || stat == null) {
            return 1.0f;
        }
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        return state.multipliers.getOrDefault(stat, 1.0f);
    }

    public void setMultiplier(UUID playerId, StatType stat, float value) {
        if (playerId == null || stat == null) {
            return;
        }
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        float clamped = Math.max(0.01f, value);
        if (Math.abs(clamped - 1.0f) <= MULTIPLIER_EPS) {
            state.multipliers.remove(stat);
        } else {
            state.multipliers.put(stat, clamped);
        }
    }

    public void addMultiplier(UUID playerId, StatType stat, float delta) {
        if (playerId == null || stat == null) {
            return;
        }
        float next = getMultiplier(playerId, stat) + delta;
        setMultiplier(playerId, stat, next);
    }

    public float baseValue(UUID playerId, StatType stat) {
        if (playerId == null || stat == null) {
            return stat != null ? stat.getDefaultValue() : 0.0f;
        }
        EntityStats stats = StatsManager.get(playerId);
        if (stats == null) {
            return stat.getDefaultValue();
        }
        EntityStats copy = stats.copy();
        removeDebugModifiers(copy);
        return copy.get(stat);
    }

    public void applyToStats(UUID playerId, EntityStats stats) {
        if (playerId == null || stats == null) {
            return;
        }
        removeDebugModifiers(stats);
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        if (!state.enabled) {
            stats.recalculate();
            return;
        }
        for (Map.Entry<StatType, Float> entry : state.addDeltas.entrySet()) {
            float value = entry.getValue();
            if (Math.abs(value) <= 0.0001f) {
                continue;
            }
            StatType stat = entry.getKey();
            stats.addModifier(StatModifier.add(sourceFor(stat, "add"), stat, value));
        }
        for (Map.Entry<StatType, Float> entry : state.multipliers.entrySet()) {
            float value = entry.getValue();
            if (Math.abs(value - 1.0f) <= MULTIPLIER_EPS) {
                continue;
            }
            StatType stat = entry.getKey();
            stats.addModifier(StatModifier.multiplyTotal(sourceFor(stat, "mult"), stat, value));
        }
        stats.recalculate();
    }

    public boolean hasActiveModifiers(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        PlayerState state = states.get(playerId);
        if (state == null) {
            return false;
        }
        return state.enabled && (!state.addDeltas.isEmpty() || !state.multipliers.isEmpty());
    }

    public void reset(UUID playerId) {
        if (playerId == null) {
            return;
        }
        PlayerState state = states.computeIfAbsent(playerId, id -> new PlayerState());
        state.addDeltas.clear();
        state.multipliers.clear();
        state.enabled = true;
    }

    private void removeDebugModifiers(EntityStats stats) {
        for (StatType stat : StatType.values()) {
            stats.removeModifier(SOURCE_PREFIX + stat.getId());
            stats.removeModifier(sourceFor(stat, "add"));
            stats.removeModifier(sourceFor(stat, "mult"));
        }
    }

    private String sourceFor(StatType stat, String op) {
        return SOURCE_PREFIX + op + ":" + stat.getId();
    }
}
