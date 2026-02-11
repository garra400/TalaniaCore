package com.talania.core.abilities;

import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;

/**
 * Defines a status-effect icon to display during cooldowns.
 */
public final class CooldownEffect {
    private final String effectId;
    private final OverlapBehavior overlapBehavior;

    public CooldownEffect(String effectId, OverlapBehavior overlapBehavior) {
        this.effectId = effectId;
        this.overlapBehavior = overlapBehavior != null ? overlapBehavior : OverlapBehavior.OVERWRITE;
    }

    public String effectId() {
        return effectId;
    }

    public OverlapBehavior overlapBehavior() {
        return overlapBehavior;
    }
}
