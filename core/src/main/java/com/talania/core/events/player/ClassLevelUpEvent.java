package com.talania.core.events.player;

import java.util.UUID;

/**
 * Event published when a player's class levels up.
 * Published by TalaniaClassRPG, consumed by TalaniaNPC (party HUD) and other mods.
 */
public final class ClassLevelUpEvent {

    private final UUID playerUuid;
    private final String classId;
    private final int oldLevel;
    private final int newLevel;
    private final long totalXp;

    public ClassLevelUpEvent(UUID playerUuid, String classId, int oldLevel, int newLevel, long totalXp) {
        this.playerUuid = playerUuid;
        this.classId = classId;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.totalXp = totalXp;
    }

    public UUID playerUuid() { return playerUuid; }
    public String classId() { return classId; }
    public int oldLevel() { return oldLevel; }
    public int newLevel() { return newLevel; }
    public int levelsGained() { return newLevel - oldLevel; }
    public long totalXp() { return totalXp; }
}
