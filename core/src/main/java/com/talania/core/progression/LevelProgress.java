package com.talania.core.progression;

/**
 * Mutable level progress tracker for a specific progression context (e.g. a class).
 */
public final class LevelProgress {
    private int level;
    private long xp;

    public LevelProgress() {
        this(0, 0L);
    }

    public LevelProgress(int level, long xp) {
        this.level = level;
        this.xp = xp;
    }

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long xp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public void addXp(long amount) {
        this.xp += amount;
    }
}
