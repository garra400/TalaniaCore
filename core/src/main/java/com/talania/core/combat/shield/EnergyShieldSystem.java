package com.talania.core.combat.shield;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;
import com.talania.core.ui.hud.EnergyShieldHud;
import com.talania.core.utils.PlayerRefUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Regenerates energy shield for players when out of combat.
 */
public final class EnergyShieldSystem extends EntityTickingSystem<EntityStore> {
    private static final Query<EntityStore> QUERY = Query.and(Archetype.of(
            UUIDComponent.getComponentType(),
            Player.getComponentType()
    ));
    private final Map<UUID, HudState> hudStates = new HashMap<>();

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(float delta, int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                     CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return;
        }
        UUID uuid = uuidComponent.getUuid();
        if (uuid == null) {
            return;
        }
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        float maxShield = StatsManager.getStat(uuid, StatType.ENERGY_SHIELD_MAX);
        float rechargeRate = StatsManager.getStat(uuid, StatType.ENERGY_SHIELD_RECHARGE);
        float rechargeDelay = StatsManager.getStat(uuid, StatType.ENERGY_SHIELD_RECHARGE_DELAY);
        EnergyShieldService.tick(uuid, delta, maxShield, rechargeRate, rechargeDelay);
        updateHud(player, ref, store, uuid, maxShield);
    }

    private void updateHud(Player player, Ref<EntityStore> ref, Store<EntityStore> store,
                           UUID uuid, float maxShield) {
        if (player == null || uuid == null) {
            return;
        }
        float safeMax = Math.max(0.0f, maxShield);
        float current = EnergyShieldService.getCurrent(uuid);
        HudState state = hudStates.computeIfAbsent(uuid, id -> new HudState());
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef = state.playerRef;
        if (playerRef == null) {
            playerRef = PlayerRefUtil.resolve(ref, store);
            if (playerRef == null) {
                return;
            }
            state.playerRef = playerRef;
        }
        if (safeMax <= 0.0f) {
            if (state.hud != null) {
                state.lastMax = 0.0f;
                state.lastCurrent = 0.0f;
                state.hud.updateValues(0.0f, 0.0f);
            }
            return;
        }
        if (state.hud == null) {
            state.hud = new EnergyShieldHud(playerRef);
            player.getHudManager().setCustomHud(playerRef, state.hud);
        }
        boolean shouldUpdate = Math.abs(state.lastMax - safeMax) > 0.001f
                || Math.abs(state.lastCurrent - current) > 0.001f;
        if (shouldUpdate) {
            state.lastMax = safeMax;
            state.lastCurrent = current;
            state.hud.updateValues(current, safeMax);
        }
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        hudStates.remove(playerId);
    }

    private static final class HudState {
        private EnergyShieldHud hud;
        private com.hypixel.hytale.server.core.universe.PlayerRef playerRef;
        private float lastCurrent = -1.0f;
        private float lastMax = -1.0f;
    }
}
