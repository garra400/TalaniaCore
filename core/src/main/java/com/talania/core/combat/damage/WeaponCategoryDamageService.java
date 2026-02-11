package com.talania.core.combat.damage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores per-entity weapon category damage modifiers.
 */
public final class WeaponCategoryDamageService {
    private final Map<UUID, Map<String, WeaponCategoryDamage>> values = new ConcurrentHashMap<>();

    public WeaponCategoryDamage forCategory(UUID entityId, String category) {
        if (entityId == null || category == null || category.isBlank()) {
            return null;
        }
        String key = normalize(category);
        return values
                .computeIfAbsent(entityId, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(key, ignored -> new WeaponCategoryDamage());
    }

    public WeaponCategoryDamage get(UUID entityId, String category) {
        if (entityId == null || category == null || category.isBlank()) {
            return null;
        }
        Map<String, WeaponCategoryDamage> map = values.get(entityId);
        if (map == null) {
            return null;
        }
        return map.get(normalize(category));
    }

    public void clear(UUID entityId) {
        if (entityId != null) {
            values.remove(entityId);
        }
    }

    private static String normalize(String category) {
        return category.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
