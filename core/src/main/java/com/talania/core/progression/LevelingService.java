package com.talania.core.progression;

/**
 * Utility for applying experience and handling level-ups.
 */
public final class LevelingService {

    /**
     * Apply experience to a progression state, leveling up as needed.
     */
    public LevelingResult addXp(LevelProgress progress, long amount, LevelingCurve curve) {
        if (progress == null || curve == null) {
            return new LevelingResult(0, 0, 0, 0L, false);
        }
        if (amount <= 0L) {
            return new LevelingResult(progress.level(), progress.level(), 0, progress.xp(), progress.level() >= curve.maxLevel());
        }

        int previousLevel = progress.level();
        int level = previousLevel;
        long xp = progress.xp() + amount;

        int maxLevel = curve.maxLevel();
        while (level < maxLevel) {
            long needed = curve.xpForNextLevel(level);
            if (needed <= 0L) {
                break;
            }
            if (xp < needed) {
                break;
            }
            xp -= needed;
            level++;
        }

        boolean maxed = level >= maxLevel;
        if (maxed) {
            xp = 0L;
        }

        progress.setLevel(level);
        progress.setXp(xp);

        return new LevelingResult(previousLevel, level, level - previousLevel, xp, maxed);
    }
}
