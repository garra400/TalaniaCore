package com.talania.core.combat;

import java.util.UUID;

/**
 * Provides per-entity combat rules.
 */
@FunctionalInterface
public interface CombatRuleProvider {
    CombatRules rulesFor(UUID entityId);
}
