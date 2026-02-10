package com.talania.core.projectiles;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Utilities for spawning rain-of-arrows style projectiles.
 */
public final class RainOfArrowsUtil {
    private RainOfArrowsUtil() {}

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
        Vector3d attackerPos = resolvePlayerPosition(attackerRef, store);
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

    public static boolean spawnRainProjectileAtTarget(Ref<EntityStore> attackerRef,
                                                      Store<EntityStore> store,
                                                      Ref<EntityStore> targetRef,
                                                      Settings settings) {
        if (attackerRef == null || store == null || targetRef == null || !targetRef.isValid()) {
            return false;
        }
        TransformComponent targetTransform =
                (TransformComponent) store.getComponent(targetRef, TransformComponent.getComponentType());
        if (targetTransform == null) {
            return false;
        }
        Vector3d targetPos = new Vector3d(targetTransform.getPosition());
        Velocity targetVelocity = (Velocity) store.getComponent(targetRef, Velocity.getComponentType());
        if (targetVelocity != null) {
            double leadX = targetVelocity.getX() * settings.leadSeconds;
            double leadZ = targetVelocity.getZ() * settings.leadSeconds;
            double leadSq = (leadX * leadX) + (leadZ * leadZ);
            if (leadSq > settings.maxLeadDistance * settings.maxLeadDistance) {
                double leadLength = Math.sqrt(leadSq);
                if (leadLength > 0.0001) {
                    double scale = settings.maxLeadDistance / leadLength;
                    leadX *= scale;
                    leadZ *= scale;
                }
            }
            targetPos.x += leadX;
            targetPos.z += leadZ;
        }
        targetPos.y += 0.9;
        return spawnRainProjectileAt(attackerRef, store, targetPos, settings);
    }

    public static boolean spawnRainProjectileAt(Ref<EntityStore> attackerRef,
                                                Store<EntityStore> store,
                                                Vector3d targetPos,
                                                Settings settings) {
        if (attackerRef == null || store == null || targetPos == null) {
            return false;
        }
        Vector3d origin = new Vector3d(targetPos.x, targetPos.y + settings.spawnHeight, targetPos.z);
        Vector3f rotation = rotationToward(origin, targetPos);
        if (rotation == null) {
            return false;
        }
        TimeResource timeResource = (TimeResource) store.getResource(TimeResource.getResourceType());
        if (timeResource == null) {
            return false;
        }
        Holder<EntityStore> holder = ProjectileComponent.assembleDefaultProjectile(
                timeResource, settings.projectileId, origin, rotation);
        ProjectileComponent projectileComponent = holder.getComponent(ProjectileComponent.getComponentType());
        if (projectileComponent == null) {
            return false;
        }
        holder.ensureComponent(com.hypixel.hytale.server.core.modules.entity.component.Intangible.getComponentType());
        if (projectileComponent.getProjectile() == null) {
            projectileComponent.initialize();
            if (projectileComponent.getProjectile() == null) {
                return false;
            }
        }
        UUIDComponent uuid = (UUIDComponent) store.getComponent(attackerRef, UUIDComponent.getComponentType());
        if (uuid == null) {
            return false;
        }
        projectileComponent.shoot(holder, uuid.getUuid(),
                origin.getX(), origin.getY(), origin.getZ(), rotation.getYaw(), rotation.getPitch());
        applyRainVelocityTuning(holder, settings.fallSpeedMultiplier);
        store.addEntity(holder, AddReason.SPAWN);
        return true;
    }

    private static void applyRainVelocityTuning(Holder<EntityStore> holder, double fallSpeedMultiplier) {
        if (holder == null) {
            return;
        }
        Velocity velocity = holder.getComponent(Velocity.getComponentType());
        if (velocity == null) {
            return;
        }
        double vy = velocity.getY();
        if (vy < 0.0) {
            velocity.set(velocity.getX(), vy * fallSpeedMultiplier, velocity.getZ());
        } else {
            velocity.addForce(0.0, -fallSpeedMultiplier, 0.0);
        }
    }

    private static Vector3f rotationToward(Vector3d origin, Vector3d target) {
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

    private static Vector3d resolvePlayerPosition(Ref<EntityStore> ref, Store<EntityStore> store) {
        TransformComponent transform = (TransformComponent) store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }
        return new Vector3d(transform.getPosition());
    }

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

    public static final class Settings {
        private String projectileId = "Arrow_FullCharge";
        private double spawnHeight = 12.0;
        private double fallSpeedMultiplier = 1.45;
        private double leadSeconds = 0.15;
        private double maxLeadDistance = 2.5;

        public Settings projectileId(String projectileId) {
            this.projectileId = projectileId;
            return this;
        }

        public Settings spawnHeight(double spawnHeight) {
            this.spawnHeight = spawnHeight;
            return this;
        }

        public Settings fallSpeedMultiplier(double fallSpeedMultiplier) {
            this.fallSpeedMultiplier = fallSpeedMultiplier;
            return this;
        }

        public Settings leadSeconds(double leadSeconds) {
            this.leadSeconds = leadSeconds;
            return this;
        }

        public Settings maxLeadDistance(double maxLeadDistance) {
            this.maxLeadDistance = maxLeadDistance;
            return this;
        }
    }
}
