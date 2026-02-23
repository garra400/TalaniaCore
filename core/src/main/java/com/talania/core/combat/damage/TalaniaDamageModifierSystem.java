package com.talania.core.combat.damage;

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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.combat.CombatManager;
import com.talania.core.combat.healing.HealingService;
import com.talania.core.combat.shield.EnergyShieldService;
import com.talania.core.debug.combat.CombatLogEntry;
import com.talania.core.debug.events.CombatLogEvent;
import com.talania.core.events.EventBus;
import com.talania.core.stats.StatsManager;
import com.talania.core.stats.DamageType;
import com.talania.core.stats.StatType;

import java.text.DecimalFormat;
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
        if (damage.hasMetaObject(DamageMetaKeys.TALANIA_APPLIED)) {
            return;
        }
        damage.putMetaObject(DamageMetaKeys.TALANIA_APPLIED, Boolean.TRUE);

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

        // Ensure debug stat modifiers are applied before reading stats.
        if (attackerIsPlayer && attackerUuid != null) {
            com.talania.core.debug.TalaniaDebug.statModifiers()
                    .applyToStats(attackerUuid, StatsManager.getOrCreate(attackerUuid));
        }
        if (targetIsPlayer && targetUuid != null) {
            com.talania.core.debug.TalaniaDebug.statModifiers()
                    .applyToStats(targetUuid, StatsManager.getOrCreate(targetUuid));
        }

        float baseAmount = damage.getAmount();
        CombatLogEntry.Builder logBuilder =
                CombatLogEntry.builder(UUID.randomUUID(), attackerUuid, targetUuid, baseAmount)
                        .cause(damage.getCause());
        logBuilder.attackerName(resolveEntityName(store, attackerRef));
        logBuilder.targetName(resolveEntityName(store, targetRef));

        AttackType attackType = (AttackType) damage.getIfPresentMetaObject(DamageMetaKeys.ATTACK_TYPE);
        if (attackType == null) {
            attackType = inferAttackType(store, attackerRef);
            if (attackType != null) {
                damage.putMetaObject(DamageMetaKeys.ATTACK_TYPE, attackType);
            }
        }
        DamageType damageType = (DamageType) damage.getIfPresentMetaObject(DamageMetaKeys.DAMAGE_TYPE);
        logBuilder.attackType(attackType).damageType(damageType);

        // Target dodge chance
        float dodgeChance = statWithDebug(targetUuid, StatType.DODGE_CHANCE);
        if (attackerRef != null && dodgeChance > 0.0F) {
            if (ThreadLocalRandom.current().nextFloat() < dodgeChance) {
                damage.setCancelled(true);
                damage.setAmount(0.0F);
                logBuilder.cancelled("dodge").finalAmount(0.0F);
                publishCombatLog(logBuilder);
                return;
            }
        }

        // Crit from attacker stats
        if (attackerUuid != null) {
            float critChance = statWithDebug(attackerUuid, StatType.CRIT_CHANCE);
            if (critChance > 0.0F && ThreadLocalRandom.current().nextFloat() < critChance) {
                float critMultiplier = statWithDebug(attackerUuid, StatType.CRIT_DAMAGE);
                if (critMultiplier <= 0.0F) {
                    critMultiplier = 1.5F;
                }
                float before = damage.getAmount();
                damage.setAmount(damage.getAmount() * critMultiplier);
                damage.putMetaObject(DamageMetaKeys.CRIT_HIT, Boolean.TRUE);
                logBuilder.crit(true)
                        .step("Critical Hit", before, damage.getAmount(),
                                "before * " + formatMultiplier(critMultiplier)
                                        + " (" + displayNameForStat(StatType.CRIT_DAMAGE) + ")");
            }
        }

        // Base attack power (physical or magic)
        if (attackerUuid != null && attackType != null) {
            StatType powerStat = attackType == AttackType.MAGIC ? StatType.MAGIC_ATTACK : StatType.ATTACK;
            float power = statWithDebug(attackerUuid, powerStat);
            if (power != 1.0F) {
                float before = damage.getAmount();
                damage.setAmount(damage.getAmount() * power);
                String label = displayNameForStat(powerStat);
                logBuilder.step(label, before, damage.getAmount(),
                        "before * " + formatMultiplier(power) + " (" + label + ")");
            }
        }

        // Attack-type multipliers (optional meta)
        if (attackType != null) {
            if (attackerUuid != null) {
                float outgoing = statWithDebug(attackerUuid, attackType.damageStat());
                if (outgoing != 1.0F) {
                    float before = damage.getAmount();
                    damage.setAmount(damage.getAmount() * outgoing);
                    String label = displayNameForStat(attackType.damageStat());
                    logBuilder.step(label, before, damage.getAmount(),
                            "before * " + formatMultiplier(outgoing) + " (" + label + ")");
                }
            }
            if (targetUuid != null) {
                float incoming = statWithDebug(targetUuid, attackType.damageTakenStat());
                if (incoming != 1.0F) {
                    float before = damage.getAmount();
                    damage.setAmount(damage.getAmount() * incoming);
                    String label = displayNameForStat(attackType.damageTakenStat());
                    logBuilder.step(label, before, damage.getAmount(),
                            "before * " + formatMultiplier(incoming) + " (" + label + ")");
                }
            }
        }

        // Sprint damage multiplier (per-player stat)
        if (attackerRef != null) {
            com.hypixel.hytale.protocol.MovementStates movementStates = movementStates(store, attackerRef);
            if (movementStates != null && movementStates.sprinting) {
                float sprintMultiplier = statWithDebug(attackerUuid, StatType.SPRINT_DAMAGE_MULT);
                if (sprintMultiplier > 1.0F) {
                    float before = damage.getAmount();
                    damage.setAmount(damage.getAmount() * sprintMultiplier);
                    String label = displayNameForStat(StatType.SPRINT_DAMAGE_MULT);
                    logBuilder.step(label, before, damage.getAmount(),
                            "before * " + formatMultiplier(sprintMultiplier) + " (" + label + ")");
                }
            }
        }

        // Player damage multipliers (global settings)
        if (attackerIsPlayer && attackerUuid != null) {
            float multiplier = targetIsPlayer
                    ? CombatManager.settings().playerDamageToPlayerMultiplier()
                    : CombatManager.settings().playerDamageMultiplier();
            if (multiplier != 1.0F) {
                float before = damage.getAmount();
                damage.setAmount(damage.getAmount() * multiplier);
                logBuilder.step("Player Damage", before, damage.getAmount(),
                        "before * " + formatMultiplier(multiplier) + " (Combat Settings)");
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
                        float before = damage.getAmount();
                        damage.setAmount(damage.getAmount() * Math.max(0.0f, 1.0f + weaponDamage.bonus));
                        logBuilder.step("Weapon Bonus", before, damage.getAmount(),
                                "before * (1 + " + formatMultiplier(weaponDamage.bonus) + ") (" + category + ")");
                    }
                    if (weaponDamage.multiplier != 1.0f) {
                        float before = damage.getAmount();
                        damage.setAmount(damage.getAmount() * weaponDamage.multiplier);
                        logBuilder.step("Weapon Multiplier", before, damage.getAmount(),
                                "before * " + formatMultiplier(weaponDamage.multiplier) + " (" + category + ")");
                    }
                }
            }
        }

        // Flat damage reduction (per-player stat) + armor stat (percent)
        if (targetUuid != null) {
            float armorReduction = statWithDebug(targetUuid, StatType.ARMOR);
            if (armorReduction > 0.0F) {
                float clamped = Math.max(0.0F, Math.min(1.0F, armorReduction));
                float before = damage.getAmount();
                damage.setAmount(damage.getAmount() * (1.0F - clamped));
                String label = displayNameForStat(StatType.ARMOR);
                logBuilder.step(label, before, damage.getAmount(),
                        "before * (1 - " + formatMultiplier(clamped) + ") (" + label + ")");
            }
            float flatReduction = statWithDebug(targetUuid, StatType.FLAT_DAMAGE_REDUCTION);
            if (flatReduction > 0.0F) {
                float before = damage.getAmount();
                float reduced = Math.max(0.0F, damage.getAmount() - flatReduction);
                damage.setAmount(reduced);
                String label = displayNameForStat(StatType.FLAT_DAMAGE_REDUCTION);
                logBuilder.step(label, before, reduced,
                        "before - " + formatAmount(flatReduction) + " (" + label + ")");
                if (reduced <= 0.0F) {
                    damage.setCancelled(true);
                    logBuilder.cancelled("flat_reduction").finalAmount(0.0F);
                    publishCombatLog(logBuilder);
                    return;
                }
            }
        }

        // Damage-type resistances (optional meta)
        if (damageType != null && damageType != DamageType.PHYSICAL) {
            float resist = statWithDebug(targetUuid, damageType.resistanceStat());
            if (resist > 0.0F) {
                float clamped = Math.max(0.0F, Math.min(1.0F, resist));
                float before = damage.getAmount();
                damage.setAmount(damage.getAmount() * (1.0F - clamped));
                String label = displayNameForStat(damageType.resistanceStat());
                logBuilder.step(label, before, damage.getAmount(),
                        "before * (1 - " + formatMultiplier(clamped) + ") (" + label + ")");
            }
        }

        // Fall damage reduction
        if (damage.getCause() == DamageCause.FALL) {
            float fallResist = statWithDebug(targetUuid, StatType.FALL_RESISTANCE);
            if (fallResist > 0.0F) {
                float clamped = Math.max(0.0F, Math.min(1.0F, fallResist));
                float before = damage.getAmount();
                damage.setAmount(damage.getAmount() * (1.0F - clamped));
                String label = displayNameForStat(StatType.FALL_RESISTANCE);
                logBuilder.step(label, before, damage.getAmount(),
                        "before * (1 - " + formatMultiplier(clamped) + ") (" + label + ")");
            }
        }

        float preShieldAmount = damage.getAmount();
        float shieldAbsorbed = 0.0f;
        float lifeDamage = preShieldAmount;
        // Energy shield absorption (applies before health damage).
        if (targetUuid != null) {
            float shieldMax = statWithDebug(targetUuid, StatType.ENERGY_SHIELD_MAX);
            if (shieldMax > 0.0F) {
                float remaining = EnergyShieldService.applyDamage(targetUuid, preShieldAmount, shieldMax);
                if (remaining != preShieldAmount) {
                    shieldAbsorbed = preShieldAmount - remaining;
                    lifeDamage = remaining;
                    damage.setAmount(remaining);
                }
            }
        }

        // Blocking efficiency & stamina drain scaling (per-player stat)
        Float existing = (Float) damage.getIfPresentMetaObject(Damage.STAMINA_DRAIN_MULTIPLIER);
        if (existing == null) {
            float staminaMult = statWithDebug(targetUuid, StatType.STAMINA_DRAIN_MULT);
            Boolean blocked = damage.getIfPresentMetaObject(Damage.BLOCKED);
            if (blocked != null && blocked.booleanValue()) {
                float blockingEfficiency = statWithDebug(targetUuid, StatType.BLOCKING_EFFICIENCY);
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
            float lifesteal = statWithDebug(attackerUuid, StatType.LIFESTEAL);
            if (lifesteal > 0.0F) {
                float heal = damage.getAmount() * lifesteal;
                if (heal > 0.0F) {
                    float applied = HealingService.applyHeal(attackerRef, store, heal);
                    if (applied > 0.0f) {
                        logBuilder.lifesteal(applied);
                    }
                }
            }
        }

        Boolean blocked = damage.getIfPresentMetaObject(Damage.BLOCKED);
        if (blocked != null) {
            logBuilder.blocked(blocked);
        }
        Float thornsDamage = (Float) damage.getIfPresentMetaObject(DamageMetaKeys.THORNS_DAMAGE);
        if (thornsDamage != null) {
            logBuilder.thorns(thornsDamage);
        }
        logBuilder.finalAmount(preShieldAmount);
        logBuilder.lifeDamage(lifeDamage);
        logBuilder.shieldAbsorbed(shieldAbsorbed);
        publishCombatLog(logBuilder);
    }

    private static final float DEBUG_EPS = 0.0001f;
    private static final DecimalFormat MULT_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("0.##");

    private static float statWithDebug(UUID entityId, StatType stat) {
        if (entityId == null || stat == null) {
            return stat != null ? stat.getDefaultValue() : 0.0f;
        }
        com.talania.core.debug.DebugStatModifierService debug = com.talania.core.debug.TalaniaDebug.statModifiers();
        float delta = debug.getDelta(entityId, stat);
        float mult = debug.getMultiplier(entityId, stat);
        if (Math.abs(delta) > DEBUG_EPS || Math.abs(mult - 1.0f) > DEBUG_EPS) {
            float base = debug.baseValue(entityId, stat);
            return (base + delta) * mult;
        }
        return StatsManager.getStat(entityId, stat);
    }

    private static String formatMultiplier(float value) {
        return MULT_FORMAT.format(value);
    }

    private static String formatAmount(float value) {
        return AMOUNT_FORMAT.format(value);
    }

    private static String displayNameForStat(StatType stat) {
        if (stat == null) {
            return "Unknown";
        }
        return switch (stat) {
            case ATTACK -> "Attack Power";
            case MAGIC_ATTACK -> "Magic Power";
            case MELEE_DAMAGE_MULT -> "Melee Damage";
            case RANGED_DAMAGE_MULT -> "Ranged Damage";
            case MAGIC_DAMAGE_MULT -> "Magic Damage";
            case SPRINT_DAMAGE_MULT -> "Sprint Damage";
            case MELEE_DAMAGE_TAKEN_MULT -> "Melee Damage Taken";
            case RANGED_DAMAGE_TAKEN_MULT -> "Ranged Damage Taken";
            case MAGIC_DAMAGE_TAKEN_MULT -> "Magic Damage Taken";
            case CRIT_DAMAGE -> "Critical Damage";
            case CRIT_CHANCE -> "Critical Chance";
            case FLAT_DAMAGE_REDUCTION -> "Flat Reduction";
            case STAMINA_DRAIN_MULT -> "Stamina Drain";
            default -> humanizeStatId(stat.getId());
        };
    }

    private static String humanizeStatId(String id) {
        if (id == null || id.isBlank()) {
            return "Unknown";
        }
        String raw = id.replace('_', ' ').trim();
        StringBuilder sb = new StringBuilder(raw.length());
        boolean upperNext = true;
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == ' ') {
                sb.append(' ');
                upperNext = true;
                continue;
            }
            sb.append(upperNext ? Character.toUpperCase(ch) : ch);
            upperNext = false;
        }
        return sb.toString();
    }

    private static void publishCombatLog(CombatLogEntry.Builder builder) {
        if (builder == null) {
            return;
        }
        EventBus.publish(new CombatLogEvent(builder.build()));
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

    private static AttackType inferAttackType(Store<EntityStore> store, Ref<EntityStore> attackerRef) {
        if (store == null || attackerRef == null) {
            return null;
        }
        ItemStack itemStack = itemInHand(store, attackerRef);
        if (itemStack == null || itemStack.isEmpty()) {
            return AttackType.MELEE;
        }
        Item item = itemStack.getItem();
        if (item == null) {
            return AttackType.MELEE;
        }
        String family = familyTag(item.getData());
        String probe = family != null ? family : item.getId();
        if (probe == null) {
            return AttackType.MELEE;
        }
        String lower = probe.toLowerCase();
        if (lower.contains("bow") || lower.contains("crossbow") || lower.contains("gun")
                || lower.contains("rifle") || lower.contains("pistol") || lower.contains("musket")) {
            return AttackType.RANGED;
        }
        if (lower.contains("staff") || lower.contains("wand") || lower.contains("tome")
                || lower.contains("spell") || lower.contains("magic")) {
            return AttackType.MAGIC;
        }
        return AttackType.MELEE;
    }

    private static String resolvePlayerName(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (store == null || ref == null) {
            return null;
        }
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef =
                com.talania.core.utils.PlayerRefUtil.resolve(ref, store);
        if (playerRef == null) {
            return null;
        }
        String name = playerRef.getUsername();
        return name == null || name.isBlank() ? null : name;
    }

    private static String resolveEntityName(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (store == null || ref == null || !ref.isValid()) {
            return null;
        }
        String playerName = resolvePlayerName(store, ref);
        if (playerName != null) {
            return playerName;
        }
        DisplayNameComponent displayName =
                (DisplayNameComponent) store.getComponent(ref, DisplayNameComponent.getComponentType());
        if (displayName == null) {
            return null;
        }
        Message message = displayName.getDisplayName();
        if (message == null) {
            return null;
        }
        String raw = message.getRawText();
        if (raw != null && !raw.isBlank()) {
            return raw;
        }
        String messageId = message.getMessageId();
        if (messageId != null && !messageId.isBlank()) {
            String humanized = humanizeMessageId(messageId);
            return humanized == null || humanized.isBlank() ? messageId : humanized;
        }
        return null;
    }

    private static String humanizeMessageId(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            return null;
        }
        String trimmed = messageId.trim();
        String[] parts = trimmed.split("[/.:]");
        String core = null;
        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }
            String lower = part.toLowerCase();
            if (lower.equals("name") || lower.equals("title") || lower.equals("display") || lower.equals("label")) {
                continue;
            }
            if (lower.equals("entity") && i > 0) {
                continue;
            }
            core = part;
            break;
        }
        if (core == null) {
            core = trimmed;
        }
        core = core.replace('_', ' ').replace('-', ' ').trim();
        if (core.isBlank()) {
            return core;
        }
        return Character.toUpperCase(core.charAt(0)) + core.substring(1);
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
