package com.talania.core.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.events.player.PromptClassSelectionEvent;
import com.talania.core.events.player.PromptRaceSelectionEvent;

/**
 * Convenience helpers for publishing common Talania events.
 */
public final class CoreEvents {

    private CoreEvents() {}

    public static void promptClassSelection(Ref<EntityStore> playerEntityRef, boolean respec) {
        EventBus.publish(new PromptClassSelectionEvent(playerEntityRef, respec));
    }

    public static void promptRaceSelection(Ref<EntityStore> playerEntityRef, boolean respec) {
        EventBus.publish(new PromptRaceSelectionEvent(playerEntityRef, respec));
    }
}
