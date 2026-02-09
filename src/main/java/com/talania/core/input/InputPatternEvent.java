package com.talania.core.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Emitted when an input pattern is detected.
 */
public final class InputPatternEvent {
    private final InputPattern pattern;
    private final UUID playerId;
    private final long timestamp;
    private final Ref<EntityStore> ref;
    private final Store<EntityStore> store;
    private final InputSnapshot snapshot;

    public InputPatternEvent(InputPattern pattern,
                             UUID playerId,
                             long timestamp,
                             Ref<EntityStore> ref,
                             Store<EntityStore> store,
                             InputSnapshot snapshot) {
        this.pattern = pattern;
        this.playerId = playerId;
        this.timestamp = timestamp;
        this.ref = ref;
        this.store = store;
        this.snapshot = snapshot;
    }

    public InputPattern pattern() {
        return pattern;
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

    public InputSnapshot snapshot() {
        return snapshot;
    }
}
