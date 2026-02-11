package com.talania.core.profile;

import com.talania.core.profile.api.TalaniaProfileApi;
import com.talania.core.profile.api.TalaniaProfileInfo;
import com.talania.core.stats.StatType;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runtime manager for loaded player profiles.
 *
 * <p>This class keeps profiles in memory and persists them through
 * {@link TalaniaProfileStore}. It does not hook into player lifecycle
 * events automatically; call {@link #load(UUID)} and {@link #unload(UUID, boolean)}
 * from your login/logout handlers.</p>
 */
public final class TalaniaProfileRuntime implements TalaniaProfileApi {
    private final TalaniaProfileStore store;
    private final Map<UUID, TalaniaPlayerProfile> loaded = new ConcurrentHashMap<>();

    public TalaniaProfileRuntime(Path dataDirectory) {
        this.store = new TalaniaProfileStore(dataDirectory);
    }

    /**
     * Load a profile into memory (or return existing loaded profile).
     */
    public TalaniaPlayerProfile load(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return loaded.computeIfAbsent(playerId, store::loadProfile);
    }

    /**
     * Get a loaded profile, or null if it has not been loaded.
     */
    public TalaniaPlayerProfile get(UUID playerId) {
        return playerId != null ? loaded.get(playerId) : null;
    }

    /**
     * Save a loaded profile back to disk.
     */
    public void save(UUID playerId) {
        TalaniaPlayerProfile profile = get(playerId);
        if (profile != null) {
            store.saveProfile(profile);
        }
    }

    /**
     * Unload a profile, optionally saving it first.
     */
    public void unload(UUID playerId, boolean save) {
        if (playerId == null) {
            return;
        }
        TalaniaPlayerProfile profile = loaded.remove(playerId);
        if (save && profile != null) {
            store.saveProfile(profile);
        }
    }

    /**
     * Build a snapshot of a player's stored profile data.
     */
    @Override
    public TalaniaProfileInfo getProfile(UUID playerId) {
        TalaniaPlayerProfile profile = get(playerId);
        if (profile == null) {
            return null;
        }
        Map<String, Float> stats = new HashMap<>();
        for (Map.Entry<StatType, Float> entry : profile.baseStats().entrySet()) {
            stats.put(entry.getKey().getId(), entry.getValue());
        }
        return new TalaniaProfileInfo(profile.playerId(), profile.raceId(), stats);
    }
}
