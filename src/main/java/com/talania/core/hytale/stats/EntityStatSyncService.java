package com.talania.core.hytale.stats;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatType;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Synchronizes Talania stats into Hytale EntityStatMap modifiers.
 *
 * <p>By default, this applies the delta from Talania's StatType defaults
 * to avoid double-counting base values.</p>
 */
public final class EntityStatSyncService {
    public static final String DEFAULT_SOURCE_KEY = "talania-core";

    private final EntityStatModifierService modifierService;
    private final Map<StatType, SyncRule> rules = new EnumMap<>(StatType.class);

    public EntityStatSyncService(EntityStatModifierService modifierService) {
        this.modifierService = modifierService;
        registerDefaultRules();
    }

    public void registerRule(StatType statType, SyncRule rule) {
        rules.put(statType, rule);
    }

    public void applyAll(Ref<EntityStore> ref, Store<EntityStore> store, UUID entityId, EntityStats stats) {
        if (ref == null || store == null || stats == null) {
            return;
        }
        EntityStatMap statMap = (EntityStatMap) store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }
        for (Map.Entry<StatType, SyncRule> entry : rules.entrySet()) {
            StatType statType = entry.getKey();
            SyncRule rule = entry.getValue();
            float value = stats.get(statType);
            float amount = rule.toAmount(value, statType);
            modifierService.applyMaxModifier(
                    statMap,
                    rule.targetId,
                    amount,
                    rule.calculationType,
                    rule.sourceKey
            );
        }
    }

    public void clearAll(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        EntityStatMap statMap = (EntityStatMap) store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) {
            return;
        }
        for (SyncRule rule : rules.values()) {
            modifierService.removeModifier(statMap, rule.targetId, rule.sourceKey);
        }
    }

    private void registerDefaultRules() {
        registerRule(StatType.HEALTH, new SyncRule(EntityStatModifierId.HEALTH_MAX));
        registerRule(StatType.MANA, new SyncRule(EntityStatModifierId.MANA_MAX));
        registerRule(StatType.STAMINA, new SyncRule(EntityStatModifierId.STAMINA_MAX));
    }

    public static final class SyncRule {
        private final EntityStatModifierId targetId;
        private StaticModifier.CalculationType calculationType = StaticModifier.CalculationType.ADDITIVE;
        private String sourceKey = DEFAULT_SOURCE_KEY;
        private boolean useDeltaFromDefault = true;

        public SyncRule(EntityStatModifierId targetId) {
            this.targetId = targetId;
        }

        public SyncRule calculationType(StaticModifier.CalculationType calculationType) {
            this.calculationType = calculationType;
            return this;
        }

        public SyncRule sourceKey(String sourceKey) {
            this.sourceKey = sourceKey;
            return this;
        }

        public SyncRule useDeltaFromDefault(boolean useDeltaFromDefault) {
            this.useDeltaFromDefault = useDeltaFromDefault;
            return this;
        }

        float toAmount(float statValue, StatType statType) {
            if (!useDeltaFromDefault) {
                return statValue;
            }
            return statValue - statType.getDefaultValue();
        }
    }
}
