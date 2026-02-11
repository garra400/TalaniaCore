package com.talania.core.events.entity.npc;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Marker component to ensure NPC deaths are handled only once.
 */
public final class NpcDeathHandledComponent implements Component<EntityStore> {
    @Override
    public Component<EntityStore> clone() {
        return new NpcDeathHandledComponent();
    }
}
