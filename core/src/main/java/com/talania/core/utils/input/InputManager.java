package com.talania.core.utils.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.events.EventBus;
import com.talania.core.input.InputAction;
import com.talania.core.input.InputActionEvent;

import java.util.UUID;

/**
 * Dispatches action key events and provides access to configured bindings.
 */
public final class InputManager {
    private final KeyBindings keyBindings;

    public InputManager() {
        this(new KeyBindings());
    }

    public InputManager(KeyBindings keyBindings) {
        this.keyBindings = keyBindings == null ? new KeyBindings() : keyBindings;
    }

    public KeyBindings getKeyBindings() {
        return keyBindings;
    }

    /**
     * Trigger an action key event (no entity context).
     */
    public void trigger(UUID playerId, InputAction action) {
        trigger(playerId, action, null, null);
    }

    /**
     * Trigger an action key event with entity context.
     */
    public void trigger(UUID playerId, InputAction action, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (playerId == null || action == null) {
            return;
        }
        EventBus.publish(new InputActionEvent(action, playerId, System.currentTimeMillis(), ref, store));
    }
}
