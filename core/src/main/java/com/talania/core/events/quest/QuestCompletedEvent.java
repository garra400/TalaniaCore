package com.talania.core.events.quest;

import java.util.UUID;

/**
 * Event published when a player completes (turns in) a quest.
 * Published by TalaniaNPC, consumed by TalaniaClassRPG for class XP rewards.
 */
public final class QuestCompletedEvent {

    private final UUID playerUuid;
    private final String playerName;
    private final String questId;
    private final String questType;   // NORMAL, DAILY, WEEKLY, CHAIN
    private final String tier;        // BRONZE, SILVER, GOLD, DIAMOND
    private final int xpReward;
    private final int tokenReward;

    public QuestCompletedEvent(UUID playerUuid, String playerName, String questId,
                               String questType, String tier, int xpReward, int tokenReward) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.questId = questId;
        this.questType = questType;
        this.tier = tier;
        this.xpReward = xpReward;
        this.tokenReward = tokenReward;
    }

    public UUID playerUuid() { return playerUuid; }
    public String playerName() { return playerName; }
    public String questId() { return questId; }
    public String questType() { return questType; }
    public String tier() { return tier; }
    public int xpReward() { return xpReward; }
    public int tokenReward() { return tokenReward; }
}
