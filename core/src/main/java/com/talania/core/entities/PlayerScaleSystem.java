package com.talania.core.entities;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the PLAYER_SCALE stat to player entities.
 */
public final class PlayerScaleSystem extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> QUERY = Query.and(Archetype.of(
            UUIDComponent.getComponentType(),
            Player.getComponentType()
    ));
    private static final float EPSILON = 0.001f;
    private final Map<UUID, Float> lastScale = new HashMap<>();

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }
        UUID uuid = uuidComponent.getUuid();
        if (uuid == null) {
            return;
        }
        float scale = StatsManager.getStat(uuid, StatType.PLAYER_SCALE);
        Float last = lastScale.get(uuid);
        if (last != null && Math.abs(last - scale) <= EPSILON) {
            return;
        }

        boolean appliedModelScale = applyModelScale(ref, store, commandBuffer, scale);
        if (!appliedModelScale) {
            applyEntityScale(ref, store, commandBuffer, scale);
        } else {
            resetEntityScaleIfPresent(ref, store, commandBuffer);
        }
        lastScale.put(uuid, scale);
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        lastScale.remove(playerId);
    }

    private boolean applyModelScale(Ref<EntityStore> ref, Store<EntityStore> store,
                                    CommandBuffer<EntityStore> commandBuffer, float scale) {
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef =
                com.talania.core.utils.PlayerRefUtil.resolve(ref, store);
        if (playerRef != null && !com.talania.core.cosmetics.TalaniaCosmetics.getOverrides(playerRef).isEmpty()) {
            // Avoid rebuilding the base model when cosmetics overrides are active.
            return false;
        }
        PlayerSkinComponent skinComponent =
                (PlayerSkinComponent) store.getComponent(ref, PlayerSkinComponent.getComponentType());
        if (skinComponent == null) {
            return false;
        }
        PlayerSkin skin = skinComponent.getPlayerSkin();
        if (skin == null) {
            return false;
        }
        CosmeticsModule cosmetics = CosmeticsModule.get();
        if (cosmetics == null) {
            return false;
        }
        com.hypixel.hytale.server.core.asset.type.model.config.Model model = cosmetics.createModel(skin, scale);
        if (model == null) {
            return false;
        }
        commandBuffer.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
        PlayerSkinComponent refreshed = new PlayerSkinComponent(skin);
        refreshed.setNetworkOutdated();
        commandBuffer.replaceComponent(ref, PlayerSkinComponent.getComponentType(), refreshed);
        return true;
    }

    private void applyEntityScale(Ref<EntityStore> ref, Store<EntityStore> store,
                                  CommandBuffer<EntityStore> commandBuffer, float scale) {
        EntityScaleComponent scaleComponent =
                (EntityScaleComponent) store.getComponent(ref, EntityScaleComponent.getComponentType());
        if (scaleComponent == null) {
            commandBuffer.addComponent(ref, EntityScaleComponent.getComponentType(), new EntityScaleComponent(scale));
        } else {
            EntityScaleComponent next = new EntityScaleComponent(scale);
            commandBuffer.replaceComponent(ref, EntityScaleComponent.getComponentType(), next);
        }
    }

    private void resetEntityScaleIfPresent(Ref<EntityStore> ref, Store<EntityStore> store,
                                           CommandBuffer<EntityStore> commandBuffer) {
        EntityScaleComponent scaleComponent =
                (EntityScaleComponent) store.getComponent(ref, EntityScaleComponent.getComponentType());
        if (scaleComponent == null) {
            return;
        }
        EntityScaleComponent reset = new EntityScaleComponent(1.0f);
        commandBuffer.replaceComponent(ref, EntityScaleComponent.getComponentType(), reset);
    }

}
