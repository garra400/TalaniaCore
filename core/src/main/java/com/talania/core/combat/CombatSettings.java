package com.talania.core.combat;

/**
 * Global combat settings loaded from server configuration.
 */
public final class CombatSettings {
    private boolean pvpEnabled = true;
    private float playerDamageMultiplier = 1.0f;
    private float playerDamageToPlayerMultiplier = 1.0f;

    /**
     * Whether player-vs-player damage is allowed.
     */
    public boolean pvpEnabled() {
        return pvpEnabled;
    }

    /**
     * Enable or disable PvP damage globally.
     */
    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    /**
     * Global multiplier for player damage against non-players.
     */
    public float playerDamageMultiplier() {
        return playerDamageMultiplier;
    }

    /**
     * Set the global multiplier for player damage against non-players.
     */
    public void setPlayerDamageMultiplier(float playerDamageMultiplier) {
        this.playerDamageMultiplier = sanitizeMultiplier(playerDamageMultiplier);
    }

    /**
     * Global multiplier for player damage against other players.
     */
    public float playerDamageToPlayerMultiplier() {
        return playerDamageToPlayerMultiplier;
    }

    /**
     * Set the global multiplier for player damage against other players.
     */
    public void setPlayerDamageToPlayerMultiplier(float playerDamageToPlayerMultiplier) {
        this.playerDamageToPlayerMultiplier = sanitizeMultiplier(playerDamageToPlayerMultiplier);
    }

    /**
     * Ensure a multiplier is finite and non-negative.
     */
    private float sanitizeMultiplier(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return 1.0f;
        }
        return Math.max(0.0f, value);
    }
}
