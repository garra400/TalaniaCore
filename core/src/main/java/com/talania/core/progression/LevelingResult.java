package com.talania.core.progression;

/**
 * Result of applying experience to a progression state.
 */
public record LevelingResult(
        int previousLevel,
        int newLevel,
        int levelsGained,
        long remainingXp,
        boolean maxLevelReached
) {
}
