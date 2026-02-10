package com.talania.core.events.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Event published when a player entity dies.
 */
public final class PlayerDeathEvent {
    private final Ref<EntityStore> targetRef;
    private final UUID targetUuid;
    private final Damage damage;
    private final DeathComponent death;

    public PlayerDeathEvent(Ref<EntityStore> targetRef, UUID targetUuid, Damage damage, DeathComponent death) {
        this.targetRef = targetRef;
        this.targetUuid = targetUuid;
        this.damage = damage;
        this.death = death;
    }

    public Ref<EntityStore> targetRef() {
        return targetRef;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public Damage damage() {
        return damage;
    }

    public DeathComponent death() {
        return death;
    }
}
