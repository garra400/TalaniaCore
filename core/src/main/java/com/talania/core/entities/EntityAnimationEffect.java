package com.talania.core.entities;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Runtime effect that manages temporary entity animations.
 *
 * <p>Effects are started once, ticked until they expire, and then stopped.
 * The manager is responsible for calling {@link #start} and {@link #tick}.</p>
 */
public interface EntityAnimationEffect {

    /**
     * Called once when the effect is registered with the manager.
     */
    void start(Store<EntityStore> store, long nowMs);

    /**
     * Called every tick to update animation state.
     */
    void tick(Store<EntityStore> store, long nowMs);

    /**
     * Called when the effect expires or is removed.
     */
    void stop(Store<EntityStore> store);

    /**
     * Whether this effect has finished and should be removed.
     */
    boolean isFinished(long nowMs);
}
