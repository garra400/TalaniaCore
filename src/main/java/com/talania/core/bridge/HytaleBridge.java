package com.talania.core.bridge;

import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Bridge for synchronizing TalaniaCore stats with Hytale's native stat system.
 * 
 * <p>Provides hooks for integrating with Hytale's EntityStatMap and other
 * native systems. Mods should configure the sync handlers during initialization.
 * 
 * <p>Usage:
 * <pre>{@code
 * // Configure sync handlers on mod init
 * HytaleBridge.setToHytaleHandler((entityId, stats) -> {
 *     EntityStatMap nativeStats = getNativeStats(entityId);
 *     for (StatType type : stats.getDefinedStats()) {
 *         nativeStats.set(type.getHytaleId(), stats.get(type));
 *     }
 * });
 * 
 * HytaleBridge.setFromHytaleHandler((entityId) -> {
 *     EntityStatMap nativeStats = getNativeStats(entityId);
 *     EntityStats stats = StatsManager.getOrCreate(entityId);
 *     // Sync relevant values from native
 *     return stats;
 * });
 * 
 * // Later, sync stats
 * HytaleBridge.syncToHytale(playerUUID);
 * HytaleBridge.syncFromHytale(playerUUID);
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class HytaleBridge {

    private static BiConsumer<UUID, EntityStats> toHytaleHandler;
    private static Function<UUID, EntityStats> fromHytaleHandler;
    private static boolean enabled = false;

    private HytaleBridge() {}

    // ==================== CONFIGURATION ====================

    /**
     * Set the handler for syncing TalaniaCore stats TO Hytale's native system.
     * 
     * @param handler A consumer that receives the entity UUID and stats to sync
     */
    public static void setToHytaleHandler(BiConsumer<UUID, EntityStats> handler) {
        toHytaleHandler = handler;
        updateEnabled();
    }

    /**
     * Set the handler for syncing FROM Hytale's native system to TalaniaCore.
     * 
     * @param handler A function that receives entity UUID and returns synced stats
     */
    public static void setFromHytaleHandler(Function<UUID, EntityStats> handler) {
        fromHytaleHandler = handler;
        updateEnabled();
    }

    /**
     * Check if the bridge is enabled (has handlers configured).
     */
    public static boolean isEnabled() {
        return enabled;
    }

    private static void updateEnabled() {
        enabled = (toHytaleHandler != null || fromHytaleHandler != null);
    }

    // ==================== SYNC OPERATIONS ====================

    /**
     * Sync an entity's TalaniaCore stats TO Hytale's native stat system.
     * 
     * @param entityId The entity's UUID
     * @return true if sync was performed, false if handler not configured or entity not found
     */
    public static boolean syncToHytale(UUID entityId) {
        if (toHytaleHandler == null || entityId == null) {
            return false;
        }

        EntityStats stats = StatsManager.get(entityId);
        if (stats == null) {
            return false;
        }

        try {
            toHytaleHandler.accept(entityId, stats);
            return true;
        } catch (Exception e) {
            // Log error in production
            return false;
        }
    }

    /**
     * Sync stats FROM Hytale's native system to TalaniaCore.
     * 
     * @param entityId The entity's UUID
     * @return The synced EntityStats, or null if handler not configured
     */
    public static EntityStats syncFromHytale(UUID entityId) {
        if (fromHytaleHandler == null || entityId == null) {
            return null;
        }

        try {
            return fromHytaleHandler.apply(entityId);
        } catch (Exception e) {
            // Log error in production
            return null;
        }
    }

    /**
     * Sync all registered entities TO Hytale's native system.
     * 
     * @return Number of entities synced
     */
    public static int syncAllToHytale() {
        if (toHytaleHandler == null) {
            return 0;
        }

        int count = 0;
        // Note: In real implementation, iterate StatsManager registry
        // This is a placeholder that requires StatsManager to expose iteration
        return count;
    }

    /**
     * Perform a bidirectional sync for an entity.
     * First syncs FROM Hytale, then applies TalaniaCore modifiers, then syncs back TO Hytale.
     * 
     * @param entityId The entity's UUID
     * @return true if full sync was performed
     */
    public static boolean fullSync(UUID entityId) {
        if (!enabled || entityId == null) {
            return false;
        }

        // First pull from Hytale
        EntityStats stats = syncFromHytale(entityId);
        if (stats == null) {
            stats = StatsManager.getOrCreate(entityId);
        }

        // Then push back to Hytale (with modifiers applied)
        return syncToHytale(entityId);
    }

    // ==================== UTILITY ====================

    /**
     * Reset the bridge configuration.
     * Primarily for testing purposes.
     */
    public static void reset() {
        toHytaleHandler = null;
        fromHytaleHandler = null;
        enabled = false;
    }
}
