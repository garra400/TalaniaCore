package com.talania.core.combat.healing;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.EntityStatOp;
import com.hypixel.hytale.protocol.EntityStatUpdate;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsSystems;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.debug.DebugCategory;
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Scales additive health gains to respect {@link StatType#HEALING_RECEIVED_MULT}.
 *
 * <p>This system runs before the core stat update processing and only adjusts
 * positive {@link EntityStatOp#Add} updates for the health stat.</p>
 */
public final class HealingStatScalingSystem extends EntityTickingSystem<EntityStore>
        implements EntityStatsSystems.StatModifyingSystem {
    private static final float EPSILON = 0.0001f;
    private static final int HEALTH_INDEX = DefaultEntityStatTypes.getHealth();
    private static final Method STAT_VALUE_SETTER = resolveSetter();

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Archetype.of(
                EntityStatMap.getComponentType(),
                UUIDComponent.getComponentType()
        ));
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        EntityStatMap statMap = (EntityStatMap) store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        UUID uuid = uuidComponent != null ? uuidComponent.getUuid() : null;
        float healingMult = uuid != null
                ? StatsManager.getStat(uuid, StatType.HEALING_RECEIVED_MULT)
                : 1.0f;
        float regenMult = uuid != null
                ? StatsManager.getStat(uuid, StatType.HEALTH_REGEN)
                : 1.0f;
        boolean anyMultiplier = Math.abs(healingMult - 1.0f) > EPSILON
                || Math.abs(regenMult - 1.0f) > EPSILON;
        if (!anyMultiplier) {
            return;
        }
        Map<Integer, List<EntityStatUpdate>> updates = statMap.getSelfUpdates();
        if (updates == null) {
            return;
        }
        List<EntityStatUpdate> healthUpdates = updates.get(HEALTH_INDEX);
        if (healthUpdates == null || healthUpdates.isEmpty()) {
            return;
        }
        Map<Integer, FloatList> valuesMap = statMap.getSelfStatValues();
        if (valuesMap == null) {
            return;
        }
        FloatList values = valuesMap.get(HEALTH_INDEX);
        if (values == null) {
            return;
        }
        int updateCount = healthUpdates.size();
        if (values.size() < updateCount * 2) {
            return;
        }

        float offset = 0.0f;
        boolean changed = false;
        for (int i = 0; i < updateCount; i++) {
            EntityStatUpdate update = healthUpdates.get(i);
            if (update == null) {
                continue;
            }
            int baseIndex = i * 2;
            float oldValue = values.getFloat(baseIndex);
            float newValue = values.getFloat(baseIndex + 1);
            float adjustedOld = oldValue + offset;

            if (update.op == EntityStatOp.Add) {
                float delta = update.value;
                if (delta > 0.0f) {
                    float scaledDelta = delta * Math.max(0.0f, healingMult);
                    if (!update.predictable) {
                        scaledDelta *= Math.max(0.0f, regenMult);
                    }
                    float adjustedNew = adjustedOld + scaledDelta;
                    if (Math.abs(scaledDelta - delta) > EPSILON) {
                        update.value = scaledDelta;
                        changed = true;
                    }
                    values.set(baseIndex, adjustedOld);
                    values.set(baseIndex + 1, adjustedNew);
                    offset += (adjustedNew - newValue);
                } else {
                    float adjustedNew = adjustedOld + delta;
                    values.set(baseIndex, adjustedOld);
                    values.set(baseIndex + 1, adjustedNew);
                    offset += (adjustedNew - newValue);
                }
                continue;
            }

            if (Math.abs(offset) > EPSILON) {
                values.set(baseIndex, adjustedOld);
            }
            values.set(baseIndex + 1, newValue);
            offset = 0.0f;
        }

        if (Math.abs(offset) > EPSILON || changed) {
            EntityStatValue health = statMap.get(HEALTH_INDEX);
            if (health != null) {
                float finalValue = values.getFloat((updateCount * 2) - 1);
                setStatValue(health, finalValue);
            }
        }

        if (changed && uuid != null && TalaniaDebug.logs().isEnabled(uuid, DebugCategory.MODIFIERS)) {
            TalaniaDebug.logs().log(uuid, DebugCategory.MODIFIERS,
                    "Healing scaled (received=" + String.format("%.2f", healingMult)
                            + ", regen=" + String.format("%.2f", regenMult) + ").");
        }
    }

    private static void setStatValue(EntityStatValue value, float newValue) {
        if (value == null || STAT_VALUE_SETTER == null) {
            return;
        }
        try {
            STAT_VALUE_SETTER.invoke(value, newValue);
        } catch (Exception ignored) {
            // Best-effort; if we fail, the update stream still reflects the scaled value.
        }
    }

    private static Method resolveSetter() {
        try {
            Method method = EntityStatValue.class.getDeclaredMethod("set", float.class);
            method.setAccessible(true);
            return method;
        } catch (Exception ignored) {
            return null;
        }
    }
}
