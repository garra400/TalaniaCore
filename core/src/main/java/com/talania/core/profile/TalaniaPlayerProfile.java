package com.talania.core.profile;

import com.talania.core.progression.LevelProgress;
import com.talania.core.stats.StatType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent player profile for Talania.
 */
public final class TalaniaPlayerProfile {
    private final UUID playerId;
    private int profileVersion;
    private String raceId;
    private final Map<StatType, Float> baseStats = new EnumMap<>(StatType.class);
    private final Map<String, LevelProgress> classProgress = new HashMap<>();

    public TalaniaPlayerProfile(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() {
        return playerId;
    }

    /**
     * Schema version for migration handling.
     */
    public int profileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(int profileVersion) {
        this.profileVersion = profileVersion;
    }

    /**
     * Race identifier stored in the profile (e.g., "human").
     */
    public String raceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    /**
     * Raw base stats stored for this player.
     */
    public Map<StatType, Float> baseStats() {
        return baseStats;
    }

    /**
     * Read a base stat value with fallback.
     */
    public float getBaseStat(StatType stat, float fallback) {
        return baseStats.getOrDefault(stat, fallback);
    }

    /**
     * Set a base stat value.
     */
    public void setBaseStat(StatType stat, float value) {
        baseStats.put(stat, value);
    }

    /**
     * Get or create level progress for a specific class.
     */
    public LevelProgress getOrCreateClassProgress(String classId) {
        return classProgress.computeIfAbsent(classId, id -> new LevelProgress());
    }

    /**
     * All class progress entries.
     */
    public Map<String, LevelProgress> classProgress() {
        return classProgress;
    }
}
