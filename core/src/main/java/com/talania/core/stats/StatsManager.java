package com.talania.core.stats;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Global manager for entity stats.
 * 
 * <p>Provides a centralized registry for tracking stats across all entities.
 * Handles persistence, events, and cross-entity operations.
 * 
 * <p>Usage:
 * <pre>{@code
 * // Get or create stats for an entity
 * EntityStats stats = StatsManager.getOrCreate(entityId);
 * 
 * // Apply race bonuses
 * stats.addModifier(StatModifier.add("race:orc", StatType.HEALTH, 100));
 * 
 * // Listen for stat changes
 * StatsManager.onStatChange(StatType.HEALTH, (id, oldVal, newVal) -> {
 *     System.out.println("Health changed: " + oldVal + " -> " + newVal);
 * });
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class StatsManager {

    private static final Map<UUID, EntityStats> statsRegistry = new ConcurrentHashMap<>();
    private static final Map<StatType, Consumer<StatChangeEvent>> changeListeners = new ConcurrentHashMap<>();

    private StatsManager() {}

    // ==================== REGISTRY ====================

    /**
     * Get stats for an entity, or create new stats if not registered.
     * 
     * @param entityId The entity's unique identifier
     * @return The entity's stats
     */
    public static EntityStats getOrCreate(UUID entityId) {
        return statsRegistry.computeIfAbsent(entityId, k -> new EntityStats());
    }

    /**
     * Get stats for an entity, or null if not registered.
     */
    public static EntityStats get(UUID entityId) {
        return statsRegistry.get(entityId);
    }

    /**
     * Register stats for an entity.
     */
    public static void register(UUID entityId, EntityStats stats) {
        statsRegistry.put(entityId, stats);
    }

    /**
     * Unregister an entity's stats.
     * 
     * @return The removed stats, or null if not registered
     * @deprecated Use {@link #remove(UUID)} instead
     */
    @Deprecated
    public static EntityStats unregister(UUID entityId) {
        return statsRegistry.remove(entityId);
    }

    /**
     * Remove an entity's stats from the registry.
     * 
     * @param entityId The entity's UUID
     * @return The removed stats, or null if not registered
     */
    public static EntityStats remove(UUID entityId) {
        return statsRegistry.remove(entityId);
    }

    /**
     * Check if an entity has registered stats.
     */
    public static boolean has(UUID entityId) {
        return statsRegistry.containsKey(entityId);
    }

    /**
     * Clear all registered stats.
     */
    public static void clear() {
        statsRegistry.clear();
    }

    // ==================== BULK OPERATIONS ====================

    /**
     * Apply a modifier to all registered entities.
     * 
     * @param modifier The modifier to apply
     */
    public static void applyToAll(StatModifier modifier) {
        for (EntityStats stats : statsRegistry.values()) {
            stats.addModifier(modifier);
        }
    }

    /**
     * Remove modifiers from a source across all entities.
     * 
     * @param source The source identifier
     * @return Total number of modifiers removed
     */
    public static int removeFromAll(String source) {
        int total = 0;
        for (EntityStats stats : statsRegistry.values()) {
            total += stats.removeModifiersBySource(source);
        }
        return total;
    }

    /**
     * Get the count of registered entities.
     */
    public static int getEntityCount() {
        return statsRegistry.size();
    }

    // ==================== EVENTS ====================

    /**
     * Register a listener for stat changes.
     * Note: Listeners must be manually triggered by calling {@link #notifyChange}.
     */
    public static void onStatChange(StatType type, Consumer<StatChangeEvent> listener) {
        if (type != null && listener != null) {
            changeListeners.put(type, listener);
        }
    }

    /**
     * Notify listeners of a stat change.
     * Call this after modifying stats if you want change events.
     */
    public static void notifyChange(UUID entityId, StatType type, float oldValue, float newValue) {
        Consumer<StatChangeEvent> listener = changeListeners.get(type);
        if (listener != null) {
            listener.accept(new StatChangeEvent(entityId, type, oldValue, newValue));
        }
    }

    // ==================== UTILITY ====================

    /**
     * Get a stat value for an entity, or the default if not registered.
     */
    public static float getStat(UUID entityId, StatType type) {
        EntityStats stats = statsRegistry.get(entityId);
        return stats != null ? stats.get(type) : type.getDefaultValue();
    }

    /**
     * Convenience method to add a modifier to an entity.
     */
    public static void addModifier(UUID entityId, StatModifier modifier) {
        getOrCreate(entityId).addModifier(modifier);
    }

    // ==================== EVENT CLASS ====================

    /**
     * Event fired when a stat changes.
     */
    public static class StatChangeEvent {
        private final UUID entityId;
        private final StatType statType;
        private final float oldValue;
        private final float newValue;

        public StatChangeEvent(UUID entityId, StatType statType, float oldValue, float newValue) {
            this.entityId = entityId;
            this.statType = statType;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public UUID getEntityId() { return entityId; }
        public StatType getStatType() { return statType; }
        public float getOldValue() { return oldValue; }
        public float getNewValue() { return newValue; }
        public float getDelta() { return newValue - oldValue; }
    }
}
