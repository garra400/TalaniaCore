package com.talania.core.progression;

/**
 * Result of an XP addition operation.
 */
public final class LevelingResult {
    private final int oldLevel;
    private final int newLevel;
    private final int levelsGained;
    private final long xpAdded;
    private final boolean leveledUp;

    public LevelingResult(int oldLevel, int newLevel, int levelsGained, long xpAdded, boolean leveledUp) {
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.levelsGained = levelsGained;
        this.xpAdded = xpAdded;
        this.leveledUp = leveledUp;
    }

    public int oldLevel() {
        return oldLevel;
    }

    public int newLevel() {
        return newLevel;
    }

    public int levelsGained() {
        return levelsGained;
    }

    public long xpAdded() {
        return xpAdded;
    }

    public boolean leveledUp() {
        return leveledUp;
    }
}
