package com.talania.core.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Event published to prompt a player to select a race.
 */
public final class PromptRaceSelectionEvent {
    private final Ref<EntityStore> playerEntityRef;
    private final boolean respec;

    public PromptRaceSelectionEvent(Ref<EntityStore> playerEntityRef, boolean respec) {
        this.playerEntityRef = playerEntityRef;
        this.respec = respec;
    }

    public Ref<EntityStore> playerEntityRef() {
        return playerEntityRef;
    }

    public boolean respec() {
        return respec;
    }
}
