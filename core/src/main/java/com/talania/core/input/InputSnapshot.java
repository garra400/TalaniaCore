package com.talania.core.input;

import com.hypixel.hytale.protocol.MouseButtonEvent;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;

/**
 * Snapshot of relevant input and player state at the time a pattern is detected.
 */
public final class InputSnapshot {
    private final com.hypixel.hytale.protocol.MovementStates movementStates;
    private final MouseButtonEvent mouseButton;
    private final Item itemInHand;
    private final float lookPitchRadians;

    public InputSnapshot(com.hypixel.hytale.protocol.MovementStates movementStates,
                         MouseButtonEvent mouseButton,
                         Item itemInHand,
                         float lookPitchRadians) {
        this.movementStates = movementStates;
        this.mouseButton = mouseButton;
        this.itemInHand = itemInHand;
        this.lookPitchRadians = lookPitchRadians;
    }

    public com.hypixel.hytale.protocol.MovementStates movementStates() {
        return movementStates;
    }

    public MouseButtonEvent mouseButton() {
        return mouseButton;
    }

    public Item itemInHand() {
        return itemInHand;
    }

    public float lookPitchRadians() {
        return lookPitchRadians;
    }
}
