package com.talania.core.hytale.effects;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Utility for applying Hytale entity effects by string ID.
 */
public final class EntityEffectService {

    private EntityEffectService() {
    }

    /**
     * Apply a named entity effect to the target.
     *
     * @param target           entity to apply the effect to
     * @param store            entity store
     * @param effectId         the effect asset identifier
     * @param durationMs       duration in milliseconds
     * @param overlapBehavior  how to handle overlapping effects
     */
    public static void apply(Ref<EntityStore> target, Store<EntityStore> store,
                             String effectId, long durationMs,
                             OverlapBehavior overlapBehavior) {
        if (target == null || store == null || effectId == null || effectId.isBlank() || durationMs <= 0L) {
            return;
        }
        EffectControllerComponent effectController =
                (EffectControllerComponent) store.getComponent(target, EffectControllerComponent.getComponentType());
        if (effectController == null) {
            return;
        }
        int effectIndex = resolveEffectIndex(effectId);
        if (effectIndex == Integer.MIN_VALUE) {
            return;
        }
        EntityEffect effect = (EntityEffect) EntityEffect.getAssetMap().getAsset(effectIndex);
        if (effect == null) {
            return;
        }
        float durationSeconds = Math.max(0.1F, durationMs / 1000.0F);
        effectController.addEffect(target, effect, durationSeconds, overlapBehavior, store);
    }

    private static int resolveEffectIndex(String effectId) {
        int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
        if (effectIndex != Integer.MIN_VALUE) {
            return effectIndex;
        }
        return EntityEffect.getAssetMap().getIndex("EntityEffect/" + effectId);
    }
}
