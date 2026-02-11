package com.talania.core.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Event published when an action key is triggered.
 */
public final class InputActionEvent {
    private final InputAction action;
    private final UUID playerId;
    private final long timestamp;
    private final Ref<EntityStore> ref;
    private final Store<EntityStore> store;

    public InputActionEvent(InputAction action,
                            UUID playerId,
                            long timestamp,
                            Ref<EntityStore> ref,
                            Store<EntityStore> store) {
        this.action = action;
        this.playerId = playerId;
        this.timestamp = timestamp;
        this.ref = ref;
        this.store = store;
    }

    public InputAction action() {
        return action;
    }

    public UUID playerId() {
        return playerId;
    }

    public long timestamp() {
        return timestamp;
    }

    public Ref<EntityStore> ref() {
        return ref;
    }

    public Store<EntityStore> store() {
        return store;
    }
}
