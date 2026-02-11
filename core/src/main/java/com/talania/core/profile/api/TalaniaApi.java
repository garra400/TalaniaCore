package com.talania.core.profile.api;

import java.util.UUID;

/**
 * Public API surface for querying Talania player data.
 */
public interface TalaniaApi {

    /**
     * Get a snapshot of a player's current info.
     *
     * @return info snapshot or null if player is unknown.
     */
    TalaniaPlayerInfo getPlayerInfo(UUID playerId);
}
