package com.talania.core.debug.dev;

import com.talania.core.config.ConfigManager;
import com.talania.core.debug.DebugCategory;
import com.talania.core.debug.DebugLogService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dev-only persistence for per-player debug log settings.
 */
public final class DebugLogSettingsStore {
    private static final String FILE = "debug_log_settings_dev.json";
    private static DebugLogSettingsStore INSTANCE;

    private Map<String, List<String>> enabledByPlayer = new HashMap<>();

    public static DebugLogSettingsStore load() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        if (!ConfigManager.isInitialized()) {
            INSTANCE = new DebugLogSettingsStore();
            return INSTANCE;
        }
        DebugLogSettingsStore loaded = ConfigManager.load(FILE, DebugLogSettingsStore.class);
        INSTANCE = loaded != null ? loaded : new DebugLogSettingsStore();
        if (INSTANCE.enabledByPlayer == null) {
            INSTANCE.enabledByPlayer = new HashMap<>();
        }
        return INSTANCE;
    }

    public EnumSet<DebugCategory> getEnabled(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        List<String> ids = enabledByPlayer.get(playerId.toString());
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        EnumSet<DebugCategory> result = EnumSet.noneOf(DebugCategory.class);
        for (String id : ids) {
            DebugCategory category = DebugCategory.fromId(id);
            if (category != null) {
                result.add(category);
            }
        }
        return result;
    }

    public void setEnabled(UUID playerId, EnumSet<DebugCategory> categories) {
        if (playerId == null) {
            return;
        }
        List<String> ids = new ArrayList<>();
        if (categories != null) {
            for (DebugCategory category : categories) {
                ids.add(category.id());
            }
        }
        enabledByPlayer.put(playerId.toString(), ids);
        save();
    }

    public void applyTo(DebugLogService logService, UUID playerId) {
        if (logService == null || playerId == null) {
            return;
        }
        EnumSet<DebugCategory> stored = getEnabled(playerId);
        if (stored == null) {
            return;
        }
        logService.setAll(playerId, stored);
    }

    private void save() {
        if (!ConfigManager.isInitialized()) {
            return;
        }
        ConfigManager.save(FILE, this);
    }
}
