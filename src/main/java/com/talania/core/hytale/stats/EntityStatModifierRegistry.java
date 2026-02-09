package com.talania.core.hytale.stats;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for supported EntityStat modifiers.
 */
public final class EntityStatModifierRegistry {
    private final Map<EntityStatModifierId, EntityStatModifierDefinition> modifiers =
            new EnumMap<>(EntityStatModifierId.class);

    public void register(EntityStatModifierDefinition definition) {
        modifiers.put(definition.id(), definition);
    }

    public EntityStatModifierDefinition get(EntityStatModifierId id) {
        return modifiers.get(id);
    }

    public Collection<EntityStatModifierDefinition> all() {
        return modifiers.values();
    }

    public void registerDefaults() {
        register(new EntityStatModifierDefinition(
                EntityStatModifierId.HEALTH_MAX,
                "Health",
                "Max Health",
                "Increase/decrease maximum health."
        ));
        register(new EntityStatModifierDefinition(
                EntityStatModifierId.STAMINA_MAX,
                "Stamina",
                "Max Stamina",
                "Increase/decrease maximum stamina."
        ));
        register(new EntityStatModifierDefinition(
                EntityStatModifierId.MANA_MAX,
                "Mana",
                "Max Mana",
                "Increase/decrease maximum mana."
        ));
        register(new EntityStatModifierDefinition(
                EntityStatModifierId.OXYGEN_MAX,
                "Oxygen",
                "Max Oxygen",
                "Increase/decrease maximum oxygen."
        ));
        register(new EntityStatModifierDefinition(
                EntityStatModifierId.SIGNATURE_ENERGY_MAX,
                "SignatureEnergy",
                "Max Signature Energy",
                "Increase/decrease maximum signature energy."
        ));
        register(new EntityStatModifierDefinition(
                EntityStatModifierId.AMMO_MAX,
                "Ammo",
                "Max Ammo",
                "Increase/decrease maximum ammo."
        ));
    }
}
