package com.talania.core.debug.combat;

import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.talania.core.combat.damage.AttackType;
import com.talania.core.stats.DamageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Immutable combat log entry for a single damage event.
 */
public final class CombatLogEntry {
    private final UUID eventId;
    private final long timestamp;
    private final UUID attackerId;
    private final UUID targetId;
    private final String attackerName;
    private final String targetName;
    private final DamageCause cause;
    private final DamageType damageType;
    private final AttackType attackType;
    private final float baseAmount;
    private final float finalAmount;
    private final boolean cancelled;
    private final String cancelReason;
    private final boolean crit;
    private final float lifesteal;
    private final Float thorns;
    private final Boolean blocked;
    private final float lifeDamage;
    private final float shieldAbsorbed;
    private final List<CombatLogStep> steps;

    private CombatLogEntry(Builder builder) {
        this.eventId = builder.eventId;
        this.timestamp = builder.timestamp;
        this.attackerId = builder.attackerId;
        this.targetId = builder.targetId;
        this.attackerName = builder.attackerName;
        this.targetName = builder.targetName;
        this.cause = builder.cause;
        this.damageType = builder.damageType;
        this.attackType = builder.attackType;
        this.baseAmount = builder.baseAmount;
        this.finalAmount = builder.finalAmount;
        this.cancelled = builder.cancelled;
        this.cancelReason = builder.cancelReason;
        this.crit = builder.crit;
        this.lifesteal = builder.lifesteal;
        this.thorns = builder.thorns;
        this.blocked = builder.blocked;
        this.lifeDamage = Float.isNaN(builder.lifeDamage) ? builder.finalAmount : builder.lifeDamage;
        this.shieldAbsorbed = builder.shieldAbsorbed;
        this.steps = Collections.unmodifiableList(new ArrayList<>(builder.steps));
    }

    public UUID eventId() {
        return eventId;
    }

    public long timestamp() {
        return timestamp;
    }

    public UUID attackerId() {
        return attackerId;
    }

    public UUID targetId() {
        return targetId;
    }

    public String attackerName() {
        return attackerName;
    }

    public String targetName() {
        return targetName;
    }

    public DamageCause cause() {
        return cause;
    }

    public DamageType damageType() {
        return damageType;
    }

    public AttackType attackType() {
        return attackType;
    }

    public float baseAmount() {
        return baseAmount;
    }

    public float finalAmount() {
        return finalAmount;
    }

    /**
     * Total damage after modifiers/reductions, before splitting into life vs shield.
     */
    public float totalDamage() {
        return finalAmount;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public String cancelReason() {
        return cancelReason;
    }

    public boolean crit() {
        return crit;
    }

    public float lifesteal() {
        return lifesteal;
    }

    public Float thorns() {
        return thorns;
    }

    public Boolean blocked() {
        return blocked;
    }

    public float lifeDamage() {
        return lifeDamage;
    }

    public float shieldAbsorbed() {
        return shieldAbsorbed;
    }

    public List<CombatLogStep> steps() {
        return steps;
    }

    public static Builder builder(UUID eventId, UUID attackerId, UUID targetId, float baseAmount) {
        return new Builder(eventId, attackerId, targetId, baseAmount);
    }

    public static final class CombatLogStep {
        private final String label;
        private final float before;
        private final float after;
        private final String formula;

        public CombatLogStep(String label, float before, float after, String formula) {
            this.label = label;
            this.before = before;
            this.after = after;
            this.formula = formula;
        }

        public String label() {
            return label;
        }

        public float before() {
            return before;
        }

        public float after() {
            return after;
        }

        public String formula() {
            return formula;
        }
    }

    public static final class Builder {
        private final UUID eventId;
        private final long timestamp;
        private final UUID attackerId;
        private final UUID targetId;
        private String attackerName;
        private String targetName;
        private final float baseAmount;
        private DamageCause cause;
        private DamageType damageType;
        private AttackType attackType;
        private float finalAmount;
        private boolean cancelled;
        private String cancelReason;
        private boolean crit;
        private float lifesteal;
        private Float thorns;
        private Boolean blocked;
        private float lifeDamage = Float.NaN;
        private float shieldAbsorbed;
        private final List<CombatLogStep> steps = new ArrayList<>();

        private Builder(UUID eventId, UUID attackerId, UUID targetId, float baseAmount) {
            this.eventId = eventId;
            this.timestamp = System.currentTimeMillis();
            this.attackerId = attackerId;
            this.targetId = targetId;
            this.baseAmount = baseAmount;
            this.finalAmount = baseAmount;
        }

        public Builder attackerName(String name) {
            this.attackerName = name;
            return this;
        }

        public Builder targetName(String name) {
            this.targetName = name;
            return this;
        }

        public Builder cause(DamageCause cause) {
            this.cause = cause;
            return this;
        }

        public Builder damageType(DamageType damageType) {
            this.damageType = damageType;
            return this;
        }

        public Builder attackType(AttackType attackType) {
            this.attackType = attackType;
            return this;
        }

        public Builder finalAmount(float amount) {
            this.finalAmount = amount;
            return this;
        }

        public Builder cancelled(String reason) {
            this.cancelled = true;
            this.cancelReason = reason;
            return this;
        }

        public Builder crit(boolean crit) {
            this.crit = crit;
            return this;
        }

        public Builder lifesteal(float lifesteal) {
            this.lifesteal = lifesteal;
            return this;
        }

        public Builder thorns(Float thorns) {
            this.thorns = thorns;
            return this;
        }

        public Builder blocked(Boolean blocked) {
            this.blocked = blocked;
            return this;
        }

        public Builder lifeDamage(float lifeDamage) {
            this.lifeDamage = lifeDamage;
            return this;
        }

        public Builder shieldAbsorbed(float shieldAbsorbed) {
            this.shieldAbsorbed = shieldAbsorbed;
            return this;
        }

        public Builder step(String label, float before, float after, String formula) {
            if (label != null && formula != null && before != after) {
                steps.add(new CombatLogStep(label, before, after, formula));
            }
            return this;
        }

        public CombatLogEntry build() {
            return new CombatLogEntry(this);
        }
    }
}
