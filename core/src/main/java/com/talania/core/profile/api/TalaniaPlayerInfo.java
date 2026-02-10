package com.talania.core.profile.api;

import java.util.Map;
import java.util.UUID;

/**
 * Snapshot of player state exposed by the Talania API.
 */
public record TalaniaPlayerInfo(
        /** Player UUID. */
        UUID playerId,
        /** Race identifier, or null if none. */
        String raceId,
        /** Current computed stats keyed by stat ID. */
        Map<String, Float> stats
) {
}
