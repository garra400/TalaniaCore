package com.talania.core.progression;

/**
 * Service that applies XP to a {@link LevelProgress} using a {@link LevelingCurve}.
 */
public final class LevelingService {

    public LevelingService() {
    }

    /**
     * Add XP to the given progress and level up as needed.
     *
     * @param progress the progress to modify
     * @param amount   XP to add
     * @param curve    the leveling curve to use
     * @return result describing what happened
     */
    public LevelingResult addXp(LevelProgress progress, long amount, LevelingCurve curve) {
        if (progress == null || curve == null || amount <= 0) {
            return new LevelingResult(
                    progress == null ? 0 : progress.level(),
                    progress == null ? 0 : progress.level(),
                    0, 0L, false);
        }

        int oldLevel = progress.level();
        progress.addXp(amount);

        while (progress.level() < curve.maxLevel()) {
            long required = curve.xpForLevel(progress.level());
            if (progress.xp() < required) {
                break;
            }
            progress.setXp(progress.xp() - required);
            progress.setLevel(progress.level() + 1);
        }

        // Cap at max level
        if (progress.level() >= curve.maxLevel()) {
            progress.setLevel(curve.maxLevel());
        }

        int newLevel = progress.level();
        int levelsGained = newLevel - oldLevel;
        return new LevelingResult(oldLevel, newLevel, levelsGained, amount, levelsGained > 0);
    }
}
