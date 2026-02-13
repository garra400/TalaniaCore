package com.talania.core.progression;

/**
 * Linear leveling curve where each level requires {@code baseXp + (level * stepXp)}.
 */
public final class LinearLevelingCurve implements LevelingCurve {
    private final int maxLevel;
    private final long baseXp;
    private final long stepXp;

    public LinearLevelingCurve(int maxLevel, long baseXp, long stepXp) {
        this.maxLevel = maxLevel;
        this.baseXp = baseXp;
        this.stepXp = stepXp;
    }

    @Override
    public long xpForLevel(int level) {
        return baseXp + ((long) level * stepXp);
    }

    @Override
    public int maxLevel() {
        return maxLevel;
    }
}
