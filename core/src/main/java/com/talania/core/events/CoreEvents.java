package com.talania.core.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.events.player.PromptClassSelectionEvent;
import com.talania.core.events.player.PromptRaceSelectionEvent;

/**
 * Convenience helpers for publishing common Talania events.
 */
public final class CoreEvents {

    private CoreEvents() {}

    public static void promptClassSelection(PlayerRef playerRef, Ref<EntityStore> playerEntityRef, boolean respec) {
        EventBus.publish(new PromptClassSelectionEvent(playerRef, playerEntityRef, respec));
    }

    public static void promptRaceSelection(PlayerRef playerRef, Ref<EntityStore> playerEntityRef, boolean respec) {
        EventBus.publish(new PromptRaceSelectionEvent(playerRef, playerEntityRef, respec));
    }
}
