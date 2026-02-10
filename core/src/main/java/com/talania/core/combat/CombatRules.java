package com.talania.core.combat;

/**
 * Tunable combat rules applied by the combat modifier system.
 *
 * <p>These are global/default values unless a custom provider is installed.</p>
 */
public final class CombatRules {
    private float sprintDamageMultiplier = 1.0f;
    private float playerDamageMultiplier = 1.0f;
    private float playerDamageToPlayerMultiplier = 1.0f;
    private float flatDamageReduction = 0.0f;
    private float staminaDrainMultiplier = 1.0f;

    public float sprintDamageMultiplier() {
        return sprintDamageMultiplier;
    }

    public CombatRules sprintDamageMultiplier(float sprintDamageMultiplier) {
        this.sprintDamageMultiplier = sprintDamageMultiplier;
        return this;
    }

    public float playerDamageMultiplier() {
        return playerDamageMultiplier;
    }

    public CombatRules playerDamageMultiplier(float playerDamageMultiplier) {
        this.playerDamageMultiplier = playerDamageMultiplier;
        return this;
    }

    public float playerDamageToPlayerMultiplier() {
        return playerDamageToPlayerMultiplier;
    }

    public CombatRules playerDamageToPlayerMultiplier(float playerDamageToPlayerMultiplier) {
        this.playerDamageToPlayerMultiplier = playerDamageToPlayerMultiplier;
        return this;
    }

    public float flatDamageReduction() {
        return flatDamageReduction;
    }

    public CombatRules flatDamageReduction(float flatDamageReduction) {
        this.flatDamageReduction = flatDamageReduction;
        return this;
    }

    public float staminaDrainMultiplier() {
        return staminaDrainMultiplier;
    }

    public CombatRules staminaDrainMultiplier(float staminaDrainMultiplier) {
        this.staminaDrainMultiplier = staminaDrainMultiplier;
        return this;
    }
}
