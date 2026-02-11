package com.talania.core.projectiles;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper utilities for selecting targets and computing projectile aim vectors.
 */
public final class ProjectileTargetingUtil {
    private ProjectileTargetingUtil() {}

    /**
     * Picks a random entity within range of the origin, optionally excluding players.
     */
    public static Ref<EntityStore> pickRandomTarget(Ref<EntityStore> attackerRef,
                                                    Store<EntityStore> store,
                                                    Vector3d origin,
                                                    double range,
                                                    boolean includePlayers,
                                                    Ref<EntityStore> excludeRef) {
        if (attackerRef == null || store == null || origin == null) {
            return null;
        }
        List<Ref<EntityStore>> candidates = TargetUtil.getAllEntitiesInSphere(origin, range, store);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        List<Ref<EntityStore>> filtered = new ArrayList<>();
        for (Ref<EntityStore> targetRef : candidates) {
            if (targetRef == null || !targetRef.isValid() || targetRef.equals(attackerRef)) {
                continue;
            }
            if (excludeRef != null && excludeRef.equals(targetRef)) {
                continue;
            }
            Player targetPlayer = (Player) store.getComponent(targetRef, Player.getComponentType());
            if (targetPlayer != null && !includePlayers) {
                continue;
            }
            filtered.add(targetRef);
        }
        if (filtered.isEmpty()) {
            return null;
        }
        return filtered.get(ThreadLocalRandom.current().nextInt(filtered.size()));
    }

    /**
     * Picks a random ground position in a radius, avoiding players if requested.
     */
    public static Vector3d pickRandomLandingPosition(Ref<EntityStore> attackerRef,
                                                     Store<EntityStore> store,
                                                     Vector3d center,
                                                     double range,
                                                     boolean includePlayers,
                                                     double minPlayerDistance,
                                                     double minOtherPlayerDistance,
                                                     int attempts) {
        if (attackerRef == null || store == null || center == null) {
            return null;
        }
        Vector3d attackerPos = resolveEntityPosition(attackerRef, store);
        double minDistanceSq = minPlayerDistance * minPlayerDistance;
        double otherPlayerMinSq = minOtherPlayerDistance * minOtherPlayerDistance;
        for (int i = 0; i < attempts; i++) {
            double angle = ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2.0);
            double radius = Math.sqrt(ThreadLocalRandom.current().nextDouble()) * range;
            Vector3d candidate = new Vector3d(
                    center.x + Math.cos(angle) * radius,
                    center.y,
                    center.z + Math.sin(angle) * radius
            );
            if (attackerPos != null) {
                Vector3d delta = new Vector3d(candidate).subtract(attackerPos);
                if (delta.squaredLength() < minDistanceSq) {
                    continue;
                }
            }
            if (!includePlayers && hasOtherPlayerNear(candidate, attackerRef, store, otherPlayerMinSq)) {
                continue;
            }
            return candidate;
        }
        return null;
    }

    /**
     * Compute a rotation that aims from origin toward target.
     */
    public static Vector3f rotationToward(Vector3d origin, Vector3d target) {
        if (origin == null || target == null) {
            return null;
        }
        Vector3d direction = new Vector3d(target).subtract(origin);
        if (direction.squaredLength() <= 0.0001) {
            return null;
        }
        direction.normalize();
        float pitch = (float) Math.asin(direction.getY());
        float yaw = (float) Math.atan2(-direction.getX(), -direction.getZ());
        return new Vector3f(pitch, yaw, 0.0F);
    }

    /**
     * Resolve an entity position from its transform component.
     */
    public static Vector3d resolveEntityPosition(Ref<EntityStore> ref, Store<EntityStore> store) {
        TransformComponent transform = (TransformComponent) store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }
        return new Vector3d(transform.getPosition());
    }

    /**
     * Check whether another player is within the given squared distance.
     */
    private static boolean hasOtherPlayerNear(Vector3d position, Ref<EntityStore> attackerRef, Store<EntityStore> store,
                                              double minDistanceSq) {
        if (position == null || store == null) {
            return false;
        }
        List<Ref<EntityStore>> players = collectPlayersNear(position, store);
        for (Ref<EntityStore> playerRef : players) {
            if (playerRef == null || !playerRef.isValid() || playerRef.equals(attackerRef)) {
                continue;
            }
            TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
            if (transform == null) {
                continue;
            }
            Vector3d delta = new Vector3d(transform.getPosition()).subtract(position);
            if (delta.squaredLength() < minDistanceSq) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collect player entity refs within a fixed radius of a position.
     */
    private static List<Ref<EntityStore>> collectPlayersNear(Vector3d position, Store<EntityStore> store) {
        ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
        if (position == null || store == null) {
            return playerRefs;
        }
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource =
                (SpatialResource) store.getResource(EntityModule.get().getPlayerSpatialResourceType());
        if (playerSpatialResource == null) {
            return playerRefs;
        }
        playerSpatialResource.getSpatialStructure().collect(position, 75.0, playerRefs);
        return playerRefs;
    }
}
