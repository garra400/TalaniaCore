package com.talania.core.events.entity.npc;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.events.EventBus;
import com.talania.core.events.entity.NpcDeathEvent;
import com.talania.core.events.entity.PlayerDeathEvent;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

/**
 * ECS system that publishes NPC and player death events.
 */
public final class NpcDeathEventSystem extends EntityTickingSystem<EntityStore> {
    private final ComponentType<EntityStore, DeathComponent> deathType = DeathComponent.getComponentType();
    private final ComponentType<EntityStore, NpcDeathHandledComponent> handledType;
    private final Query<EntityStore> query;
    private final Set<Dependency<EntityStore>> dependencies;

    public NpcDeathEventSystem(@Nonnull ComponentType<EntityStore, NpcDeathHandledComponent> handledType) {
        this.handledType = handledType;
        this.query = Query.and(
                Archetype.of(deathType),
                Query.not(Archetype.of(handledType))
        );
        this.dependencies = Set.of(
                new SystemGroupDependency<>(Order.AFTER, DamageModule.get().getInspectDamageGroup())
        );
    }

    @Override
    public Query<EntityStore> getQuery() {
        return query;
    }

    @Override
    @Nonnull
    public Set<Dependency<EntityStore>> getDependencies() {
        return dependencies;
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        DeathComponent death = chunk.getComponent(index, deathType);
        if (death == null) {
            return;
        }
        Damage damage = death.getDeathInfo();
        if (damage == null) {
            return;
        }
        Ref<EntityStore> targetRef = chunk.getReferenceTo(index);
        UUID targetUuid = uuidFor(store, targetRef);
        if (isPlayer(store, targetRef)) {
            EventBus.publish(new PlayerDeathEvent(targetRef, targetUuid, damage, death));
        } else {
            EventBus.publish(new NpcDeathEvent(targetRef, targetUuid, damage, death));
        }
        commandBuffer.ensureAndGetComponent(targetRef, handledType);
    }

    private static boolean isPlayer(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (store == null || ref == null || !ref.isValid()) {
            return false;
        }
        return store.getComponent(ref, Player.getComponentType()) != null;
    }

    private static UUID uuidFor(Store<EntityStore> store, Ref<EntityStore> ref) {
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        return uuidComponent == null ? null : uuidComponent.getUuid();
    }
}
