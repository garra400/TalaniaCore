package com.talania.core.movement;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Helpers for applying movement-related stat changes.
 */
public final class MovementStatUtil {
    private MovementStatUtil() {}

    /**
     * Apply a jump height multiplier by adjusting MovementSettings.jumpForce.
     * Use multiplier 1.0f to restore defaults.
     */
    public static void applyJumpHeightMultiplier(Ref<EntityStore> ref, Store<EntityStore> store, float multiplier) {
        if (ref == null || store == null) {
            return;
        }
        MovementManager movementManager = (MovementManager) store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager == null) {
            return;
        }
        MovementSettings current = movementManager.getSettings();
        MovementSettings defaults = movementManager.getDefaultSettings();
        if (current == null || defaults == null) {
            return;
        }
        current.jumpForce = defaults.jumpForce * Math.max(0.0f, multiplier);
        pushMovementSettings(ref, store, movementManager);
    }

    private static void pushMovementSettings(Ref<EntityStore> ref, Store<EntityStore> store,
                                             MovementManager movementManager) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        PlayerRef playerRef = player.getPlayerRef();
        if (playerRef == null) {
            return;
        }
        movementManager.update(playerRef.getPacketHandler());
    }
}
