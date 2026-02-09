package com.talania.core.hytale.stats;

/**
 * Metadata for a supported entity stat modifier.
 */
public final class EntityStatModifierDefinition {
    private final EntityStatModifierId id;
    private final String statId;
    private final String name;
    private final String description;

    public EntityStatModifierDefinition(EntityStatModifierId id, String statId, String name, String description) {
        this.id = id;
        this.statId = statId;
        this.name = name;
        this.description = description;
    }

    public EntityStatModifierId id() {
        return id;
    }

    public String statId() {
        return statId;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }
}
