package com.talania.core.events.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Event published when a projectile launch is detected.
 */
public final class ProjectileFiredEvent {
    private final UUID shooterUuid;
    private final UUID predictedUuid;
    private final Ref<EntityStore> projectileRef;
    private final boolean predicted;

    public ProjectileFiredEvent(UUID shooterUuid, UUID predictedUuid,
                                Ref<EntityStore> projectileRef, boolean predicted) {
        this.shooterUuid = shooterUuid;
        this.predictedUuid = predictedUuid;
        this.projectileRef = projectileRef;
        this.predicted = predicted;
    }

    public UUID shooterUuid() {
        return shooterUuid;
    }

    public UUID predictedUuid() {
        return predictedUuid;
    }

    public Ref<EntityStore> projectileRef() {
        return projectileRef;
    }

    public boolean predicted() {
        return predicted;
    }
}
