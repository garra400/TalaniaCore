package com.talania.core.debug.dev;

import com.talania.core.config.ConfigManager;
import com.talania.core.debug.DebugStatModifierService;
import com.talania.core.stats.StatType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dev-only persistence for per-player debug stat modifiers.
 */
public final class DebugStatModifierStore {
    private static final String FILE = "debug_stat_modifiers_dev.json";
    private static final float EPS = 0.0001f;
    private static DebugStatModifierStore INSTANCE;

    private Map<String, PlayerState> players = new HashMap<>();

    private static final class PlayerState {
        private Map<String, Float> add = new HashMap<>();
        private Map<String, Float> mult = new HashMap<>();
    }

    public static DebugStatModifierStore load() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        if (!ConfigManager.isInitialized()) {
            INSTANCE = new DebugStatModifierStore();
            return INSTANCE;
        }
        DebugStatModifierStore loaded = ConfigManager.load(FILE, DebugStatModifierStore.class);
        INSTANCE = loaded != null ? loaded : new DebugStatModifierStore();
        if (INSTANCE.players == null) {
            INSTANCE.players = new HashMap<>();
        }
        return INSTANCE;
    }

    public void applyTo(DebugStatModifierService service, UUID playerId) {
        if (service == null || playerId == null) {
            return;
        }
        PlayerState state = players.get(playerId.toString());
        if (state == null) {
            return;
        }
        service.reset(playerId);
        if (state.add != null) {
            for (Map.Entry<String, Float> entry : state.add.entrySet()) {
                StatType stat = StatType.fromId(entry.getKey());
                if (stat != null && entry.getValue() != null) {
                    service.setDelta(playerId, stat, entry.getValue());
                }
            }
        }
        if (state.mult != null) {
            for (Map.Entry<String, Float> entry : state.mult.entrySet()) {
                StatType stat = StatType.fromId(entry.getKey());
                if (stat != null && entry.getValue() != null) {
                    service.setMultiplier(playerId, stat, entry.getValue());
                }
            }
        }
    }

    public void saveFrom(DebugStatModifierService service, UUID playerId) {
        if (service == null || playerId == null) {
            return;
        }
        PlayerState state = new PlayerState();
        for (StatType stat : StatType.values()) {
            float add = service.getDelta(playerId, stat);
            if (Math.abs(add) > EPS) {
                state.add.put(stat.getId(), add);
            }
            float mult = service.getMultiplier(playerId, stat);
            if (Math.abs(mult - 1.0f) > EPS) {
                state.mult.put(stat.getId(), mult);
            }
        }
        if (state.add.isEmpty() && state.mult.isEmpty()) {
            players.remove(playerId.toString());
        } else {
            players.put(playerId.toString(), state);
        }
        save();
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        players.remove(playerId.toString());
        save();
    }

    private void save() {
        if (!ConfigManager.isInitialized()) {
            return;
        }
        ConfigManager.save(FILE, this);
    }
}
