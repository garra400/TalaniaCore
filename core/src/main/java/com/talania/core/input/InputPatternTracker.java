package com.talania.core.input;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.MouseButtonEvent;
import com.hypixel.hytale.protocol.MouseButtonState;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.talania.core.events.EventBus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects high-level input patterns and emits {@link InputPatternEvent}s.
 *
 * <p>This is optional infrastructure; it can be dropped if Hytale exposes
 * richer server input in the future.</p>
 */
public final class InputPatternTracker {
    private static final long DEFAULT_DOUBLE_TAP_MS = 500L;
    private static final double DEFAULT_LOOK_UP_DEGREES = 10.0;

    private final Map<UUID, PlayerInputState> states = new ConcurrentHashMap<>();
    private long doubleTapWindowMs = DEFAULT_DOUBLE_TAP_MS;
    private double lookUpDegrees = DEFAULT_LOOK_UP_DEGREES;

    public void setDoubleTapWindowMs(long windowMs) {
        this.doubleTapWindowMs = Math.max(50L, windowMs);
    }

    public void setLookUpDegrees(double degrees) {
        this.lookUpDegrees = Math.max(0.0, degrees);
    }

    public void handleMovement(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        UUID playerId = uuidFor(ref, store);
        if (playerId == null) {
            return;
        }
        MovementStatesComponent movementStatesComponent =
                (MovementStatesComponent) store.getComponent(ref, MovementStatesComponent.getComponentType());
        if (movementStatesComponent == null) {
            return;
        }
        com.hypixel.hytale.protocol.MovementStates current = movementStatesComponent.getMovementStates();
        PlayerInputState state = states.computeIfAbsent(playerId, ignored -> new PlayerInputState());

        if (!state.hasMovementStates) {
            state.lastMovementStates = new com.hypixel.hytale.protocol.MovementStates(current);
            state.hasMovementStates = true;
            return;
        }

        long now = System.currentTimeMillis();
        boolean crouchPressed = current.crouching && !state.lastMovementStates.crouching;
        boolean sprintPressed = current.sprinting && !state.lastMovementStates.sprinting;
        boolean jumpPressed = current.jumping && !state.lastMovementStates.jumping;

        if (crouchPressed) {
            if (state.lastCrouchTapAt != 0 && now - state.lastCrouchTapAt <= doubleTapWindowMs) {
                emit(InputPattern.DOUBLE_TAP_CROUCH, playerId, now, ref, store, current, null, null);
                state.lastCrouchTapAt = 0L;
            } else {
                state.lastCrouchTapAt = now;
            }
        }

        if (sprintPressed) {
            if (state.lastSprintTapAt != 0 && now - state.lastSprintTapAt <= doubleTapWindowMs) {
                emit(InputPattern.DOUBLE_TAP_SPRINT, playerId, now, ref, store, current, null, null);
                state.lastSprintTapAt = 0L;
            } else {
                state.lastSprintTapAt = now;
            }
        }

        if (jumpPressed && current.crouching) {
            emit(InputPattern.CROUCH_JUMP, playerId, now, ref, store, current, null, null);
        }

        if (jumpPressed && isLookingUp(ref, store)) {
            emit(InputPattern.JUMP_LOOK_UP, playerId, now, ref, store, current, null, null);
        }

        state.lastMovementStates = new com.hypixel.hytale.protocol.MovementStates(current);
    }

    public void handleMouseButton(Ref<EntityStore> ref, Store<EntityStore> store,
                                  MouseButtonEvent mouseButton, Item itemInHand) {
        if (ref == null || store == null || mouseButton == null) {
            return;
        }
        UUID playerId = uuidFor(ref, store);
        if (playerId == null) {
            return;
        }
        if (mouseButton.state != MouseButtonState.Pressed) {
            return;
        }
        long now = System.currentTimeMillis();
        PlayerInputState state = states.computeIfAbsent(playerId, ignored -> new PlayerInputState());
        com.hypixel.hytale.protocol.MovementStates movementStates = movementStates(store, ref);

        if (mouseButton.mouseButtonType == MouseButtonType.Right) {
            if (state.lastRightClickTapAt != 0 && now - state.lastRightClickTapAt <= doubleTapWindowMs) {
                emit(InputPattern.DOUBLE_TAP_RIGHT_CLICK, playerId, now, ref, store, movementStates, mouseButton, itemInHand);
                state.lastRightClickTapAt = 0L;
            } else {
                state.lastRightClickTapAt = now;
            }

            if (movementStates != null && movementStates.crouching) {
                emit(InputPattern.RIGHT_CLICK_CROUCH, playerId, now, ref, store, movementStates, mouseButton, itemInHand);
            }
        }
    }

    public void handlePlaceBlock(Ref<EntityStore> ref, Store<EntityStore> store, Item itemInHand) {
        if (ref == null || store == null || itemInHand == null) {
            return;
        }
        UUID playerId = uuidFor(ref, store);
        if (playerId == null) {
            return;
        }
        com.hypixel.hytale.protocol.MovementStates movementStates = movementStates(store, ref);
        if (movementStates != null && movementStates.crouching) {
            emit(InputPattern.PLACE_BLOCK_CROUCH, playerId, System.currentTimeMillis(),
                    ref, store, movementStates, null, itemInHand);
        }
    }

    public void clear(UUID playerId) {
        states.remove(playerId);
    }

    private void emit(InputPattern pattern, UUID playerId, long now,
                      Ref<EntityStore> ref, Store<EntityStore> store,
                      com.hypixel.hytale.protocol.MovementStates movementStates,
                      MouseButtonEvent mouseButton, Item itemInHand) {
        InputSnapshot snapshot = new InputSnapshot(movementStates, mouseButton, itemInHand, resolvePitchRadians(ref, store));
        EventBus.publish(new InputPatternEvent(pattern, playerId, now, ref, store, snapshot));
    }

    private boolean isLookingUp(Ref<EntityStore> ref, Store<EntityStore> store) {
        Transform look = TargetUtil.getLook(ref, store);
        if (look == null) {
            return false;
        }
        Vector3d direction = new Vector3d(look.getDirection()).normalize();
        double pitch = Math.asin(direction.getY());
        double minPitch = Math.toRadians(lookUpDegrees);
        return pitch >= minPitch;
    }

    private static float resolvePitchRadians(Ref<EntityStore> ref, Store<EntityStore> store) {
        Transform look = TargetUtil.getLook(ref, store);
        if (look == null) {
            return 0.0f;
        }
        Vector3d direction = new Vector3d(look.getDirection()).normalize();
        return (float) Math.asin(direction.getY());
    }

    private static UUID uuidFor(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        return uuidComponent == null ? null : uuidComponent.getUuid();
    }

    private static com.hypixel.hytale.protocol.MovementStates movementStates(Store<EntityStore> store, Ref<EntityStore> ref) {
        MovementStatesComponent movementStatesComponent =
                (MovementStatesComponent) store.getComponent(ref, MovementStatesComponent.getComponentType());
        return movementStatesComponent == null ? null : movementStatesComponent.getMovementStates();
    }

    private static final class PlayerInputState {
        private boolean hasMovementStates;
        private com.hypixel.hytale.protocol.MovementStates lastMovementStates = new com.hypixel.hytale.protocol.MovementStates();
        private long lastCrouchTapAt;
        private long lastSprintTapAt;
        private long lastRightClickTapAt;
    }
}
