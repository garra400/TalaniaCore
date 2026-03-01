package com.talania.core.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Event published to prompt a player to select a class.
 */
public final class PromptClassSelectionEvent {
    private final Ref<EntityStore> playerEntityRef;
    private final boolean respec;

    public PromptClassSelectionEvent(Ref<EntityStore> playerEntityRef, boolean respec) {
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
