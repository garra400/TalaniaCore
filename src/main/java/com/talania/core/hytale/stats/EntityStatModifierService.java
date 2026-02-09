package com.talania.core.hytale.stats;

import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Applies namespaced EntityStatMap modifiers and keeps stable stat indices.
 */
public final class EntityStatModifierService {
    private static final String KEY_PREFIX = "TalaniaCore";
    private final EntityStatModifierRegistry registry;
    private final Map<EntityStatModifierId, String> statIdMap = new EnumMap<>(EntityStatModifierId.class);
    private final Map<EntityStatModifierId, Integer> statIndexMap = new EnumMap<>(EntityStatModifierId.class);

    public EntityStatModifierService(EntityStatModifierRegistry registry) {
        this.registry = registry;
        populateStatIdMap();
    }

    public EntityStatModifierRegistry registry() {
        return registry;
    }

    public void refreshStatIndexMap() {
        if (EntityStatType.getAssetStore() == null) {
            return;
        }
        statIndexMap.clear();
        for (Map.Entry<EntityStatModifierId, String> entry : statIdMap.entrySet()) {
            String statId = entry.getValue();
            int index = EntityStatType.getAssetMap().getIndex(statId);
            if (index >= 0) {
                statIndexMap.put(entry.getKey(), index);
            }
        }
    }

    public boolean applyMaxModifier(EntityStatMap statMap,
                                    EntityStatModifierId modifierId,
                                    float amount,
                                    StaticModifier.CalculationType calculationType,
                                    String sourceKey) {
        EntityStatModifierDefinition definition = registry.get(modifierId);
        if (definition == null) {
            return false;
        }
        Integer statIndex = resolveStatIndex(modifierId);
        if (statIndex == null || statIndex == Integer.MIN_VALUE) {
            return false;
        }
        StaticModifier modifier = new StaticModifier(Modifier.ModifierTarget.MAX, calculationType, amount);
        String key = keyFor(modifierId, sourceKey);
        statMap.putModifier(EntityStatMap.Predictable.SELF, statIndex, key, modifier);
        return true;
    }

    public void removeModifier(EntityStatMap statMap, EntityStatModifierId modifierId, String sourceKey) {
        Integer statIndex = resolveStatIndex(modifierId);
        if (statIndex == null || statIndex == Integer.MIN_VALUE) {
            return;
        }
        statMap.removeModifier(EntityStatMap.Predictable.SELF, statIndex, keyFor(modifierId, sourceKey));
    }

    @Nullable
    public Integer statIndexFor(EntityStatModifierId modifierId) {
        return resolveStatIndex(modifierId);
    }

    private static String keyFor(EntityStatModifierId modifierId, String sourceKey) {
        return KEY_PREFIX + ":" + modifierId.name() + ":" + sourceKey;
    }

    private void populateStatIdMap() {
        statIdMap.put(EntityStatModifierId.HEALTH_MAX, "Health");
        statIdMap.put(EntityStatModifierId.STAMINA_MAX, "Stamina");
        statIdMap.put(EntityStatModifierId.MANA_MAX, "Mana");
        statIdMap.put(EntityStatModifierId.OXYGEN_MAX, "Oxygen");
        statIdMap.put(EntityStatModifierId.SIGNATURE_ENERGY_MAX, "SignatureEnergy");
        statIdMap.put(EntityStatModifierId.AMMO_MAX, "Ammo");
    }

    @Nullable
    private Integer resolveStatIndex(EntityStatModifierId modifierId) {
        Integer statIndex = statIndexMap.get(modifierId);
        if (statIndex == null) {
            refreshStatIndexMap();
            return statIndexMap.get(modifierId);
        }
        String expectedId = statIdMap.get(modifierId);
        if (expectedId == null) {
            return statIndex;
        }
        if (EntityStatType.getAssetStore() == null) {
            return statIndex;
        }
        EntityStatType asset = EntityStatType.getAssetMap().getAsset(statIndex);
        if (asset == null || !Objects.equals(asset.getId(), expectedId)) {
            refreshStatIndexMap();
        }
        return statIndexMap.get(modifierId);
    }
}
