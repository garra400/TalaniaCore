package com.talania.core.combat;

import com.talania.core.combat.damage.WeaponCategoryDamageService;

public final class CombatManager {
    private static final CombatSettings SETTINGS = new CombatSettings();
    private static volatile WeaponCategoryDamageService weaponCategoryDamageService;

    private CombatManager() {}

    public static void setWeaponCategoryDamageService(WeaponCategoryDamageService service) {
        weaponCategoryDamageService = service;
    }

    public static WeaponCategoryDamageService weaponCategoryDamageService() {
        return weaponCategoryDamageService;
    }

    /**
     * Global combat settings (server-wide).
     */
    public static CombatSettings settings() {
        return SETTINGS;
    }
}
