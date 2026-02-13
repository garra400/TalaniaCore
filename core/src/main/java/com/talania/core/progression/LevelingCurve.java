package com.talania.core.progression;

/**
 * Strategy interface for computing XP thresholds per level.
 */
public interface LevelingCurve {

    /**
     * XP required to advance from the given level to the next.
     */
    long xpForLevel(int level);

    /**
     * Maximum level reachable with this curve.
     */
    int maxLevel();
}
