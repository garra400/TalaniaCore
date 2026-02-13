package com.talania.core.input;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * ECS system that forwards block placement into {@link InputPatternTracker}.
 */
public final class InputPatternPlaceBlockSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private final InputPatternTracker tracker;

    public InputPatternPlaceBlockSystem(InputPatternTracker tracker) {
        super(PlaceBlockEvent.class);
        this.tracker = tracker;
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlaceBlockEvent event) {
        if (tracker == null) {
            return;
        }
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) {
            return;
        }
        ItemStack itemStack = event.getItemInHand();
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return;
        }
        tracker.handlePlaceBlock(ref, store, item);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Archetype.of(Player.getComponentType()),
                Archetype.of(UUIDComponent.getComponentType())
        );
    }
}
