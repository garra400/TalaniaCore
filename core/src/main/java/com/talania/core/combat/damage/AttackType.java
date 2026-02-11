package com.talania.core.combat.damage;

import com.talania.core.stats.StatType;

/**
 * High-level attack channels used for class and race modifiers.
 *
 * <p>Attack types describe how the damage was delivered (melee, ranged, magic),
 * while damage types describe the element/energy (fire, poison, etc.).</p>
 */
public enum AttackType {
    MELEE(StatType.MELEE_DAMAGE_MULT, StatType.MELEE_DAMAGE_TAKEN_MULT),
    RANGED(StatType.RANGED_DAMAGE_MULT, StatType.RANGED_DAMAGE_TAKEN_MULT),
    MAGIC(StatType.MAGIC_DAMAGE_MULT, StatType.MAGIC_DAMAGE_TAKEN_MULT);

    private final StatType damageStat;
    private final StatType damageTakenStat;

    AttackType(StatType damageStat, StatType damageTakenStat) {
        this.damageStat = damageStat;
        this.damageTakenStat = damageTakenStat;
    }

    /**
     * Stat that scales outgoing damage for this attack type.
     */
    public StatType damageStat() {
        return damageStat;
    }

    /**
     * Stat that scales incoming damage for this attack type.
     */
    public StatType damageTakenStat() {
        return damageTakenStat;
    }
}
