package com.talania.core.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Event published to prompt a player to select a class.
 */
public final class PromptClassSelectionEvent {
    private final PlayerRef playerRef;
    private final Ref<EntityStore> playerEntityRef;
    private final boolean respec;

    public PromptClassSelectionEvent(PlayerRef playerRef, Ref<EntityStore> playerEntityRef, boolean respec) {
        this.playerRef = playerRef;
        this.playerEntityRef = playerEntityRef;
        this.respec = respec;
    }

    public PlayerRef playerRef() {
        return playerRef;
    }

    public Ref<EntityStore> playerEntityRef() {
        return playerEntityRef;
    }

    public boolean respec() {
        return respec;
    }
}
