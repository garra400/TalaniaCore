package com.talania.core.projectiles;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Resolves a projectile creator without mixins, using best-effort heuristics.
 */
public final class ProjectileOwnerResolver {
    private static final Field PROJECTILE_CREATOR_UUID_FIELD = resolveProjectileCreatorField();

    private ProjectileOwnerResolver() {}

    public static UUID resolveShooterUuid(UUID predictedUuid, Ref<EntityStore> projectileRef, Store<EntityStore> store) {
        if (predictedUuid == null || store == null) {
            return predictedUuid;
        }
        UUID fromCreator = resolveCreatorUuid(projectileRef, store);
        if (fromCreator != null) {
            return fromCreator;
        }
        UUID nearest = resolveNearestRangedPlayerUuid(projectileRef, store);
        return nearest != null ? nearest : predictedUuid;
    }

    public static UUID resolveCreatorUuid(Ref<EntityStore> projectileRef, Store<EntityStore> store) {
        if (PROJECTILE_CREATOR_UUID_FIELD == null || projectileRef == null || store == null) {
            return null;
        }
        ProjectileComponent projectileComponent =
                (ProjectileComponent) store.getComponent(projectileRef, ProjectileComponent.getComponentType());
        if (projectileComponent == null) {
            return null;
        }
        try {
            Object value = PROJECTILE_CREATOR_UUID_FIELD.get(projectileComponent);
            return value instanceof UUID ? (UUID) value : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static Field resolveProjectileCreatorField() {
        try {
            Field field = ProjectileComponent.class.getDeclaredField("creatorUuid");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            return null;
        }
    }

    private static UUID resolveNearestRangedPlayerUuid(Ref<EntityStore> projectileRef, Store<EntityStore> store) {
        if (projectileRef == null || store == null) {
            return null;
        }
        TransformComponent transform = store.getComponent(projectileRef, TransformComponent.getComponentType());
        Vector3d position = transform != null ? transform.getPosition() : null;
        if (position == null) {
            return null;
        }
        List<Ref<EntityStore>> candidates = TargetUtil.getAllEntitiesInSphere(position, 4.0, store);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        UUID best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Ref<EntityStore> candidate : candidates) {
            if (candidate == null || !candidate.isValid()) {
                continue;
            }
            Player player = (Player) store.getComponent(candidate, Player.getComponentType());
            if (player == null) {
                continue;
            }
            Inventory inventory = player.getInventory();
            if (inventory == null) {
                continue;
            }
            ItemStack held = inventory.getItemInHand();
            if (held == null || held.isEmpty()) {
                continue;
            }
            Item item = held.getItem();
            String family = item != null ? resolveItemFamily(item) : null;
            if (item == null || !isRangedWeaponFamily(family)) {
                continue;
            }
            TransformComponent candidateTransform =
                    store.getComponent(candidate, TransformComponent.getComponentType());
            Vector3d candidatePos = candidateTransform != null ? candidateTransform.getPosition() : null;
            if (candidatePos == null) {
                continue;
            }
            double distance = candidatePos.distanceSquaredTo(position);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = uuidFor(candidate, store);
            }
        }
        return best;
    }

    private static UUID uuidFor(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        return uuidComponent == null ? null : uuidComponent.getUuid();
    }

    private static String resolveItemFamily(Item item) {
        if (item == null) {
            return null;
        }
        String family = familyTag(item.getData());
        if (family != null) {
            return family;
        }
        return weaponFamilyFromItemId(item.getId());
    }

    private static String familyTag(com.hypixel.hytale.assetstore.AssetExtraInfo.Data data) {
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

    private static boolean isRangedWeaponFamily(String family) {
        if (family == null) {
            return false;
        }
        String normalized = family.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("bow") || normalized.equals("crossbow");
    }
}
