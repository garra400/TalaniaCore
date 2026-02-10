package com.talania.core.abilities;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-entity cooldowns and optionally displays a status-effect icon.
 *
 * <p>How it works:</p>
 * <ul>
 *   <li>Cooldowns are stored per (ownerId, abilityId) with a ready-at timestamp.</li>
 *   <li>{@link #tryActivate} checks readiness, then records the cooldown.</li>
 *   <li>If a {@link CooldownEffect} is provided, the service applies the
 *       corresponding {@code EntityEffect} via {@code EffectControllerComponent}.</li>
 * </ul>
 *
 * <p>How to use it:</p>
 * <ul>
 *   <li>Call {@link #tryActivate} when an ability is triggered.</li>
 *   <li>Use {@link #remainingMs} or {@link #isReady} for UI or gating.</li>
 *   <li>Call {@link #clear} when an entity is removed.</li>
 * </ul>
 *
 * <p>Integration with Hytale:</p>
 * <ul>
 *   <li>Uses {@code EffectControllerComponent} to apply a status icon.</li>
 *   <li>Resolves effect IDs via {@code EntityEffect.getAssetMap()}.</li>
 * </ul>
 */
public final class AbilityCooldownService {
    public static final String DEFAULT_COOLDOWN_EFFECT_ID = "Potion_Stamina_Cooldown";
    public static final OverlapBehavior DEFAULT_COOLDOWN_OVERLAP = OverlapBehavior.OVERWRITE;

    private final CooldownEffect defaultCooldownEffect;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public AbilityCooldownService() {
        this(new CooldownEffect(DEFAULT_COOLDOWN_EFFECT_ID, DEFAULT_COOLDOWN_OVERLAP));
    }

    public AbilityCooldownService(CooldownEffect defaultCooldownEffect) {
        this.defaultCooldownEffect = defaultCooldownEffect;
    }

    public boolean isReady(UUID ownerId, String abilityId) {
        return remainingMs(ownerId, abilityId, System.currentTimeMillis()) <= 0L;
    }

    public long remainingMs(UUID ownerId, String abilityId, long now) {
        Map<String, Long> ownerCooldowns = cooldowns.get(ownerId);
        if (ownerCooldowns == null) {
            return 0L;
        }
        Long readyAt = ownerCooldowns.get(abilityId);
        if (readyAt == null) {
            return 0L;
        }
        return Math.max(0L, readyAt - now);
    }

    public boolean tryActivate(UUID ownerId, String abilityId, long cooldownMs,
                               Ref<EntityStore> ownerRef, Store<EntityStore> store) {
        return tryActivate(ownerId, abilityId, cooldownMs, ownerRef, store, defaultCooldownEffect);
    }

    public boolean tryActivate(UUID ownerId, String abilityId, long cooldownMs,
                               Ref<EntityStore> ownerRef, Store<EntityStore> store,
                               @Nullable CooldownEffect cooldownEffect) {
        long now = System.currentTimeMillis();
        if (remainingMs(ownerId, abilityId, now) > 0L) {
            return false;
        }
        trigger(ownerId, abilityId, cooldownMs, ownerRef, store, cooldownEffect);
        return true;
    }

    public void trigger(UUID ownerId, String abilityId, long cooldownMs,
                        Ref<EntityStore> ownerRef, Store<EntityStore> store) {
        trigger(ownerId, abilityId, cooldownMs, ownerRef, store, defaultCooldownEffect);
    }

    public void trigger(UUID ownerId, String abilityId, long cooldownMs,
                        Ref<EntityStore> ownerRef, Store<EntityStore> store,
                        @Nullable CooldownEffect cooldownEffect) {
        if (cooldownMs <= 0L) {
            return;
        }
        long readyAt = System.currentTimeMillis() + cooldownMs;
        cooldowns.computeIfAbsent(ownerId, ignored -> new ConcurrentHashMap<>()).put(abilityId, readyAt);
        if (cooldownEffect != null) {
            applyCooldownEffect(ownerRef, store, cooldownMs, cooldownEffect);
        }
    }

    public void applyEffect(Ref<EntityStore> ownerRef, Store<EntityStore> store,
                            long durationMs, @Nullable CooldownEffect cooldownEffect) {
        if (cooldownEffect == null || durationMs <= 0L) {
            return;
        }
        applyCooldownEffect(ownerRef, store, durationMs, cooldownEffect);
    }

    public void clear(UUID ownerId) {
        cooldowns.remove(ownerId);
    }

    private void applyCooldownEffect(Ref<EntityStore> ownerRef, Store<EntityStore> store,
                                     long cooldownMs, CooldownEffect cooldownEffect) {
        EffectControllerComponent effectController =
                (EffectControllerComponent) store.getComponent(ownerRef, EffectControllerComponent.getComponentType());
        if (effectController == null) {
            return;
        }
        int effectIndex = resolveEffectIndex(cooldownEffect.effectId());
        if (effectIndex == Integer.MIN_VALUE) {
            return;
        }
        EntityEffect effect = (EntityEffect) EntityEffect.getAssetMap().getAsset(effectIndex);
        if (effect == null) {
            return;
        }
        float durationSeconds = Math.max(0.1F, cooldownMs / 1000.0F);
        effectController.addEffect(ownerRef, effect, durationSeconds, cooldownEffect.overlapBehavior(), store);
    }

    private int resolveEffectIndex(String effectId) {
        int effectIndex = EntityEffect.getAssetMap().getIndex(effectId);
        if (effectIndex != Integer.MIN_VALUE) {
            return effectIndex;
        }
        if (effectId == null || effectId.isBlank()) {
            return effectIndex;
        }
        return EntityEffect.getAssetMap().getIndex("EntityEffect/" + effectId);
    }
}
