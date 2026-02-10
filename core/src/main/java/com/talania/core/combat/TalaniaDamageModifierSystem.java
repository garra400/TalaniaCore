package com.talania.core.combat;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.StatsManager;
import com.talania.core.stats.DamageType;
import com.talania.core.stats.StatType;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ECS system that applies Talania stats and combat rules to damage events.
 *
 * <p>How it works:</p>
 * <ul>
 *   <li>Runs in the damage filter group and inspects each damage instance.</li>
 *   <li>Uses Talania {@link com.talania.core.stats.StatType} for dodge, crit, lifesteal,
 *       fall resistance, attack-type scaling, and blocking efficiency.</li>
 *   <li>Optionally applies {@link DamageType} resistance if a damage type is attached.</li>
 *   <li>Applies global combat settings (PVP scaling) and per-player stats
 *       (sprint damage, flat reduction, stamina drain).</li>
 *   <li>Optionally applies weapon-category modifiers if a service is installed.</li>
 * </ul>
 *
 * <p>How to use it:</p>
 * <ul>
 *   <li>Register the system with the entity store registry.</li>
 *   <li>Configure global settings via {@link CombatManager#settings()} and optional services.</li>
 * </ul>
 *
 * <p>Integration with Hytale:</p>
 * <ul>
 *   <li>Uses {@code DamageModule} filter group and {@code Damage} meta keys.</li>
 *   <li>Reads {@code EntityStatMap} for lifesteal heals.</li>
 * </ul>
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
        if (attackerIsPlayer && targetIsPlayer && !CombatManager.settings().pvpEnabled()) {
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

        // Attack-type multipliers (optional meta)
        AttackType attackType = (AttackType) damage.getIfPresentMetaObject(CombatMetaKeys.ATTACK_TYPE);
        if (attackType != null) {
            if (attackerUuid != null) {
                float outgoing = StatsManager.getStat(attackerUuid, attackType.damageStat());
                if (outgoing != 1.0F) {
                    damage.setAmount(damage.getAmount() * outgoing);
                }
            }
            if (targetUuid != null) {
                float incoming = StatsManager.getStat(targetUuid, attackType.damageTakenStat());
                if (incoming != 1.0F) {
                    damage.setAmount(damage.getAmount() * incoming);
                }
            }
        }

        // Sprint damage multiplier (per-player stat)
        if (attackerRef != null) {
            com.hypixel.hytale.protocol.MovementStates movementStates = movementStates(store, attackerRef);
            if (movementStates != null && movementStates.sprinting) {
                float sprintMultiplier = StatsManager.getStat(attackerUuid, StatType.SPRINT_DAMAGE_MULT);
                if (sprintMultiplier > 1.0F) {
                    damage.setAmount(damage.getAmount() * sprintMultiplier);
                }
            }
        }

        // Player damage multipliers (global settings)
        if (attackerIsPlayer && attackerUuid != null) {
            float multiplier = targetIsPlayer
                    ? CombatManager.settings().playerDamageToPlayerMultiplier()
                    : CombatManager.settings().playerDamageMultiplier();
            if (multiplier != 1.0F) {
                damage.setAmount(damage.getAmount() * multiplier);
            }
        }

        // Weapon category damage modifiers (optional service)
        if (attackerUuid != null) {
            WeaponCategoryDamageService service = CombatManager.weaponCategoryDamageService();
            if (service != null && attackerRef != null) {
                String category = resolveWeaponCategory(store, attackerRef);
                WeaponCategoryDamage weaponDamage = service.get(attackerUuid, category);
                if (weaponDamage != null) {
                    if (weaponDamage.bonus != 0.0f) {
                        damage.setAmount(damage.getAmount() * Math.max(0.0f, 1.0f + weaponDamage.bonus));
                    }
                    if (weaponDamage.multiplier != 1.0f) {
                        damage.setAmount(damage.getAmount() * weaponDamage.multiplier);
                    }
                }
            }
        }

        // Flat damage reduction (per-player stat) + armor stat (percent)
        if (targetUuid != null) {
            float armorReduction = StatsManager.getStat(targetUuid, StatType.ARMOR);
            if (armorReduction > 0.0F) {
                float clamped = Math.max(0.0F, Math.min(1.0F, armorReduction));
                damage.setAmount(damage.getAmount() * (1.0F - clamped));
            }
            float flatReduction = StatsManager.getStat(targetUuid, StatType.FLAT_DAMAGE_REDUCTION);
            if (flatReduction > 0.0F) {
                float reduced = Math.max(0.0F, damage.getAmount() - flatReduction);
                damage.setAmount(reduced);
                if (reduced <= 0.0F) {
                    damage.setCancelled(true);
                    return;
                }
            }
        }

        // Damage-type resistances (optional meta)
        DamageType damageType = (DamageType) damage.getIfPresentMetaObject(CombatMetaKeys.DAMAGE_TYPE);
        if (damageType != null && damageType != DamageType.PHYSICAL) {
            float resist = StatsManager.getStat(targetUuid, damageType.resistanceStat());
            if (resist > 0.0F) {
                float clamped = Math.max(0.0F, Math.min(1.0F, resist));
                damage.setAmount(damage.getAmount() * (1.0F - clamped));
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

        // Blocking efficiency & stamina drain scaling (per-player stat)
        Float existing = (Float) damage.getIfPresentMetaObject(Damage.STAMINA_DRAIN_MULTIPLIER);
        if (existing == null) {
            float staminaMult = StatsManager.getStat(targetUuid, StatType.STAMINA_DRAIN_MULT);
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

    private static String resolveWeaponCategory(Store<EntityStore> store, Ref<EntityStore> attackerRef) {
        if (store == null || attackerRef == null) {
            return null;
        }
        ItemStack itemStack = itemInHand(store, attackerRef);
        if (itemStack == null || itemStack.isEmpty()) {
            return "Unarmed";
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return null;
        }
        String family = familyTag(item.getData());
        if (family != null) {
            return family;
        }
        return weaponFamilyFromItemId(item.getId());
    }

    private static ItemStack itemInHand(Store<EntityStore> store, Ref<EntityStore> attackerRef) {
        Player player = (Player) store.getComponent(attackerRef, Player.getComponentType());
        if (player != null) {
            Inventory inventory = player.getInventory();
            return inventory == null ? null : inventory.getItemInHand();
        }
        return null;
    }

    private static String familyTag(AssetExtraInfo.Data data) {
        if (data == null) {
            return null;
        }
        java.util.Map<String, String[]> tags = data.getRawTags();
        if (tags == null) {
            return null;
        }
        String[] family = tags.get("Family");
        if (family != null && family.length > 0) {
            return family[0];
        }
        return null;
    }

    private static String weaponFamilyFromItemId(String itemId) {
        if (itemId == null || !itemId.startsWith("Weapon_")) {
            return null;
        }
        int start = "Weapon_".length();
        int end = itemId.indexOf('_', start);
        if (end == -1) {
            return itemId.substring(start);
        }
        return itemId.substring(start, end);
    }
}
