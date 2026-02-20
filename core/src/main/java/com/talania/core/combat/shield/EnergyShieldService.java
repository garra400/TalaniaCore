package com.talania.core.combat.shield;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-entity energy shield values and recharge timing.
 */
public final class EnergyShieldService {
    private static final float EPS = 0.0001f;
    private static final Map<UUID, ShieldState> STATES = new ConcurrentHashMap<>();

    private EnergyShieldService() {
    }

    public enum Status {
        NONE,
        ACTIVE,
        RECHARGING,
        DEPLETED
    }

    public static float getCurrent(UUID entityId) {
        ShieldState state = STATES.get(entityId);
        return state == null ? 0.0f : state.current;
    }

    public static float applyDamage(UUID entityId, float amount, float maxShield) {
        if (entityId == null || amount <= 0.0f) {
            return amount;
        }
        if (maxShield <= 0.0f) {
            ShieldState existing = STATES.get(entityId);
            if (existing != null) {
                existing.current = 0.0f;
                existing.lastDamageAt = System.currentTimeMillis();
            }
            return amount;
        }
        ShieldState state = STATES.computeIfAbsent(entityId, id -> new ShieldState());
        if (!state.initialized) {
            state.current = maxShield;
            state.initialized = true;
        }
        if (state.current > maxShield + EPS) {
            state.current = maxShield;
        }
        long now = System.currentTimeMillis();
        state.lastDamageAt = now;
        float absorbed = Math.min(amount, state.current);
        state.current = Math.max(0.0f, state.current - absorbed);
        return amount - absorbed;
    }

    public static void tick(UUID entityId, float deltaSeconds, float maxShield,
                            float rechargePerSecond, float rechargeDelaySeconds) {
        if (entityId == null) {
            return;
        }
        if (maxShield <= 0.0f) {
            STATES.remove(entityId);
            return;
        }
        ShieldState state = STATES.computeIfAbsent(entityId, id -> new ShieldState());
        if (!state.initialized) {
            state.current = maxShield;
            state.initialized = true;
        }
        if (state.current > maxShield + EPS) {
            state.current = maxShield;
        }
        if (rechargePerSecond <= EPS) {
            return;
        }
        if (state.current >= maxShield - EPS) {
            return;
        }
        long now = System.currentTimeMillis();
        if (state.lastDamageAt > 0L) {
            long delayMs = (long) (Math.max(0.0f, rechargeDelaySeconds) * 1000.0f);
            if (now - state.lastDamageAt < delayMs) {
                return;
            }
        }
        float next = state.current + rechargePerSecond * Math.max(0.0f, deltaSeconds);
        state.current = Math.min(maxShield, next);
    }

    public static Status getStatus(UUID entityId, float maxShield, float rechargePerSecond, float rechargeDelaySeconds) {
        if (entityId == null || maxShield <= 0.0f) {
            return Status.NONE;
        }
        ShieldState state = STATES.get(entityId);
        float current = state == null ? 0.0f : state.current;
        if (current <= EPS) {
            return Status.DEPLETED;
        }
        if (current >= maxShield - EPS) {
            return Status.ACTIVE;
        }
        if (rechargePerSecond <= EPS) {
            return Status.DEPLETED;
        }
        if (state == null || state.lastDamageAt <= 0L) {
            return Status.RECHARGING;
        }
        long now = System.currentTimeMillis();
        long delayMs = (long) (Math.max(0.0f, rechargeDelaySeconds) * 1000.0f);
        if (now - state.lastDamageAt < delayMs) {
            return Status.DEPLETED;
        }
        return Status.RECHARGING;
    }

    public static void clear(UUID entityId) {
        if (entityId == null) {
            return;
        }
        STATES.remove(entityId);
    }

    private static final class ShieldState {
        private float current;
        private long lastDamageAt;
        private boolean initialized;
    }
}
