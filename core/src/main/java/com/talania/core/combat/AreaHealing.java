package com.talania.core.combat;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.List;

/**
 * Utility for a simple area healing zone.
 */
public final class AreaHealing {
    private AreaHealing() {}

    /**
     * Starts a healing zone at the given center.
     */
    public static void start(State state, Vector3d center, long nowMs, long durationMs) {
        if (state == null || center == null || durationMs <= 0L) {
            return;
        }
        state.activeUntil = nowMs + durationMs;
        state.nextTickAt = nowMs;
        state.vfxSpawned = false;
        state.center = new Vector3d(center);
    }

    /**
     * Ticks healing zone logic and applies healing to nearby players.
     */
    public static void tick(State state, Store<EntityStore> store, Settings settings, float healMultiplier) {
        if (state == null || store == null || settings == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (state.activeUntil <= 0L) {
            return;
        }
        if (now >= state.activeUntil) {
            end(state);
            return;
        }
        if (now < state.nextTickAt) {
            return;
        }
        if (state.center == null) {
            return;
        }
        if (!state.vfxSpawned && settings.particleId != null && !settings.particleId.isBlank()) {
            Vector3d vfxPosition = new Vector3d(state.center);
            vfxPosition.y += settings.vfxYOffset;
            ObjectList<Ref<EntityStore>> viewers = collectPlayersNear(vfxPosition, store);
            ParticleUtil.spawnParticleEffect(
                    settings.particleId, vfxPosition, 0.0F, 0.0F, 0.0F,
                    settings.vfxScale, null, viewers, store);
            state.vfxSpawned = true;
        }
        List<Ref<EntityStore>> targets =
                TargetUtil.getAllEntitiesInSphere(state.center, settings.radius, store);
        for (Ref<EntityStore> targetRef : targets) {
            if (targetRef == null || !targetRef.isValid()) {
                continue;
            }
            Player player = (Player) store.getComponent(targetRef, Player.getComponentType());
            if (player == null) {
                continue;
            }
            EntityStatMap statMap = (EntityStatMap) store.getComponent(targetRef, EntityStatMap.getComponentType());
            if (statMap == null) {
                continue;
            }
            EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
            if (health == null) {
                continue;
            }
            float max = health.getMax();
            if (max <= 0.0F) {
                continue;
            }
            float heal = max * settings.healRatio * Math.max(0.0F, healMultiplier);
            statMap.addStatValue(DefaultEntityStatTypes.getHealth(), heal);
        }
        state.nextTickAt = now + settings.tickMs;
    }

    public static void end(State state) {
        if (state == null) {
            return;
        }
        state.activeUntil = 0L;
        state.nextTickAt = 0L;
        state.vfxSpawned = false;
        state.center = null;
    }

    private static ObjectList<Ref<EntityStore>> collectPlayersNear(Vector3d position, Store<EntityStore> store) {
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

    public static final class State {
        private long activeUntil;
        private long nextTickAt;
        private boolean vfxSpawned;
        private Vector3d center;
    }

    public static final class Settings {
        private double radius = 2.5;
        private long tickMs = 1_000L;
        private float healRatio = 0.04f;
        private String particleId = "Totem_Heal_Simple_Test";
        private float vfxScale = 0.75f;
        private float vfxYOffset = -0.2f;

        public Settings radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Settings tickMs(long tickMs) {
            this.tickMs = tickMs;
            return this;
        }

        public Settings healRatio(float healRatio) {
            this.healRatio = healRatio;
            return this;
        }

        public Settings particleId(String particleId) {
            this.particleId = particleId;
            return this;
        }

        public Settings vfxScale(float vfxScale) {
            this.vfxScale = vfxScale;
            return this;
        }

        public Settings vfxYOffset(float vfxYOffset) {
            this.vfxYOffset = vfxYOffset;
            return this;
        }
    }
}
