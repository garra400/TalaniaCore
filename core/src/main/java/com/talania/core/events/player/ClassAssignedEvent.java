package com.talania.core.events.player;

import java.util.UUID;

/**
 * Event published when a player is assigned (or changes) their RPG class.
 * Published by TalaniaClassRPG, consumed by any mod that needs to react to class changes.
 */
public final class ClassAssignedEvent {

    private final UUID playerUuid;
    private final String newClassId;
    private final String previousClassId;   // null if first-time assignment

    public ClassAssignedEvent(UUID playerUuid, String newClassId, String previousClassId) {
        this.playerUuid = playerUuid;
        this.newClassId = newClassId;
        this.previousClassId = previousClassId;
    }

    public UUID playerUuid() { return playerUuid; }
    public String newClassId() { return newClassId; }
    public String previousClassId() { return previousClassId; }
    public boolean isFirstAssignment() { return previousClassId == null; }
}
