package com.talania.core.input;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * ECS system that forwards movement state changes into {@link InputPatternTracker}.
 */
public final class InputPatternMovementSystem extends EntityTickingSystem<EntityStore> {
    private final InputPatternTracker tracker;

    private static final Query<EntityStore> QUERY = Query.and(
            Archetype.of(Player.getComponentType()),
            Archetype.of(UUIDComponent.getComponentType()),
            Archetype.of(MovementStatesComponent.getComponentType())
    );

    public InputPatternMovementSystem(InputPatternTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        if (tracker == null || store == null) {
            return;
        }
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) {
            return;
        }
        tracker.handleMovement(ref, store);
    }
}
