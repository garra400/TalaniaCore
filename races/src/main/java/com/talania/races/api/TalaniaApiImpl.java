package com.talania.races.api;

import com.talania.core.profile.api.TalaniaApi;
import com.talania.core.profile.api.TalaniaPlayerInfo;
import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;
import com.talania.races.RaceType;
import com.talania.races.RaceService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default Talania API implementation backed by in-memory stats and race state.
 */
public final class TalaniaApiImpl implements TalaniaApi {
    private final RaceService raceService;

    /**
     * Create an API implementation backed by the provided race service.
     */
    public TalaniaApiImpl(RaceService raceService) {
        this.raceService = raceService;
    }

    @Override
    public TalaniaPlayerInfo getPlayerInfo(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        EntityStats stats = StatsManager.get(playerId);
        Map<String, Float> snapshot = new HashMap<>();
        if (stats != null) {
            for (StatType stat : StatType.values()) {
                snapshot.put(stat.getId(), stats.get(stat));
            }
        }
        String raceId = null;
        if (raceService != null) {
            RaceType race = raceService.getRace(playerId);
            raceId = race != null ? race.id() : null;
        }
        return new TalaniaPlayerInfo(playerId, raceId, snapshot);
    }
}
