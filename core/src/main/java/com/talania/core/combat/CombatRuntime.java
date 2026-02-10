package com.talania.core.combat;

import java.util.UUID;

public final class CombatRuntime {
    private static final CombatSettings SETTINGS = new CombatSettings();
    private static volatile WeaponCategoryDamageService weaponCategoryDamageService;

    private CombatRuntime() {}

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
