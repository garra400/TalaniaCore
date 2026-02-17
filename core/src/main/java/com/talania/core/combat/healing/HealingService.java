package com.talania.core.combat.healing;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Centralized helper for applying healing to a target's health stat.
 */
public final class HealingService {
    private HealingService() {}

    /**
     * Apply additive healing to the target and return the applied amount.
     */
    public static float applyHeal(Ref<EntityStore> targetRef, Store<EntityStore> store, float amount) {
        if (targetRef == null || store == null) {
            return 0.0f;
        }
        if (amount <= 0.0f) {
            return 0.0f;
        }
        EntityStatMap statMap = (EntityStatMap) store.getComponent(targetRef, EntityStatMap.getComponentType());
        if (statMap == null) {
            return 0.0f;
        }
        EntityStatValue health = statMap.get(DefaultEntityStatTypes.getHealth());
        if (health == null) {
            return 0.0f;
        }
        float before = health.get();
        statMap.addStatValue(EntityStatMap.Predictable.SELF, DefaultEntityStatTypes.getHealth(), amount);
        float after = health.get();
        return Math.max(0.0f, after - before);
    }
}
