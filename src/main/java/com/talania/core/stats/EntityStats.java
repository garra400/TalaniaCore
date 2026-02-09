package com.talania.core.stats;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages stats and modifiers for a single entity.
 * 
 * <p>This class tracks base values and modifiers, calculating
 * final stat values on demand. Thread-safe for concurrent access.
 * 
 * <p>Usage:
 * <pre>{@code
 * EntityStats stats = new EntityStats();
 * 
 * // Set base values
 * stats.setBase(StatType.HEALTH, 100);
 * 
 * // Add modifiers
 * stats.addModifier(StatModifier.add("race:dwarf", StatType.HEALTH, 50));
 * stats.addModifier(StatModifier.multiplyBase("buff:strength", StatType.ATTACK, 1.2f));
 * 
 * // Get final value
 * float maxHealth = stats.get(StatType.HEALTH); // 150
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public class EntityStats {

    private final Map<StatType, Float> baseValues = new ConcurrentHashMap<>();
    private final Map<StatType, List<StatModifier>> modifiers = new ConcurrentHashMap<>();
    private final Map<StatType, Float> cachedValues = new ConcurrentHashMap<>();
    private volatile boolean dirty = true;

    /**
     * Create new entity stats with default values.
     */
    public EntityStats() {
        // Initialize with defaults
        for (StatType type : StatType.values()) {
            baseValues.put(type, type.getDefaultValue());
        }
    }

    // ==================== BASE VALUES ====================

    /**
     * Set the base value for a stat.
     * 
     * @param type The stat type
     * @param value The base value
     */
    public void setBase(StatType type, float value) {
        baseValues.put(type, type.clamp(value));
        invalidateCache(type);
    }

    /**
     * Get the base value for a stat (before modifiers).
     */
    public float getBase(StatType type) {
        return baseValues.getOrDefault(type, type.getDefaultValue());
    }

    /**
     * Add to the base value of a stat.
     */
    public void addBase(StatType type, float amount) {
        setBase(type, getBase(type) + amount);
    }

    // ==================== MODIFIERS ====================

    /**
     * Add a modifier to this entity's stats.
     * 
     * @param modifier The modifier to add
     */
    public void addModifier(StatModifier modifier) {
        if (modifier == null) return;
        
        modifiers.computeIfAbsent(modifier.getStatType(), k -> new ArrayList<>())
                 .add(modifier);
        
        // Keep modifiers sorted
        modifiers.get(modifier.getStatType()).sort(Comparator.naturalOrder());
        invalidateCache(modifier.getStatType());
    }

    /**
     * Remove a modifier by its ID.
     * 
     * @param modifierId The modifier's unique ID
     * @return true if a modifier was removed
     */
    public boolean removeModifier(UUID modifierId) {
        for (Map.Entry<StatType, List<StatModifier>> entry : modifiers.entrySet()) {
            if (entry.getValue().removeIf(m -> m.getId().equals(modifierId))) {
                invalidateCache(entry.getKey());
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all modifiers from a specific source.
     * 
     * @param source The source identifier (e.g., "race:dwarf")
     * @return Number of modifiers removed
     */
    public int removeModifiersBySource(String source) {
        int removed = 0;
        for (Map.Entry<StatType, List<StatModifier>> entry : modifiers.entrySet()) {
            int before = entry.getValue().size();
            entry.getValue().removeIf(m -> m.getSource().equals(source));
            int after = entry.getValue().size();
            if (before != after) {
                invalidateCache(entry.getKey());
                removed += (before - after);
            }
        }
        return removed;
    }

    /**
     * Get all modifiers for a stat type.
     */
    public List<StatModifier> getModifiers(StatType type) {
        return new ArrayList<>(modifiers.getOrDefault(type, Collections.emptyList()));
    }

    /**
     * Get all modifiers from a specific source.
     */
    public List<StatModifier> getModifiersBySource(String source) {
        List<StatModifier> result = new ArrayList<>();
        for (List<StatModifier> list : modifiers.values()) {
            for (StatModifier m : list) {
                if (m.getSource().equals(source)) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    /**
     * Clear all modifiers.
     */
    public void clearModifiers() {
        modifiers.clear();
        dirty = true;
        cachedValues.clear();
    }

    /**
     * Clear only non-persistent modifiers.
     */
    public void clearTemporaryModifiers() {
        for (Map.Entry<StatType, List<StatModifier>> entry : modifiers.entrySet()) {
            if (entry.getValue().removeIf(m -> !m.isPersistent())) {
                invalidateCache(entry.getKey());
            }
        }
    }

    // ==================== CALCULATED VALUES ====================

    /**
     * Get the final calculated value for a stat.
     * Applies all modifiers in order: ADD, MULTIPLY_BASE, MULTIPLY_TOTAL
     * 
     * @param type The stat type
     * @return The final stat value
     */
    public float get(StatType type) {
        if (!dirty && cachedValues.containsKey(type)) {
            return cachedValues.get(type);
        }

        float value = calculate(type);
        cachedValues.put(type, value);
        return value;
    }

    /**
     * Calculate stat value without caching.
     */
    private float calculate(StatType type) {
        float base = getBase(type);
        List<StatModifier> mods = modifiers.getOrDefault(type, Collections.emptyList());

        if (mods.isEmpty()) {
            return base;
        }

        float additive = 0;
        float multiplyBase = 1.0f;
        float multiplyTotal = 1.0f;

        for (StatModifier mod : mods) {
            switch (mod.getOperation()) {
                case ADD -> additive += mod.getValue();
                case MULTIPLY_BASE -> multiplyBase *= mod.getValue();
                case MULTIPLY_TOTAL -> multiplyTotal *= mod.getValue();
            }
        }

        float result = (base + additive) * multiplyBase * multiplyTotal;
        return type.clamp(result);
    }

    /**
     * Invalidate cached value for a stat.
     */
    private void invalidateCache(StatType type) {
        cachedValues.remove(type);
        dirty = true;
    }

    /**
     * Recalculate all cached values.
     */
    public void recalculate() {
        cachedValues.clear();
        for (StatType type : StatType.values()) {
            cachedValues.put(type, calculate(type));
        }
        dirty = false;
    }

    // ==================== UTILITY ====================

    /**
     * Copy all stats and modifiers to a new instance.
     */
    public EntityStats copy() {
        EntityStats copy = new EntityStats();
        copy.baseValues.putAll(this.baseValues);
        for (Map.Entry<StatType, List<StatModifier>> entry : this.modifiers.entrySet()) {
            copy.modifiers.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Get a summary of all stats for debugging.
     */
    public Map<String, Float> toMap() {
        Map<String, Float> result = new LinkedHashMap<>();
        for (StatType type : StatType.values()) {
            result.put(type.getId(), get(type));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EntityStats{\n");
        for (StatType type : StatType.values()) {
            float base = getBase(type);
            float final_ = get(type);
            if (base != type.getDefaultValue() || final_ != base) {
                sb.append(String.format("  %s: %.1f (base: %.1f)\n", 
                    type.getId(), final_, base));
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
