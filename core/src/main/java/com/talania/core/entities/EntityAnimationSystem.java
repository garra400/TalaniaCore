package com.talania.core.entities;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * ECS system that ticks {@link EntityAnimationManager} each frame.
 */
public final class EntityAnimationSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Archetype.of(UUIDComponent.getComponentType()));
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        // Only tick once per frame using index 0 of any chunk.
        if (index != 0) {
            return;
        }
        EntityAnimationManager.get().tick(store, System.currentTimeMillis());
    }
}
