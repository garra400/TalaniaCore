package com.talania.core.stats;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a modification to a stat value.
 * 
 * <p>Modifiers have an operation type that determines how they stack:
 * <ul>
 *   <li>{@link Operation#ADD} - Added to base value</li>
 *   <li>{@link Operation#MULTIPLY_BASE} - Multiplies the base value</li>
 *   <li>{@link Operation#MULTIPLY_TOTAL} - Multiplies the final total</li>
 * </ul>
 * 
 * <p>Calculation order: (base + additive) * multiply_base * multiply_total
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public class StatModifier implements Comparable<StatModifier> {

    /**
     * How the modifier is applied to the stat.
     */
    public enum Operation {
        /** Added directly to the base value */
        ADD(0),
        /** Multiplies the base value (before other multipliers) */
        MULTIPLY_BASE(1),
        /** Multiplies the total (after all other calculations) */
        MULTIPLY_TOTAL(2);

        private final int priority;

        Operation(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    private final UUID id;
    private final String source;
    private final StatType statType;
    private final float value;
    private final Operation operation;
    private final int priority;
    private final boolean persistent;

    /**
     * Create a new stat modifier.
     * 
     * @param source Identifier for the source (e.g., "race:dwarf", "item:iron_sword")
     * @param statType The stat being modified
     * @param value The modification value
     * @param operation How the modifier is applied
     */
    public StatModifier(String source, StatType statType, float value, Operation operation) {
        this(UUID.randomUUID(), source, statType, value, operation, 0, true);
    }

    /**
     * Full constructor for stat modifier.
     * 
     * @param id Unique identifier
     * @param source Identifier for the source
     * @param statType The stat being modified
     * @param value The modification value
     * @param operation How the modifier is applied
     * @param priority Order priority (higher = applied later)
     * @param persistent Whether this modifier persists across sessions
     */
    public StatModifier(UUID id, String source, StatType statType, float value, 
                        Operation operation, int priority, boolean persistent) {
        this.id = id != null ? id : UUID.randomUUID();
        this.source = source != null ? source : "unknown";
        this.statType = Objects.requireNonNull(statType, "statType cannot be null");
        this.value = value;
        this.operation = operation != null ? operation : Operation.ADD;
        this.priority = priority;
        this.persistent = persistent;
    }

    // ==================== GETTERS ====================

    public UUID getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public StatType getStatType() {
        return statType;
    }

    public float getValue() {
        return value;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isPersistent() {
        return persistent;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Create an additive modifier.
     */
    public static StatModifier add(String source, StatType stat, float value) {
        return new StatModifier(source, stat, value, Operation.ADD);
    }

    /**
     * Create a base multiplier modifier.
     */
    public static StatModifier multiplyBase(String source, StatType stat, float value) {
        return new StatModifier(source, stat, value, Operation.MULTIPLY_BASE);
    }

    /**
     * Create a total multiplier modifier.
     */
    public static StatModifier multiplyTotal(String source, StatType stat, float value) {
        return new StatModifier(source, stat, value, Operation.MULTIPLY_TOTAL);
    }

    /**
     * Create a temporary (non-persistent) modifier.
     */
    public static StatModifier temporary(String source, StatType stat, float value, Operation op) {
        return new StatModifier(UUID.randomUUID(), source, stat, value, op, 0, false);
    }

    // ==================== OBJECT METHODS ====================

    @Override
    public int compareTo(StatModifier other) {
        // First by operation priority, then by custom priority
        int opCompare = Integer.compare(this.operation.priority, other.operation.priority);
        if (opCompare != 0) return opCompare;
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatModifier that = (StatModifier) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("StatModifier[%s %s %.2f %s from %s]",
                statType.getId(),
                operation == Operation.ADD ? "+" : "*",
                value,
                operation.name(),
                source);
    }
}
