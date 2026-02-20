package com.talania.core.debug.combat;

import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Formatting helpers for combat log display.
 */
public final class CombatLogFormatter {
    private static final DecimalFormat AMOUNT = new DecimalFormat("0.##");
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private CombatLogFormatter() {}

    public static String summary(CombatLogEntry entry) {
        return summaryFor(null, entry, null, null);
    }

    public static String summaryFor(UUID viewerId, CombatLogEntry entry, String attackerName, String targetName) {
        if (entry == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String attackerLabel = resolveLabel(viewerId, entry.attackerId(),
                attackerName != null ? attackerName : entry.attackerName(), "Attacker");
        String targetLabel = resolveLabel(viewerId, entry.targetId(),
                targetName != null ? targetName : entry.targetName(), "Target");
        sb.append(formatTimestamp(entry.timestamp())).append(" ");
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
            sb.append(" cause=").append(formatCause(entry.cause()));
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
        if (entry.shieldAbsorbed() > 0.0f) {
            sb.append(" shield=").append(formatAmount(entry.shieldAbsorbed()));
        }
        if (entry.cancelled()) {
            sb.append(" cancelled");
            if (entry.cancelReason() != null) {
                sb.append(" (").append(entry.cancelReason()).append(")");
            }
        }
        return sb.toString();
    }

    public static String actorSummaryFor(UUID viewerId, CombatLogEntry entry, String attackerName, String targetName) {
        if (entry == null) {
            return "";
        }
        String attackerLabel = resolveLabel(viewerId, entry.attackerId(),
                attackerName != null ? attackerName : entry.attackerName(), "Attacker");
        String targetLabel = resolveLabel(viewerId, entry.targetId(),
                targetName != null ? targetName : entry.targetName(), "Target");
        return formatTimestamp(entry.timestamp()) + " " + attackerLabel + " -> " + targetLabel;
    }

    public static String damageText(CombatLogEntry entry) {
        if (entry == null) {
            return "";
        }
        return "Damage " + formatAmount(entry.finalAmount());
    }

    public static String damageTooltip(CombatLogEntry entry) {
        if (entry == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Damage: ").append(formatAmount(entry.finalAmount()));
        if (entry.damageType() != null) {
            sb.append(" [").append(formatEnumName(entry.damageType().name())).append("]");
        }
        if (entry.attackType() != null) {
            sb.append(" (").append(formatEnumName(entry.attackType().name())).append(")");
        }
        if (entry.cause() != null) {
            sb.append("\nCause: ").append(formatCause(entry.cause()));
        }
        if (entry.crit()) {
            sb.append("\nCrit: yes");
        }
        if (entry.lifesteal() > 0.0f) {
            sb.append("\nLifesteal: ").append(formatAmount(entry.lifesteal()));
        }
        if (entry.thorns() != null && entry.thorns() > 0.0f) {
            sb.append("\nThorns: ").append(formatAmount(entry.thorns()));
        }
        if (entry.blocked() != null) {
            sb.append("\nBlocked: ").append(entry.blocked() ? "yes" : "no");
        }
        if (entry.cancelled()) {
            sb.append("\nCancelled");
            if (entry.cancelReason() != null && !entry.cancelReason().isBlank()) {
                sb.append(": ").append(entry.cancelReason());
            }
        }
        sb.append("\n");
        sb.append("\nBase damage: ").append(formatAmount(entry.baseAmount()));
        if (entry.steps().isEmpty()) {
            sb.append(" (no modifiers)");
            appendMitigationSummary(sb, entry, "\n");
        } else {
            for (String line : modifierLines(entry, false)) {
                sb.append("\n").append(line);
            }
        }
        return sb.toString();
    }

    public static List<String> modifierLines(CombatLogEntry entry) {
        return modifierLines(entry, true);
    }

    public static List<String> modifierLines(CombatLogEntry entry, boolean includeTimestamp) {
        List<String> lines = new ArrayList<>();
        if (entry == null) {
            return lines;
        }
        String prefix = includeTimestamp ? formatTimestamp(entry.timestamp()) + " " : "";
        if (entry.steps().isEmpty()) {
            float base = entry.baseAmount();
            float fin = entry.finalAmount();
            if (base == fin) {
                lines.add(prefix + "base: " + formatAmount(base) + " (no modifiers)");
            } else {
                lines.add(prefix + "base: " + formatAmount(base) + " -> " + formatAmount(fin));
            }
            appendMitigationSummary(lines, entry, prefix);
            return lines;
        }
        for (CombatLogEntry.CombatLogStep step : entry.steps()) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append(step.label()).append(": ");
            sb.append(formatAmount(step.before())).append(" -> ").append(formatAmount(step.after()));
            if (step.formula() != null && !step.formula().isBlank()) {
                sb.append(" (").append(step.formula()).append(")");
            }
            lines.add(sb.toString());
        }
        appendMitigationSummary(lines, entry, prefix);
        return lines;
    }

    private static String formatAmount(float value) {
        return AMOUNT.format(value);
    }

    private static String formatTimestamp(long timestamp) {
        return "[" + TIME.format(Instant.ofEpochMilli(timestamp)) + "]";
    }

    private static String resolveLabel(UUID viewerId, UUID subjectId, String label, String fallback) {
        String resolved = label;
        if (resolved == null || resolved.isBlank()) {
            resolved = fallback;
        }
        if (viewerId != null && subjectId != null && viewerId.equals(subjectId)) {
            return "You";
        }
        return resolved;
    }

    private static String formatEnumName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Unknown";
        }
        String cleaned = raw.replace('-', ' ').replace('_', ' ').trim();
        if (cleaned.isEmpty()) {
            return "Unknown";
        }
        String lower = cleaned.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        boolean upper = true;
        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            if (ch == ' ') {
                sb.append(' ');
                upper = true;
                continue;
            }
            sb.append(upper ? Character.toUpperCase(ch) : ch);
            upper = false;
        }
        return sb.toString();
    }

    private static void appendMitigationSummary(StringBuilder sb, CombatLogEntry entry, String prefix) {
        if (entry == null) {
            return;
        }
        float shield = entry.shieldAbsorbed();
        float life = entry.lifeDamage();
        boolean showShield = shield > 0.0f;
        boolean showLife = life > 0.0f;
        if (!showShield && !showLife) {
            return;
        }
        if (showShield) {
            sb.append(prefix).append("Energy shield removed: ").append(formatAmount(shield));
        }
        if (showLife && !(showShield && life <= 0.0f)) {
            sb.append(prefix).append("Life removed: ").append(formatAmount(life));
        }
    }

    private static void appendMitigationSummary(List<String> lines, CombatLogEntry entry, String prefix) {
        if (entry == null) {
            return;
        }
        float shield = entry.shieldAbsorbed();
        float life = entry.lifeDamage();
        boolean showShield = shield > 0.0f;
        boolean showLife = life > 0.0f;
        if (!showShield && !showLife) {
            return;
        }
        if (showShield) {
            lines.add(prefix + "Energy shield removed: " + formatAmount(shield));
        }
        if (showLife && !(showShield && life <= 0.0f)) {
            lines.add(prefix + "Life removed: " + formatAmount(life));
        }
    }

    private static String formatCause(DamageCause cause) {
        if (cause == null) {
            return "Unknown";
        }
        String id = cause.getId();
        if (id == null || id.isBlank()) {
            return "Unknown";
        }
        String cleaned = id.replace('-', ' ').replace('_', ' ').trim();
        if (cleaned.isEmpty()) {
            return "Unknown";
        }
        String lower = cleaned.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        boolean upper = true;
        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            if (ch == ' ') {
                sb.append(' ');
                upper = true;
                continue;
            }
            sb.append(upper ? Character.toUpperCase(ch) : ch);
            upper = false;
        }
        return sb.toString();
    }
}
