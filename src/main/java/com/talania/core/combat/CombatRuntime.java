package com.talania.core.combat;

import java.util.UUID;

/**
 * Runtime settings and providers for combat systems.
 */
public final class CombatRuntime {
    private static volatile CombatRuleProvider ruleProvider = id -> new CombatRules();
    private static volatile boolean pvpEnabled = true;

    private CombatRuntime() {}

    public static void setRuleProvider(CombatRuleProvider provider) {
        if (provider != null) {
            ruleProvider = provider;
        }
    }

    public static CombatRules rulesFor(UUID entityId) {
        CombatRules rules = ruleProvider.rulesFor(entityId);
        return rules != null ? rules : new CombatRules();
    }

    public static boolean pvpEnabled() {
        return pvpEnabled;
    }

    public static void setPvpEnabled(boolean enabled) {
        pvpEnabled = enabled;
    }
}
