package com.talania.core.debug;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player debug log settings and chat output.
 */
public final class DebugLogService {
    private final Map<UUID, EnumSet<DebugCategory>> enabled = new ConcurrentHashMap<>();
    private final Map<String, Long> lastLogTimes = new ConcurrentHashMap<>();
    private volatile DebugSettings settings = new DebugSettings();

    public void setSettings(DebugSettings settings) {
        this.settings = settings != null ? settings : new DebugSettings();
    }

    public DebugSettings settings() {
        return settings;
    }

    public void ensurePlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }
        enabled.computeIfAbsent(playerId, id -> defaultSet());
    }

    public void clearPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }
        enabled.remove(playerId);
        lastLogTimes.keySet().removeIf(key -> key.startsWith(playerId.toString() + ":"));
    }

    public boolean isEnabled(UUID playerId, DebugCategory category) {
        if (playerId == null || category == null) {
            return false;
        }
        EnumSet<DebugCategory> set = enabled.get(playerId);
        return set != null && set.contains(category);
    }

    public boolean toggle(UUID playerId, DebugCategory category) {
        if (playerId == null || category == null) {
            return false;
        }
        EnumSet<DebugCategory> next = enabled.computeIfAbsent(playerId, id -> defaultSet());
        if (next.contains(category)) {
            next.remove(category);
        } else {
            next.add(category);
        }
        return next.contains(category);
    }

    public void setEnabled(UUID playerId, DebugCategory category, boolean value) {
        if (playerId == null || category == null) {
            return;
        }
        EnumSet<DebugCategory> next = enabled.computeIfAbsent(playerId, id -> defaultSet());
        if (value) {
            next.add(category);
        } else {
            next.remove(category);
        }
    }

    private EnumSet<DebugCategory> defaultSet() {
        if (settings.defaultEnabledCategories == null || settings.defaultEnabledCategories.isEmpty()) {
            return EnumSet.noneOf(DebugCategory.class);
        }
        return EnumSet.copyOf(settings.defaultEnabledCategories);
    }

    public void log(UUID playerId, DebugCategory category, String message) {
        if (!settings.enableChatOutput || playerId == null || category == null || message == null) {
            return;
        }
        if (!isEnabled(playerId, category)) {
            return;
        }
        if (!shouldLog(playerId, category)) {
            return;
        }
        Player player = resolvePlayer(playerId);
        if (player == null) {
            return;
        }
        player.sendMessage(Message.raw("[talania][" + category.id() + "] " + message));
    }

    public void logToConsole(DebugCategory category, String message) {
        if (!settings.logToConsole || category == null || message == null) {
            return;
        }
        System.out.println("[TalaniaDebug][" + category.id() + "] " + message);
    }

    private boolean shouldLog(UUID playerId, DebugCategory category) {
        int rateLimitMs = settings.rateLimitMs;
        if (rateLimitMs <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        String key = playerId + ":" + category.id();
        Long last = lastLogTimes.get(key);
        if (last != null && now - last < rateLimitMs) {
            return false;
        }
        lastLogTimes.put(key, now);
        return true;
    }

    private Player resolvePlayer(UUID playerId) {
        PlayerRef playerRef = Universe.get().getPlayer(playerId);
        if (playerRef == null) {
            return null;
        }
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) {
            return null;
        }
        Store<EntityStore> store = ref.getStore();
        if (store == null) {
            return null;
        }
        return (Player) store.getComponent(ref, Player.getComponentType());
    }
}
