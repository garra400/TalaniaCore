package com.talania.core.projectiles;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.time.TimeResource;

/**
 * Utilities for spawning rain-of-arrows style projectiles.
 */
public final class RainOfArrowsUtil {
    private RainOfArrowsUtil() {}

    /**
     * Select a random valid target within range for a rain-of-arrows strike.
     *
     * <p>See {@link ProjectileTargetingUtil#pickRandomTarget} for the selection rules.</p>
     */
    public static Ref<EntityStore> pickRandomTarget(Ref<EntityStore> attackerRef,
                                                    Store<EntityStore> store,
                                                    Vector3d origin,
                                                    double range,
                                                    boolean includePlayers,
                                                    Ref<EntityStore> excludeRef) {
        return ProjectileTargetingUtil.pickRandomTarget(attackerRef, store, origin, range, includePlayers, excludeRef);
    }

    /**
     * Pick a landing position within range for a rain-of-arrows strike.
     *
     * <p>See {@link ProjectileTargetingUtil#pickRandomLandingPosition} for the selection rules.</p>
     */
    public static Vector3d pickRandomLandingPosition(Ref<EntityStore> attackerRef,
                                                     Store<EntityStore> store,
                                                     Vector3d center,
                                                     double range,
                                                     boolean includePlayers,
                                                     double minPlayerDistance,
                                                     double minOtherPlayerDistance,
                                                     int attempts) {
        return ProjectileTargetingUtil.pickRandomLandingPosition(attackerRef, store, center, range, includePlayers,
                minPlayerDistance, minOtherPlayerDistance, attempts);
    }

    /**
     * Spawn a rain projectile aimed at a moving target, using simple lead prediction.
     */
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

    /**
     * Spawn a rain projectile aimed at a fixed world position.
     */
    public static boolean spawnRainProjectileAt(Ref<EntityStore> attackerRef,
                                                Store<EntityStore> store,
                                                Vector3d targetPos,
                                                Settings settings) {
        if (attackerRef == null || store == null || targetPos == null) {
            return false;
        }
        Vector3d origin = new Vector3d(targetPos.x, targetPos.y + settings.spawnHeight, targetPos.z);
        Vector3f rotation = ProjectileTargetingUtil.rotationToward(origin, targetPos);
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

    /**
     * Boost downward velocity to make the rain feel snappier.
     */
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

    /**
     * Tunable parameters for rain-of-arrows spawning.
     */
    public static final class Settings {
        private String projectileId = "Arrow_FullCharge";
        private double spawnHeight = 12.0;
        private double fallSpeedMultiplier = 1.45;
        private double leadSeconds = 0.15;
        private double maxLeadDistance = 2.5;

        /** Projectile asset ID to spawn. */
        public Settings projectileId(String projectileId) {
            this.projectileId = projectileId;
            return this;
        }

        /** Height above target to spawn projectiles. */
        public Settings spawnHeight(double spawnHeight) {
            this.spawnHeight = spawnHeight;
            return this;
        }

        /** Multiplier for downward velocity after spawn. */
        public Settings fallSpeedMultiplier(double fallSpeedMultiplier) {
            this.fallSpeedMultiplier = fallSpeedMultiplier;
            return this;
        }

        /** Seconds of target velocity to lead. */
        public Settings leadSeconds(double leadSeconds) {
            this.leadSeconds = leadSeconds;
            return this;
        }

        /** Maximum distance the lead prediction is allowed to shift. */
        public Settings maxLeadDistance(double maxLeadDistance) {
            this.maxLeadDistance = maxLeadDistance;
            return this;
        }
    }
}
