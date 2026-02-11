package com.talania.core.progression;

/**
 * Mutable progression state (level + XP towards next level).
 */
public final class LevelProgress {
    private int level;
    private long xp;

    public LevelProgress(int level, long xp) {
        this.level = Math.max(0, level);
        this.xp = Math.max(0L, xp);
    }

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
    }

    public long xp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = Math.max(0L, xp);
    }
}
