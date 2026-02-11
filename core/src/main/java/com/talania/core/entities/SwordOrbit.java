package com.talania.core.entities;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.item.PreventItemMerging;
import com.hypixel.hytale.server.core.modules.entity.item.PreventPickup;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Spawns and animates orbiting sword entities.
 *
 * <p>For timed effects, prefer {@link SwordOrbitEffect} with {@link EntityAnimationManager}:</p>
 *
 * <pre>{@code
 * SwordOrbit orbit = new SwordOrbit("Weapon_Sword_Iron", 6, 1.2, 0.004f);
 * SwordOrbitEffect effect = new SwordOrbitEffect(playerRef, orbit, 6_000L);
 * animationManager.add(effect, store, System.currentTimeMillis());
 * }</pre>
 */
public final class SwordOrbit {
    private final List<SwordInstance> swords = new ArrayList<>();

    private static final float DEFAULT_SWORD_SCALE = 1.0f;
    private static final double DEFAULT_ORBIT_RADIUS = 1.0;
    private static final double DEFAULT_Y_OFFSET = 1.05;
    private static final float DEFAULT_YAW_OFFSET = 0.0f;
    private static final float DEFAULT_ROLL = (float) (-Math.PI * 0.5);
    private static final float DEFAULT_ROTATION_RADIANS_PER_MS =
            (float) (Math.PI * 2.0 / 6000.0) * 2.55f;

    private final String itemId;
    private final int swordCount;
    private final float swordScale;
    private final double orbitRadius;
    private final double yOffset;
    private final float yawOffset;
    private final float roll;
    private final float rotationRadiansPerMs;

    private long startAtMs;

    /**
     * Create a sword orbit effect with the default tuning values.
     *
     * @param itemId Item asset ID used for each sword.
     * @param swordCount Number of swords to spawn.
     */
    public SwordOrbit(String itemId, int swordCount) {
        this(itemId, swordCount, DEFAULT_ORBIT_RADIUS, DEFAULT_ROTATION_RADIANS_PER_MS);
    }

    /**
     * Create a sword orbit effect with custom radius and rotation speed.
     *
     * @param itemId Item asset ID used for each sword.
     * @param swordCount Number of swords to spawn.
     * @param orbitRadius Distance from the player center.
     * @param rotationRadiansPerMs Orbit rotation speed.
     */
    public SwordOrbit(String itemId, int swordCount, double orbitRadius, float rotationRadiansPerMs) {
        this.itemId = itemId != null ? itemId : "Weapon_Sword_Iron";
        this.swordCount = Math.max(1, swordCount);
        this.swordScale = DEFAULT_SWORD_SCALE;
        this.orbitRadius = Math.max(0.1, orbitRadius);
        this.yOffset = DEFAULT_Y_OFFSET;
        this.yawOffset = DEFAULT_YAW_OFFSET;
        this.roll = DEFAULT_ROLL;
        this.rotationRadiansPerMs = rotationRadiansPerMs <= 0.0f
                ? DEFAULT_ROTATION_RADIANS_PER_MS
                : rotationRadiansPerMs;
    }

    /**
     * Record the start time used for orbital rotation.
     */
    public void start(long nowMs) {
        this.startAtMs = nowMs;
    }

    /**
     * Spawn orbiting sword entities around the target player.
     */
    public void spawn(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        clear(store);

        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) {
            return;
        }
        String modelId = item.getModel();
        if (modelId == null && item.hasBlockType()) {
            BlockType blockType = BlockType.getAssetMap().getAsset(item.getId());
            if (blockType != null) {
                modelId = blockType.getCustomModel();
            }
        }
        ModelAsset modelAsset = modelId != null ? ModelAsset.getAssetMap().getAsset(modelId) : null;

        Vector3d center = resolvePlayerCenter(ref, store, yOffset);
        if (center == null) {
            return;
        }
        double step = (Math.PI * 2.0) / swordCount;
        for (int i = 0; i < swordCount; i++) {
            float angle = (float) (i * step);
            Vector3d position = new Vector3d(
                    center.x + Math.cos(angle) * orbitRadius,
                    center.y,
                    center.z + Math.sin(angle) * orbitRadius
            );
            Vector3f rotation = resolveSwordRotation(position, center, yawOffset, roll);
            Holder<EntityStore> holder = store.getRegistry().newHolder();
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
            holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
            if (modelAsset != null) {
                holder.addComponent(ModelComponent.getComponentType(),
                        new ModelComponent(Model.createStaticScaledModel(modelAsset, swordScale)));
                holder.addComponent(com.hypixel.hytale.server.core.modules.entity.component.PersistentModel.getComponentType(),
                        new com.hypixel.hytale.server.core.modules.entity.component.PersistentModel(
                                new Model.ModelReference(modelId, swordScale, null, true)));
            }
            ItemStack itemStack = new ItemStack(itemId, 1);
            itemStack.setOverrideDroppedItemAnimation(true);
            holder.addComponent(ItemComponent.getComponentType(), new ItemComponent(itemStack));
            holder.addComponent(PreventPickup.getComponentType(), PreventPickup.INSTANCE);
            holder.addComponent(PreventItemMerging.getComponentType(), PreventItemMerging.INSTANCE);
            holder.addComponent(HeadRotation.getComponentType(), new HeadRotation(rotation));
            holder.addComponent(PropComponent.getComponentType(), PropComponent.get());
            holder.addComponent(EntityStore.REGISTRY.getNonSerializedComponentType(), NonSerialized.get());
            Ref<EntityStore> swordRef = store.addEntity(holder, AddReason.SPAWN);
            swords.add(new SwordInstance(swordRef, angle));
        }
    }

    /**
     * Update sword positions and rotations. Call from your tick loop.
     */
    public void update(Ref<EntityStore> ref, Store<EntityStore> store, long nowMs) {
        if (ref == null || store == null || swords.isEmpty()) {
            return;
        }
        Vector3d center = resolvePlayerCenter(ref, store, yOffset);
        if (center == null) {
            return;
        }
        float elapsed = (float) (nowMs - startAtMs);
        float rotationOffset = elapsed * rotationRadiansPerMs;
        float playerYaw = resolvePlayerYaw(ref, store);

        for (Iterator<SwordInstance> it = swords.iterator(); it.hasNext(); ) {
            SwordInstance sword = it.next();
            if (sword == null || sword.ref == null || !sword.ref.isValid()) {
                it.remove();
                continue;
            }
            float angle = sword.baseAngle + rotationOffset - playerYaw;
            Vector3d position = new Vector3d(
                    center.x + Math.cos(angle) * orbitRadius,
                    center.y,
                    center.z + Math.sin(angle) * orbitRadius
            );
            Vector3f desiredRotation = resolveSwordRotation(position, center, yawOffset, roll);
            TransformComponent transform = store.getComponent(sword.ref, TransformComponent.getComponentType());
            if (transform != null) {
                transform.teleportPosition(position);
                Vector3f rotation = transform.getRotation();
                rotation.setYaw(desiredRotation.getYaw());
                rotation.setPitch(desiredRotation.getPitch());
                rotation.setRoll(desiredRotation.getRoll());
                transform.teleportRotation(rotation);
            }
            HeadRotation headRotation = store.getComponent(sword.ref, HeadRotation.getComponentType());
            if (headRotation != null) {
                Vector3f head = headRotation.getRotation();
                head.setYaw(desiredRotation.getYaw());
                head.setPitch(desiredRotation.getPitch());
                head.setRoll(desiredRotation.getRoll());
                headRotation.teleportRotation(head);
            }
        }
    }

    /**
     * Remove all spawned sword entities.
     */
    public void clear(Store<EntityStore> store) {
        if (store == null || swords.isEmpty()) {
            swords.clear();
            return;
        }
        for (SwordInstance sword : swords) {
            if (sword == null || sword.ref == null) {
                continue;
            }
            if (sword.ref.isValid()) {
                store.removeEntity(sword.ref, RemoveReason.REMOVE);
            }
        }
        swords.clear();
    }

    private static Vector3d resolvePlayerCenter(Ref<EntityStore> ref, Store<EntityStore> store, double yOffset) {
        TransformComponent transform = (TransformComponent) store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            return null;
        }
        Vector3d position = new Vector3d(transform.getPosition());
        position.y += yOffset;
        return position;
    }

    /**
     * Resolve the player's yaw, preferring look rotation when available.
     */
    private static float resolvePlayerYaw(Ref<EntityStore> ref, Store<EntityStore> store) {
        Transform look = TargetUtil.getLook(ref, store);
        if (look == null) {
            TransformComponent transform = (TransformComponent) store.getComponent(ref, TransformComponent.getComponentType());
            Vector3f rotation = transform != null ? transform.getRotation() : null;
            return rotation != null ? rotation.getYaw() : 0.0F;
        }
        Vector3f lookRotation = look.getRotation();
        return lookRotation != null ? lookRotation.getYaw() : 0.0F;
    }

    /**
     * Compute a sword rotation that faces away from the center.
     */
    private static Vector3f resolveSwordRotation(Vector3d position, Vector3d center, float yawOffset, float roll) {
        float baseYaw = yawAwayFromCenter(position, center) + yawOffset;
        return new Vector3f(0.0F, baseYaw, roll);
    }

    /**
     * Compute yaw angle pointing away from the orbit center.
     */
    private static float yawAwayFromCenter(Vector3d position, Vector3d center) {
        if (position == null || center == null) {
            return 0.0F;
        }
        Vector3d direction = new Vector3d(position).subtract(center);
        if (direction.squaredLength() <= 0.0001) {
            return 0.0F;
        }
        direction.normalize();
        return (float) Math.atan2(-direction.getX(), -direction.getZ());
    }

    private static final class SwordInstance {
        private final Ref<EntityStore> ref;
        private final float baseAngle;

        private SwordInstance(Ref<EntityStore> ref, float baseAngle) {
            this.ref = ref;
            this.baseAngle = baseAngle;
        }
    }
}
