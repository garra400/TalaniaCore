package com.talania.core.movement;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies movement-related stats to MovementSettings (move speed, jump height).
 */
public final class MovementStatSystem extends EntityTickingSystem<EntityStore> {
    private static final float EPSILON = 0.0005f;
    private final Map<UUID, AppliedMovement> applied = new HashMap<>();

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Archetype.of(
                UUIDComponent.getComponentType(),
                MovementManager.getComponentType()
        ));
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }
        UUID uuid = uuidComponent.getUuid();
        if (uuid == null) {
            return;
        }
        float moveSpeed = StatsManager.getStat(uuid, StatType.MOVE_SPEED);
        float jumpHeight = StatsManager.getStat(uuid, StatType.JUMP_HEIGHT);
        AppliedMovement prev = applied.get(uuid);
        boolean moveChanged = prev == null || Math.abs(prev.moveSpeed - moveSpeed) > EPSILON;
        boolean jumpChanged = prev == null || Math.abs(prev.jumpHeight - jumpHeight) > EPSILON;
        if (!moveChanged && !jumpChanged) {
            return;
        }
        if (moveChanged) {
            MovementStatUtil.applyMoveSpeedMultiplier(ref, store, moveSpeed);
        }
        if (jumpChanged) {
            MovementStatUtil.applyJumpHeightMultiplier(ref, store, jumpHeight);
        }
        applied.put(uuid, new AppliedMovement(moveSpeed, jumpHeight));
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        applied.remove(playerId);
    }

    private static final class AppliedMovement {
        private final float moveSpeed;
        private final float jumpHeight;

        private AppliedMovement(float moveSpeed, float jumpHeight) {
            this.moveSpeed = moveSpeed;
            this.jumpHeight = jumpHeight;
        }
    }
}
