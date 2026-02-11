package com.talania.core.progression;

/**
 * Simple linear leveling curve: base XP + (step * currentLevel).
 */
public final class LinearLevelingCurve implements LevelingCurve {
    private final int maxLevel;
    private final long baseXp;
    private final long stepXp;

    public LinearLevelingCurve(int maxLevel, long baseXp, long stepXp) {
        this.maxLevel = Math.max(1, maxLevel);
        this.baseXp = Math.max(1L, baseXp);
        this.stepXp = Math.max(0L, stepXp);
    }

    @Override
    public int maxLevel() {
        return maxLevel;
    }

    @Override
    public long xpForNextLevel(int currentLevel) {
        if (currentLevel >= maxLevel) {
            return 0L;
        }
        int safeLevel = Math.max(0, currentLevel);
        return Math.max(1L, baseXp + stepXp * (long) safeLevel);
    }
}
