package com.talania.core.hytale.teleport;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

/**
 * Teleport helpers based on the working blink implementation.
 */
public final class TeleportUtil {
    private TeleportUtil() {}

    /**
     * Blink forward up to the given distance, stopping short of the first solid block.
     */
    public static boolean blink(Ref<EntityStore> ref, Store<EntityStore> store, double distance) {
        if (ref == null || store == null) {
            return false;
        }
        Transform look = TargetUtil.getLook(ref, store);
        if (look == null) {
            return false;
        }
        Vector3d origin = new Vector3d(look.getPosition());
        Vector3d direction = new Vector3d(look.getDirection()).normalize();
        Vector3d hit = TargetUtil.getTargetLocation(ref, blockId -> blockId != 0, distance, store);
        Vector3d destination;
        if (hit == null) {
            destination = origin.add(new Vector3d(direction).scale(distance));
        } else {
            destination = new Vector3d(hit).subtract(new Vector3d(direction).scale(0.6));
        }
        Vector3f headRotation = resolveHeadRotation(ref, store);
        Teleport teleport = buildTeleportWithUprightRotation(ref, store, destination);
        if (headRotation != null) {
            teleport.setHeadRotation(headRotation);
        }
        store.addComponent(ref, Teleport.getComponentType(), teleport);
        applyHeadRotationNow(ref, store, headRotation);
        return true;
    }

    private static Teleport buildTeleportWithUprightRotation(Ref<EntityStore> ref, Store<EntityStore> store,
                                                            Vector3d destination) {
        TransformComponent transformComponent =
                (TransformComponent) store.getComponent(ref, TransformComponent.getComponentType());
        Vector3f bodyRotation = transformComponent != null
                ? new Vector3f(transformComponent.getRotation())
                : new Vector3f(Float.NaN, Float.NaN, Float.NaN);
        bodyRotation.setPitch(0.0F);
        bodyRotation.setRoll(0.0F);
        return new Teleport(destination, bodyRotation);
    }

    private static Vector3f resolveHeadRotation(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return null;
        }
        HeadRotation headRotationComponent =
                (HeadRotation) store.getComponent(ref, HeadRotation.getComponentType());
        Vector3f headRotation = headRotationComponent != null
                ? new Vector3f(headRotationComponent.getRotation())
                : null;
        if (headRotation == null) {
            Transform look = TargetUtil.getLook(ref, store);
            if (look != null) {
                headRotation = new Vector3f(look.getRotation());
            }
        }
        if (headRotation == null) {
            return null;
        }
        headRotation.setRoll(0.0F);
        return headRotation;
    }

    private static void applyHeadRotationNow(Ref<EntityStore> ref, Store<EntityStore> store, Vector3f headRotation) {
        if (ref == null || store == null || headRotation == null) {
            return;
        }
        HeadRotation current = (HeadRotation) store.getComponent(ref, HeadRotation.getComponentType());
        if (current == null) {
            store.addComponent(ref, HeadRotation.getComponentType(), new HeadRotation(headRotation));
        } else {
            current.teleportRotation(headRotation);
        }
    }
}
