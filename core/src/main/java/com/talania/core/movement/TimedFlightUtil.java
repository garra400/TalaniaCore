package com.talania.core.movement;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.MovementSettings;
import com.talania.core.utils.PlayerRefUtil;

/**
 * Helper for timed flight windows.
 *
 * <p>Caller is responsible for ticking and cleanup.</p>
 */
public final class TimedFlightUtil {
    private TimedFlightUtil() {}

    public static void start(State state, Ref<EntityStore> ref, Store<EntityStore> store, long durationMs) {
        if (state == null || ref == null || store == null || durationMs <= 0L) {
            return;
        }
        state.activeUntil = System.currentTimeMillis() + durationMs;
        enableFlight(ref, store, true);
        setMovementFlying(ref, store, true);
    }

    public static void tick(State state, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (state == null || ref == null || store == null) {
            return;
        }
        if (state.activeUntil <= 0L) {
            return;
        }
        if (System.currentTimeMillis() >= state.activeUntil) {
            stop(state, ref, store);
        } else {
            enableFlight(ref, store, true);
        }
    }

    public static void stop(State state, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (state == null) {
            return;
        }
        state.activeUntil = 0L;
        if (ref == null || store == null) {
            return;
        }
        enableFlight(ref, store, false);
        setMovementFlying(ref, store, false);
        Velocity velocity = store.getComponent(ref, Velocity.getComponentType());
        if (velocity != null && velocity.getY() > -0.5F) {
            velocity.setY(-0.5F);
        }
    }

    private static void enableFlight(Ref<EntityStore> ref, Store<EntityStore> store, boolean enabled) {
        MovementManager movementManager = store.getComponent(ref, MovementManager.getComponentType());
        if (movementManager == null) {
            return;
        }
        MovementSettings current = movementManager.getSettings();
        MovementSettings defaults = movementManager.getDefaultSettings();
        if (current == null || defaults == null) {
            return;
        }
        current.canFly = enabled ? true : defaults.canFly;
        if (!enabled) {
            current.horizontalFlySpeed = defaults.horizontalFlySpeed;
            current.verticalFlySpeed = defaults.verticalFlySpeed;
        }
        pushMovementSettings(ref, store, movementManager);
    }

    private static void setMovementFlying(Ref<EntityStore> ref, Store<EntityStore> store, boolean enabled) {
        MovementStatesComponent movementStatesComponent =
                store.getComponent(ref, MovementStatesComponent.getComponentType());
        if (movementStatesComponent != null) {
            com.hypixel.hytale.protocol.MovementStates movementStates = movementStatesComponent.getMovementStates();
            if (movementStates != null) {
                com.hypixel.hytale.protocol.MovementStates updated =
                        new com.hypixel.hytale.protocol.MovementStates(movementStates);
                updated.flying = enabled;
                if (!enabled) {
                    updated.jumping = false;
                    updated.gliding = false;
                }
                movementStatesComponent.setMovementStates(updated);
                movementStatesComponent.setSentMovementStates(new com.hypixel.hytale.protocol.MovementStates(updated));
            }
        }
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            PlayerRef playerRef = PlayerRefUtil.resolve(ref, store);
            if (playerRef != null && playerRef.getPacketHandler() != null) {
                playerRef.getPacketHandler().writeNoCache(new SetMovementStates(new SavedMovementStates(enabled)));
            }
        }
    }

    private static void pushMovementSettings(Ref<EntityStore> ref, Store<EntityStore> store,
                                             MovementManager movementManager) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        PlayerRef playerRef = PlayerRefUtil.resolve(ref, store);
        if (playerRef == null) {
            return;
        }
        movementManager.update(playerRef.getPacketHandler());
    }

    public static final class State {
        private long activeUntil;
    }
}
