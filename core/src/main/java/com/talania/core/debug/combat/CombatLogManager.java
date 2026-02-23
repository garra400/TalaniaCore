package com.talania.core.debug.combat;

import com.talania.core.debug.DebugCategory;
import com.talania.core.debug.DebugLogService;
import com.talania.core.debug.events.CombatLogEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores per-player combat log entries in memory.
 */
public final class CombatLogManager {
    private final DebugLogService logService;
    private final Map<UUID, CombatLogBuffer> buffers = new ConcurrentHashMap<>();

    public CombatLogManager(DebugLogService logService) {
        this.logService = logService;
    }

    public void handleEvent(CombatLogEvent event) {
        if (event == null || event.entry() == null) {
            return;
        }
        CombatLogEntry entry = event.entry();
        UUID attackerId = entry.attackerId();
        UUID targetId = entry.targetId();

        logService.logToConsole(DebugCategory.DAMAGE,
                CombatLogFormatter.summaryFor(null, entry, entry.attackerName(), entry.targetName()));

        if (attackerId != null) {
            addIfEnabled(attackerId, entry);
            emitChatIfEnabled(attackerId, entry);
        }
        if (targetId != null && !targetId.equals(attackerId)) {
            addIfEnabled(targetId, entry);
            emitChatIfEnabled(targetId, entry);
        }
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        buffers.remove(playerId);
    }

    public List<CombatLogEntry> recent(UUID playerId, int limit) {
        if (playerId == null || limit <= 0) {
            return List.of();
        }
        CombatLogBuffer buffer = buffers.get(playerId);
        if (buffer == null) {
            return List.of();
        }
        return buffer.snapshot(limit);
    }

    private void addIfEnabled(UUID playerId, CombatLogEntry entry) {
        CombatLogBuffer buffer = buffers.computeIfAbsent(playerId,
                id -> new CombatLogBuffer(logService.settings().combatLogMaxEntries));
        buffer.add(entry);
    }

    private void emitChatIfEnabled(UUID playerId, CombatLogEntry entry) {
        if (logService.isEnabled(playerId, DebugCategory.DAMAGE)) {
            logService.log(playerId, DebugCategory.DAMAGE,
                    CombatLogFormatter.summaryFor(playerId, entry, entry.attackerName(), entry.targetName()));
        }
        if (logService.isEnabled(playerId, DebugCategory.MODIFIERS)) {
            for (String line : CombatLogFormatter.modifierLines(entry)) {
                logService.log(playerId, DebugCategory.MODIFIERS, line);
            }
        }
    }


    private static final class CombatLogBuffer {
        private final int max;
        private final Deque<CombatLogEntry> entries = new ArrayDeque<>();
        private UUID lastEventId;

        private CombatLogBuffer(int max) {
            this.max = Math.max(1, max);
        }

        void add(CombatLogEntry entry) {
            if (entry == null) {
                return;
            }
            synchronized (entries) {
                if (lastEventId != null && lastEventId.equals(entry.eventId())) {
                    return;
                }
                lastEventId = entry.eventId();
                if (entries.size() >= max) {
                    entries.removeFirst();
                }
                entries.addLast(entry);
            }
        }

        List<CombatLogEntry> snapshot(int limit) {
            List<CombatLogEntry> result = new ArrayList<>();
            synchronized (entries) {
                int count = Math.min(limit, entries.size());
                int skip = Math.max(0, entries.size() - count);
                int index = 0;
                for (CombatLogEntry entry : entries) {
                    if (index++ < skip) {
                        continue;
                    }
                    result.add(entry);
                }
            }
            return result;
        }
    }
}
