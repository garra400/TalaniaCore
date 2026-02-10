package com.talania.core.profile.api;

import java.util.UUID;

/**
 * API for accessing Talania profile data.
 */
public interface TalaniaProfileApi {

    /**
     * Get a snapshot of a player's stored profile state.
     *
     * @return profile info or null if not loaded.
     */
    TalaniaProfileInfo getProfile(UUID playerId);
}
