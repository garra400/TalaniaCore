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
    private String classId;
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
     * Active class identifier stored in the profile (e.g., "swordmaster").
     */
    public String classId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    /**
     * Raw base stats stored for this player.
     */
    public Map<StatType, Float> baseStats() {
        return baseStats;
    }

    /**
     * Stored class progression states keyed by class ID.
     */
    public Map<String, LevelProgress> classProgress() {
        return classProgress;
    }

    /**
     * Get the stored progression for a class, or null if not present.
     */
    public LevelProgress getClassProgress(String classId) {
        return classId == null ? null : classProgress.get(classId);
    }

    /**
     * Get or create a class progression entry with a default level of 0.
     */
    public LevelProgress getOrCreateClassProgress(String classId) {
        if (classId == null || classId.isBlank()) {
            return null;
        }
        return classProgress.computeIfAbsent(classId, ignored -> new LevelProgress(0, 0L));
    }

    /**
     * Read a stored class level with fallback.
     */
    public int getClassLevel(String classId, int fallback) {
        LevelProgress progress = getClassProgress(classId);
        return progress == null ? fallback : progress.level();
    }

    /**
     * Read a stored class XP value with fallback.
     */
    public long getClassXp(String classId, long fallback) {
        LevelProgress progress = getClassProgress(classId);
        return progress == null ? fallback : progress.xp();
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
}
