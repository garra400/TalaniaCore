package com.talania.core.combat.damage;

import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;

/**
 * Meta keys used by Talania combat systems.
 *
 * <p>These are attached to {@code Damage} instances to avoid double-processing
 * and to mark critical hits / attack typing for downstream systems.</p>
 */
public final class DamageMetaKeys {
    private DamageMetaKeys() {}

    public static final MetaKey TALANIA_APPLIED = Damage.META_REGISTRY.registerMetaObject();
    public static final MetaKey CRIT_HIT = Damage.META_REGISTRY.registerMetaObject();
    public static final MetaKey ATTACK_TYPE = Damage.META_REGISTRY.registerMetaObject();
    public static final MetaKey DAMAGE_TYPE = Damage.META_REGISTRY.registerMetaObject();
}
