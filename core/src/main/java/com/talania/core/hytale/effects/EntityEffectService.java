package com.talania.core.hytale.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Utility for applying entity effect assets by ID.
 */
public final class EntityEffectService {
    private EntityEffectService() {}

    public static boolean apply(Ref<EntityStore> targetRef, Store<EntityStore> store,
                                String effectId, long durationMs, OverlapBehavior overlapBehavior) {
        if (targetRef == null || store == null || effectId == null || effectId.isBlank() || durationMs <= 0L) {
            return false;
        }
        EffectControllerComponent effectController =
                (EffectControllerComponent) store.getComponent(targetRef, EffectControllerComponent.getComponentType());
        if (effectController == null) {
            return false;
        }
        int effectIndex = resolveEffectIndex(effectId);
        if (effectIndex == Integer.MIN_VALUE) {
            return false;
        }
        EntityEffect effect = (EntityEffect) EntityEffect.getAssetMap().getAsset(effectIndex);
        if (effect == null) {
            return false;
        }
        float durationSeconds = Math.max(0.1F, durationMs / 1000.0F);
        OverlapBehavior overlap = overlapBehavior != null ? overlapBehavior : OverlapBehavior.OVERWRITE;
        effectController.addEffect(targetRef, effect, durationSeconds, overlap, store);
        return true;
    }

    public static int resolveEffectIndex(String effectId) {
        if (effectId == null || effectId.isBlank()) {
            return Integer.MIN_VALUE;
        }
        int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
        if (effectIndex != Integer.MIN_VALUE) {
            return effectIndex;
        }
        return EntityEffect.getAssetMap().getIndex("EntityEffect/" + effectId);
    }
}
