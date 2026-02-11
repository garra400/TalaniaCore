package com.talania.core.utils.input;

import com.talania.core.input.InputAction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Simple key binding registry for action keys (E/R by default).
 */
public final class KeyBindings {
    private final Map<InputAction, String> bindings = new EnumMap<>(InputAction.class);

    public KeyBindings() {
        bindings.put(InputAction.E, "E");
        bindings.put(InputAction.R, "R");
    }

    public String get(InputAction action) {
        return bindings.get(action);
    }

    public void set(InputAction action, String key) {
        if (action == null || key == null || key.isBlank()) {
            return;
        }
        bindings.put(action, key);
    }

    public Map<InputAction, String> asMap() {
        return Collections.unmodifiableMap(bindings);
    }
}
