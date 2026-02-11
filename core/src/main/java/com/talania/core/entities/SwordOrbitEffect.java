package com.talania.core.entities;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Entity animation effect that spawns orbiting swords for a fixed duration.
 */
public final class SwordOrbitEffect implements EntityAnimationEffect {
    private final Ref<EntityStore> targetRef;
    private final SwordOrbit orbit;
    private final long durationMs;

    private long endAtMs;
    private boolean invalidated;

    public SwordOrbitEffect(Ref<EntityStore> targetRef, SwordOrbit orbit, long durationMs) {
        this.targetRef = targetRef;
        this.orbit = orbit;
        this.durationMs = Math.max(0L, durationMs);
    }

    @Override
    public void start(Store<EntityStore> store, long nowMs) {
        if (orbit == null || store == null || targetRef == null || !targetRef.isValid()) {
            invalidated = true;
            return;
        }
        orbit.start(nowMs);
        orbit.spawn(targetRef, store);
        endAtMs = nowMs + durationMs;
    }

    @Override
    public void tick(Store<EntityStore> store, long nowMs) {
        if (orbit == null || store == null || targetRef == null || !targetRef.isValid()) {
            invalidated = true;
            return;
        }
        orbit.update(targetRef, store, nowMs);
    }

    @Override
    public void stop(Store<EntityStore> store) {
        if (orbit != null && store != null) {
            orbit.clear(store);
        }
    }

    @Override
    public boolean isFinished(long nowMs) {
        return invalidated || durationMs <= 0L || nowMs >= endAtMs;
    }
}
