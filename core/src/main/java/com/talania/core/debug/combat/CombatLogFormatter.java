package com.talania.core.debug.combat;

import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Formatting helpers for combat log display.
 */
public final class CombatLogFormatter {
    private static final DecimalFormat AMOUNT = new DecimalFormat("0.##");

    private CombatLogFormatter() {}

    public static String summary(CombatLogEntry entry) {
        return summaryFor(null, entry, null, null);
    }

    public static String summaryFor(UUID viewerId, CombatLogEntry entry, String attackerName, String targetName) {
        if (entry == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String attackerLabel = attackerName != null ? attackerName : entry.attackerName();
        if (attackerLabel == null || attackerLabel.isBlank()) {
            attackerLabel = "Attacker";
        }
        String targetLabel = targetName != null ? targetName : entry.targetName();
        if (targetLabel == null || targetLabel.isBlank()) {
            targetLabel = "Target";
        }
        if (viewerId != null) {
            if (viewerId.equals(entry.attackerId())) {
                attackerLabel = "You";
            } else if (viewerId.equals(entry.targetId())) {
                targetLabel = "You";
            }
        }
        sb.append(attackerLabel).append(" -> ").append(targetLabel).append(" | ");
        sb.append("Damage ");
        sb.append(formatAmount(entry.finalAmount()));
        if (entry.damageType() != null) {
            sb.append(" [").append(entry.damageType().name()).append("]");
        }
        if (entry.attackType() != null) {
            sb.append(" (").append(entry.attackType().name()).append(")");
        }
        if (entry.cause() != null) {
            sb.append(" cause=").append(entry.cause().toString());
        }
        if (entry.crit()) {
            sb.append(" crit");
        }
        if (entry.lifesteal() > 0.0f) {
            sb.append(" lifesteal=").append(formatAmount(entry.lifesteal()));
        }
        if (entry.thorns() != null && entry.thorns() > 0.0f) {
            sb.append(" thorns=").append(formatAmount(entry.thorns()));
        }
        if (entry.cancelled()) {
            sb.append(" cancelled");
            if (entry.cancelReason() != null) {
                sb.append(" (").append(entry.cancelReason()).append(")");
            }
        }
        return sb.toString();
    }

    public static List<String> modifierLines(CombatLogEntry entry) {
        List<String> lines = new ArrayList<>();
        if (entry == null) {
            return lines;
        }
        for (CombatLogEntry.CombatLogStep step : entry.steps()) {
            StringBuilder sb = new StringBuilder();
            sb.append(step.label()).append(": ");
            sb.append(formatAmount(step.before())).append(" -> ").append(formatAmount(step.after()));
            if (step.formula() != null && !step.formula().isBlank()) {
                sb.append(" (").append(step.formula()).append(")");
            }
            lines.add(sb.toString());
        }
        return lines;
    }

    private static String formatAmount(float value) {
        return AMOUNT.format(value);
    }
}
