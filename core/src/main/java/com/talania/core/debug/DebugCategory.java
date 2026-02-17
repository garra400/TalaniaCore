package com.talania.core.debug;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Debug log categories used by Talania debug tools.
 */
public enum DebugCategory {
    DAMAGE("damage", "Damage events and totals"),
    MODIFIERS("modifiers", "Damage modifiers and formulas"),
    COOLDOWN("cooldown", "Cooldown triggers and blocks"),
    INPUT("input", "Raw input and pattern detection"),
    ACTIVATION("activation", "Ability activations"),
    PROFILE("profile", "Profile and progression changes"),
    SYSTEM("system", "Runtime warnings and system state"),
    UI("ui", "UI actions and flows"),
    PROJECTILES("projectiles", "Projectile ownership and impacts"),
    EFFECTS("effects", "Entity effect application/removal"),
    COMBAT_LOG("combat_log", "Combat log storage and display");

    private final String id;
    private final String description;

    DebugCategory(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    @Nullable
    public static DebugCategory fromId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (DebugCategory category : values()) {
            if (category.id.equals(normalized)) {
                return category;
            }
        }
        return null;
    }
}
