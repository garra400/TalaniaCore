package com.talania.core.combat.targeting;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Helper for spherical area targeting with simple filters.
 */
public final class AreaOfEffect {
    private AreaOfEffect() {}

    /**
     * Collect entities within a radius using the entity store accessor.
     */
    public static List<Ref<EntityStore>> collectSphere(@Nullable Ref<EntityStore> sourceRef,
                                                       ComponentAccessor accessor,
                                                       Vector3d center,
                                                       double radius,
                                                       boolean includePlayers,
                                                       @Nullable Predicate<Ref<EntityStore>> filter) {
        if (accessor == null || center == null || radius <= 0.0) {
            return List.of();
        }
        List<Ref<EntityStore>> targets = TargetUtil.getAllEntitiesInSphere(center, radius, accessor);
        return filterTargets(sourceRef, accessor, targets, includePlayers, filter);
    }

    /**
     * Collect entities within a radius using a store.
     */
    public static List<Ref<EntityStore>> collectSphere(@Nullable Ref<EntityStore> sourceRef,
                                                       Store<EntityStore> store,
                                                       Vector3d center,
                                                       double radius,
                                                       boolean includePlayers,
                                                       @Nullable Predicate<Ref<EntityStore>> filter) {
        if (store == null || center == null || radius <= 0.0) {
            return List.of();
        }
        List<Ref<EntityStore>> targets = TargetUtil.getAllEntitiesInSphere(center, radius, store);
        return filterTargets(sourceRef, store, targets, includePlayers, filter);
    }

    private static List<Ref<EntityStore>> filterTargets(@Nullable Ref<EntityStore> sourceRef,
                                                        ComponentAccessor accessor,
                                                        List<Ref<EntityStore>> targets,
                                                        boolean includePlayers,
                                                        @Nullable Predicate<Ref<EntityStore>> filter) {
        if (targets == null || targets.isEmpty()) {
            return List.of();
        }
        List<Ref<EntityStore>> result = new ArrayList<>();
        for (Ref<EntityStore> targetRef : targets) {
            if (targetRef == null || !targetRef.isValid()) {
                continue;
            }
            if (sourceRef != null && sourceRef.equals(targetRef)) {
                continue;
            }
            if (!includePlayers) {
                Player player = (Player) accessor.getComponent(targetRef, Player.getComponentType());
                if (player != null) {
                    continue;
                }
            }
            if (filter != null && !filter.test(targetRef)) {
                continue;
            }
            result.add(targetRef);
        }
        return result;
    }
}
