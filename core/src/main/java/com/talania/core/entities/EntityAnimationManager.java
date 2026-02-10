package com.talania.core.entities;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central manager for timed entity animation effects.
 *
 * <p>Register effects via {@link #add}. The manager will call start/tick/stop
 * and remove finished effects automatically.</p>
 */
public final class EntityAnimationManager {
    private final List<EntityAnimationEffect> effects = new CopyOnWriteArrayList<>();

    private static final EntityAnimationManager INSTANCE = new EntityAnimationManager();

    public static EntityAnimationManager get() {
        return INSTANCE;
    }

    private EntityAnimationManager() {}

    /**
     * Register and start an effect immediately.
     */
    public void add(EntityAnimationEffect effect, Store<EntityStore> store, long nowMs) {
        if (effect == null || store == null) {
            return;
        }
        effect.start(store, nowMs);
        effects.add(effect);
    }

    /**
     * Update all effects and remove finished ones.
     */
    public void tick(Store<EntityStore> store, long nowMs) {
        if (store == null || effects.isEmpty()) {
            return;
        }
        for (EntityAnimationEffect effect : effects) {
            if (effect == null) {
                effects.remove(null);
                continue;
            }
            effect.tick(store, nowMs);
            if (effect.isFinished(nowMs)) {
                effect.stop(store);
                effects.remove(effect);
            }
        }
    }

    /**
     * Remove a specific effect and stop it immediately.
     */
    public void remove(EntityAnimationEffect effect, Store<EntityStore> store) {
        if (effect == null) {
            return;
        }
        effects.remove(effect);
        if (store != null) {
            effect.stop(store);
        }
    }

    /**
     * Clear and stop all effects.
     */
    public void clear(Store<EntityStore> store) {
        for (EntityAnimationEffect effect : effects) {
            if (effect != null && store != null) {
                effect.stop(store);
            }
        }
        effects.clear();
    }
}
