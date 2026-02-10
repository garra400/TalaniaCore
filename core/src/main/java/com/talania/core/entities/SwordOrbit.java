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
import com.hypixel.hytale.server.core.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.entity.component.PropComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
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
 */
public final class SwordOrbit {
    private final List<SwordInstance> swords = new ArrayList<>();

    private String itemId = "Weapon_Sword_Iron";
    private int swordCount = 6;
    private float swordScale = 1.0f;
    private double orbitRadius = 1.0;
    private double yOffset = 1.05;
    private float yawOffset = 0.0f;
    private float roll = (float) (-Math.PI * 0.5);
    private float rotationRadiansPerMs = (float) (Math.PI * 2.0 / 6000.0) * 2.55f;

    private long startAtMs;

    public SwordOrbit itemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public SwordOrbit swordCount(int swordCount) {
        this.swordCount = Math.max(1, swordCount);
        return this;
    }

    public SwordOrbit swordScale(float swordScale) {
        this.swordScale = Math.max(0.1f, swordScale);
        return this;
    }

    public SwordOrbit orbitRadius(double orbitRadius) {
        this.orbitRadius = Math.max(0.1, orbitRadius);
        return this;
    }

    public SwordOrbit yOffset(double yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    public SwordOrbit yawOffset(float yawOffset) {
        this.yawOffset = yawOffset;
        return this;
    }

    public SwordOrbit roll(float roll) {
        this.roll = roll;
        return this;
    }

    public SwordOrbit rotationRadiansPerMs(float rotationRadiansPerMs) {
        this.rotationRadiansPerMs = rotationRadiansPerMs;
        return this;
    }

    public void start(long nowMs) {
        this.startAtMs = nowMs;
    }

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

    private static Vector3f resolveSwordRotation(Vector3d position, Vector3d center, float yawOffset, float roll) {
        float baseYaw = yawAwayFromCenter(position, center) + yawOffset;
        return new Vector3f(0.0F, baseYaw, roll);
    }

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
