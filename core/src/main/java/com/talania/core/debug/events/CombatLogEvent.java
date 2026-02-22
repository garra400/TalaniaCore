package com.talania.core.debug.events;

import com.talania.core.debug.combat.CombatLogEntry;

/**
 * Event published when a combat log entry is created.
 */
public final class CombatLogEvent {
    private final CombatLogEntry entry;

    public CombatLogEvent(CombatLogEntry entry) {
        this.entry = entry;
    }

    public CombatLogEntry entry() {
        return entry;
    }
}
