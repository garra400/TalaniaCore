package com.talania.core.combat;

import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;

/**
 * Meta keys used by Talania combat systems.
 */
public final class CombatMetaKeys {
    private CombatMetaKeys() {}

    public static final MetaKey TALANIA_APPLIED = Damage.META_REGISTRY.registerMetaObject();
    public static final MetaKey CRIT_HIT = Damage.META_REGISTRY.registerMetaObject();
}
