package com.talania.core.progression;

/**
 * Defines how much experience is required to progress through levels.
 */
public interface LevelingCurve {

    /**
     * Maximum achievable level for this curve.
     */
    int maxLevel();

    /**
     * Experience required to advance from the provided level to the next.
     *
     * @param currentLevel current level (0-based)
     * @return experience required to reach the next level
     */
    long xpForNextLevel(int currentLevel);
}
