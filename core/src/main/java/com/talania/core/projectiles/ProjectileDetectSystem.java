package com.talania.core.projectiles;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.projectile.component.PredictedProjectile;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.events.EventBus;
import com.talania.core.events.entity.ProjectileFiredEvent;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.util.UUID;

/**
 * Detects predicted projectiles and emits {@link ProjectileFiredEvent}.
 */
public final class ProjectileDetectSystem extends EntityTickingSystem<EntityStore> {
    private static final long SEEN_TTL_MS = 5_000L;
    private static final long PRUNE_INTERVAL_MS = 10_000L;

    private static final Query<EntityStore> QUERY = Query.and(
            Archetype.of(PredictedProjectile.getComponentType()),
            Archetype.of(TransformComponent.getComponentType())
    );

    private final Int2LongOpenHashMap seenProjectiles = new Int2LongOpenHashMap();
    private long lastPruneAt;

    public ProjectileDetectSystem() {
        this.seenProjectiles.defaultReturnValue(Long.MIN_VALUE);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) {
            return;
        }
        int key = System.identityHashCode(ref);
        long now = System.currentTimeMillis();
        if (seenProjectiles.get(key) > Long.MIN_VALUE) {
            return;
        }
        seenProjectiles.put(key, now);
        if (now - lastPruneAt > PRUNE_INTERVAL_MS) {
            prune(now);
        }
        PredictedProjectile predicted = store.getComponent(ref, PredictedProjectile.getComponentType());
        if (predicted == null || predicted.getUuid() == null) {
            return;
        }
        UUID shooterUuid = ProjectileOwnerResolver.resolveShooterUuid(predicted.getUuid(), ref, store);
        EventBus.publish(new ProjectileFiredEvent(shooterUuid, predicted.getUuid(), ref, true));
    }

    private void prune(long now) {
        lastPruneAt = now;
        long cutoff = now - SEEN_TTL_MS;
        IntIterator it = seenProjectiles.keySet().iterator();
        while (it.hasNext()) {
            int key = it.nextInt();
            if (seenProjectiles.get(key) < cutoff) {
                it.remove();
            }
        }
    }
}
