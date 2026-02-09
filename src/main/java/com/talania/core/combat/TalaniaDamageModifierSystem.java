package com.talania.core.combat;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.StatsManager;
import com.talania.core.stats.StatType;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies Talania stats and combat rules to the Hytale damage pipeline.
 *
 * <p>This is a generic backbone; it does not implement any specific abilities.</p>
 */
public final class TalaniaDamageModifierSystem extends DamageEventSystem {
    private static final Query<EntityStore> QUERY = Query.and(
            Archetype.of(EntityStatMap.getComponentType()),
            Archetype.of(UUIDComponent.getComponentType())
    );

    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                       CommandBuffer<EntityStore> commandBuffer, Damage damage) {
        if (damage == null) {
            return;
        }
        if (damage.getAmount() <= 0.0F) {
            return;
        }
        if (damage.hasMetaObject(CombatMetaKeys.TALANIA_APPLIED)) {
            return;
        }
        damage.putMetaObject(CombatMetaKeys.TALANIA_APPLIED, Boolean.TRUE);

        Ref<EntityStore> targetRef = archetypeChunk.getReferenceTo(index);
        UUID targetUuid = uuidFor(targetRef, commandBuffer);
        if (targetUuid == null) {
            return;
        }

        Ref<EntityStore> attackerRef = attackerRefFromDamage(damage);
        UUID attackerUuid = attackerRef != null ? uuidFor(attackerRef, commandBuffer) : null;
        boolean targetIsPlayer = isPlayer(store, targetRef);
        boolean attackerIsPlayer = isPlayer(store, attackerRef);
        if (attackerIsPlayer && targetIsPlayer && !CombatRuntime.pvpEnabled()) {
            return;
        }

        // Target dodge chance
        float dodgeChance = StatsManager.getStat(targetUuid, StatType.DODGE_CHANCE);
        if (attackerRef != null && dodgeChance > 0.0F) {
            if (ThreadLocalRandom.current().nextFloat() < dodgeChance) {
                damage.setCancelled(true);
                damage.setAmount(0.0F);
                return;
            }
        }

        // Crit from attacker stats
        if (attackerUuid != null) {
            float critChance = StatsManager.getStat(attackerUuid, StatType.CRIT_CHANCE);
            if (critChance > 0.0F && ThreadLocalRandom.current().nextFloat() < critChance) {
                float critMultiplier = StatsManager.getStat(attackerUuid, StatType.CRIT_DAMAGE);
                if (critMultiplier <= 0.0F) {
                    critMultiplier = 1.5F;
                }
                damage.setAmount(damage.getAmount() * critMultiplier);
                damage.putMetaObject(CombatMetaKeys.CRIT_HIT, Boolean.TRUE);
            }
        }

        // Sprint damage multiplier (rules)
        if (attackerRef != null) {
            com.hypixel.hytale.protocol.MovementStates movementStates = movementStates(store, attackerRef);
            if (movementStates != null && movementStates.sprinting) {
                CombatRules rules = CombatRuntime.rulesFor(attackerUuid);
                float sprintMultiplier = rules.sprintDamageMultiplier();
                if (sprintMultiplier > 1.0F) {
                    damage.setAmount(damage.getAmount() * sprintMultiplier);
                }
            }
        }

        // Player damage multipliers
        if (attackerIsPlayer && attackerUuid != null) {
            CombatRules rules = CombatRuntime.rulesFor(attackerUuid);
            float multiplier = targetIsPlayer
                    ? rules.playerDamageToPlayerMultiplier()
                    : rules.playerDamageMultiplier();
            if (multiplier != 1.0F) {
                damage.setAmount(damage.getAmount() * multiplier);
            }
        }

        // Flat damage reduction (rules) + armor stat (percent)
        if (targetUuid != null) {
            float armorReduction = StatsManager.getStat(targetUuid, StatType.ARMOR);
            if (armorReduction > 0.0F) {
                float clamped = Math.max(0.0F, Math.min(1.0F, armorReduction));
                damage.setAmount(damage.getAmount() * (1.0F - clamped));
            }
            CombatRules rules = CombatRuntime.rulesFor(targetUuid);
            float flatReduction = rules.flatDamageReduction();
            if (flatReduction > 0.0F) {
                float reduced = Math.max(0.0F, damage.getAmount() - flatReduction);
                damage.setAmount(reduced);
                if (reduced <= 0.0F) {
                    damage.setCancelled(true);
                    return;
                }
            }
        }

        // Fall damage reduction
        if (damage.getCause() == DamageCause.FALL) {
            float fallResist = StatsManager.getStat(targetUuid, StatType.FALL_RESISTANCE);
            if (fallResist > 0.0F) {
                float clamped = Math.max(0.0F, Math.min(1.0F, fallResist));
                damage.setAmount(damage.getAmount() * (1.0F - clamped));
            }
        }

        // Blocking efficiency & stamina drain scaling
        Float existing = (Float) damage.getIfPresentMetaObject(Damage.STAMINA_DRAIN_MULTIPLIER);
        if (existing == null) {
            CombatRules rules = CombatRuntime.rulesFor(targetUuid);
            float staminaMult = rules.staminaDrainMultiplier();
            Boolean blocked = damage.getIfPresentMetaObject(Damage.BLOCKED);
            if (blocked != null && blocked.booleanValue()) {
                float blockingEfficiency = StatsManager.getStat(targetUuid, StatType.BLOCKING_EFFICIENCY);
                if (blockingEfficiency > 0.0F) {
                    staminaMult *= (1.0F / blockingEfficiency);
                }
            }
            if (staminaMult != 1.0F) {
                damage.putMetaObject(Damage.STAMINA_DRAIN_MULTIPLIER, staminaMult);
            }
        }

        // Lifesteal
        if (attackerUuid != null) {
            float lifesteal = StatsManager.getStat(attackerUuid, StatType.LIFESTEAL);
            if (lifesteal > 0.0F) {
                float heal = damage.getAmount() * lifesteal;
                if (heal > 0.0F) {
                    EntityStatMap attackerStats =
                            (EntityStatMap) store.getComponent(attackerRef, EntityStatMap.getComponentType());
                    if (attackerStats != null) {
                        attackerStats.addStatValue(DefaultEntityStatTypes.getHealth(), heal);
                    }
                }
            }
        }
    }

    private static boolean isPlayer(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (store == null || ref == null || !ref.isValid()) {
            return false;
        }
        return store.getComponent(ref, Player.getComponentType()) != null;
    }

    private static Ref<EntityStore> attackerRefFromDamage(Damage damage) {
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource) {
            return ((Damage.EntitySource) source).getRef();
        }
        return null;
    }

    private static UUID uuidFor(Ref<EntityStore> ref, ComponentAccessor accessor) {
        UUIDComponent uuidComponent = (UUIDComponent) accessor.getComponent(ref, UUIDComponent.getComponentType());
        return uuidComponent == null ? null : uuidComponent.getUuid();
    }

    private static com.hypixel.hytale.protocol.MovementStates movementStates(Store<EntityStore> store, Ref<EntityStore> ref) {
        MovementStatesComponent movementStatesComponent =
                (MovementStatesComponent) store.getComponent(ref, MovementStatesComponent.getComponentType());
        return movementStatesComponent == null ? null : movementStatesComponent.getMovementStates();
    }
}
