package com.talania.core.stats;

/**
 * Core stat types used across the Orbis and Dungeons ecosystem.
 * 
 * <p>Defines the fundamental attributes that can be modified by
 * races, classes, equipment, and effects.
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public enum StatType {
    
    // ==================== VITALS ====================
    
    /** Maximum health points */
    HEALTH("health", 100.0f, 0.0f, 10000.0f),
    
    /** Maximum mana/magic points */
    MANA("mana", 100.0f, 0.0f, 10000.0f),
    
    /** Maximum stamina/energy */
    STAMINA("stamina", 10.0f, 0.0f, 1000.0f),
    
    // ==================== OFFENSE ====================
    
    /** Physical attack power multiplier */
    ATTACK("attack", 1.0f, 0.0f, 100.0f),
    
    /** Magic attack power multiplier */
    MAGIC_ATTACK("magic_attack", 1.0f, 0.0f, 100.0f),

    /** Melee damage multiplier */
    MELEE_DAMAGE_MULT("melee_damage_mult", 1.0f, 0.0f, 10.0f),

    /** Ranged damage multiplier */
    RANGED_DAMAGE_MULT("ranged_damage_mult", 1.0f, 0.0f, 10.0f),

    /** Magic damage multiplier (attack channel is magic) */
    MAGIC_DAMAGE_MULT("magic_damage_mult", 1.0f, 0.0f, 10.0f),

    /** Sprint damage multiplier */
    SPRINT_DAMAGE_MULT("sprint_damage_mult", 1.0f, 0.0f, 10.0f),

    /** Flat damage reduction applied after other reductions */
    FLAT_DAMAGE_REDUCTION("flat_damage_reduction", 0.0f, 0.0f, 1000.0f),

    /** Stamina drain multiplier while blocking */
    STAMINA_DRAIN_MULT("stamina_drain_mult", 1.0f, 0.0f, 10.0f),
    
    /** Critical hit chance (0.0 = 0%, 1.0 = 100%) */
    CRIT_CHANCE("crit_chance", 0.05f, 0.0f, 1.0f),
    
    /** Critical hit damage multiplier */
    CRIT_DAMAGE("crit_damage", 1.5f, 1.0f, 10.0f),
    
    /** Attack speed multiplier */
    ATTACK_SPEED("attack_speed", 1.0f, 0.1f, 10.0f),
    
    /** Lifesteal percent (0.0 = 0%, 1.0 = 100%) */
    LIFESTEAL("lifesteal", 0.0f, 0.0f, 1.0f),
    
    // ==================== DEFENSE ====================
    
    /** Physical damage reduction (0.0 = none, 1.0 = immune) */
    ARMOR("armor", 0.0f, 0.0f, 1.0f),
    
    /** Magic damage reduction */
    MAGIC_RESIST("magic_resist", 0.0f, 0.0f, 1.0f),

    /** Dodge chance (0.0 = 0%, 1.0 = 100%) */
    DODGE_CHANCE("dodge_chance", 0.0f, 0.0f, 1.0f),

    /** Fall damage reduction */
    FALL_RESISTANCE("fall_resistance", 0.0f, 0.0f, 1.0f),

    /** Fire damage reduction */
    FIRE_RESISTANCE("fire_resistance", 0.0f, 0.0f, 1.0f),

    /** Poison damage reduction */
    POISON_RESISTANCE("poison_resistance", 0.0f, 0.0f, 1.0f),

    /** Lightning damage reduction */
    LIGHTNING_RESISTANCE("lightning_resistance", 0.0f, 0.0f, 1.0f),

    /** Holy damage reduction */
    HOLY_RESISTANCE("holy_resistance", 0.0f, 0.0f, 1.0f),

    /** Void damage reduction */
    VOID_RESISTANCE("void_resistance", 0.0f, 0.0f, 1.0f),
    
    /** Blocking efficiency (higher = less stamina drain while blocking) */
    BLOCKING_EFFICIENCY("blocking_efficiency", 1.0f, 0.0f, 5.0f),

    /** Melee damage taken multiplier */
    MELEE_DAMAGE_TAKEN_MULT("melee_damage_taken_mult", 1.0f, 0.0f, 10.0f),

    /** Ranged damage taken multiplier */
    RANGED_DAMAGE_TAKEN_MULT("ranged_damage_taken_mult", 1.0f, 0.0f, 10.0f),

    /** Magic damage taken multiplier (attack channel is magic) */
    MAGIC_DAMAGE_TAKEN_MULT("magic_damage_taken_mult", 1.0f, 0.0f, 10.0f),
    
    // ==================== MOBILITY ====================
    
    /** Movement speed multiplier */
    MOVE_SPEED("move_speed", 1.0f, 0.0f, 10.0f),
    
    /** Jump height multiplier */
    JUMP_HEIGHT("jump_height", 1.0f, 0.0f, 10.0f),
    
    // ==================== UTILITY ====================
    
    /** Health regeneration multiplier (1.0 = normal regen). */
    HEALTH_REGEN("health_regen", 1.0f, 0.0f, 100.0f),

    /** Healing received multiplier (1.0 = normal, 0.5 = 50% healing) */
    HEALING_RECEIVED_MULT("healing_received_mult", 1.0f, 0.0f, 10.0f),
    
    /** Mana regeneration per second */
    MANA_REGEN("mana_regen", 0.0f, 0.0f, 100.0f),
    
    /** Stamina regeneration per second */
    STAMINA_REGEN("stamina_regen", 1.0f, 0.0f, 100.0f),
    
    /** Experience gain multiplier */
    XP_BONUS("xp_bonus", 1.0f, 0.0f, 10.0f),
    
    /** Loot drop rate multiplier */
    LUCK("luck", 1.0f, 0.0f, 10.0f);

    private final String id;
    private final float defaultValue;
    private final float minValue;
    private final float maxValue;

    StatType(String id, float defaultValue, float minValue, float maxValue) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Get the string identifier for this stat.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the default value for this stat.
     */
    public float getDefaultValue() {
        return defaultValue;
    }

    /**
     * Get the minimum allowed value for this stat.
     */
    public float getMinValue() {
        return minValue;
    }

    /**
     * Get the maximum allowed value for this stat.
     */
    public float getMaxValue() {
        return maxValue;
    }

    /**
     * Clamp a value to the valid range for this stat.
     */
    public float clamp(float value) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    /**
     * Find a StatType by its string ID.
     * 
     * @param id The stat ID (e.g., "health", "attack")
     * @return The StatType, or null if not found
     */
    public static StatType fromId(String id) {
        if (id == null) return null;
        for (StatType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
