package com.talania.core.stats;

/**
 * High-level damage categories used for resistances and balance rules.
 *
 * <p>This enum does not apply any behavior by itself. It exists to standardize
 * how systems reference damage categories and which resistance stat should be
 * consulted when reducing damage.</p>
 */
public enum DamageType {
    PHYSICAL(StatType.ARMOR),
    ARCANE(StatType.MAGIC_RESIST),
    FIRE(StatType.FIRE_RESISTANCE),
    POISON(StatType.POISON_RESISTANCE),
    LIGHTNING(StatType.LIGHTNING_RESISTANCE),
    HOLY(StatType.HOLY_RESISTANCE),
    VOID(StatType.VOID_RESISTANCE);

    private final StatType resistanceStat;

    DamageType(StatType resistanceStat) {
        this.resistanceStat = resistanceStat;
    }

    /**
     * Stat used to reduce damage of this type.
     */
    public StatType resistanceStat() {
        return resistanceStat;
    }
}
