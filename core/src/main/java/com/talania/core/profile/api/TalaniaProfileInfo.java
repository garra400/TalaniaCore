package com.talania.core.profile.api;

import java.util.Map;
import java.util.UUID;

/**
 * Snapshot of a player's persisted Talania profile state.
 */
public record TalaniaProfileInfo(
        /** Player UUID. */
        UUID playerId,
        /** Stored race identifier, or null if unset. */
        String raceId,
        /** Stored active class identifier, or null if unset. */
        String classId,
        /** Stored base stats keyed by stat ID. */
        Map<String, Float> baseStats,
        /** Stored class progression keyed by class ID. */
        Map<String, ClassProgressInfo> classProgress
) {
}
