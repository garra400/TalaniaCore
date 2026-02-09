package com.talania.core.combat;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility for applying AOE damage in a sphere.
 */
public final class AreaDamage {
    private AreaDamage() {}

    public static int damageSphere(@Nullable Ref<EntityStore> sourceRef,
                                   Store<EntityStore> store,
                                   Vector3d center,
                                   double radius,
                                   DamageCause cause,
                                   float amount,
                                   boolean includePlayers,
                                   @Nullable Predicate<Ref<EntityStore>> filter) {
        if (store == null || center == null || radius <= 0.0 || amount <= 0.0F) {
            return 0;
        }
        List<Ref<EntityStore>> targets = TargetUtil.getAllEntitiesInSphere(center, radius, store);
        int hits = 0;
        for (Ref<EntityStore> targetRef : targets) {
            if (targetRef == null || !targetRef.isValid()) {
                continue;
            }
            if (sourceRef != null && sourceRef.equals(targetRef)) {
                continue;
            }
            if (!includePlayers) {
                Player player = (Player) store.getComponent(targetRef, Player.getComponentType());
                if (player != null) {
                    continue;
                }
            }
            if (filter != null && !filter.test(targetRef)) {
                continue;
            }
            Damage.Source source = sourceRef == null ? Damage.NULL_SOURCE : new Damage.EntitySource(sourceRef);
            Damage damage = new Damage(source, cause, amount);
            DamageSystems.executeDamage(targetRef, store, damage);
            hits++;
        }
        return hits;
    }

    public static int damageSphere(@Nullable Ref<EntityStore> sourceRef,
                                   ComponentAccessor accessor,
                                   Vector3d center,
                                   double radius,
                                   DamageCause cause,
                                   float amount,
                                   boolean includePlayers,
                                   @Nullable Predicate<Ref<EntityStore>> filter) {
        if (accessor == null || center == null || radius <= 0.0 || amount <= 0.0F) {
            return 0;
        }
        List<Ref<EntityStore>> targets = TargetUtil.getAllEntitiesInSphere(center, radius, accessor);
        int hits = 0;
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
            Damage.Source source = sourceRef == null ? Damage.NULL_SOURCE : new Damage.EntitySource(sourceRef);
            Damage damage = new Damage(source, cause, amount);
            DamageSystems.executeDamage(targetRef, accessor, damage);
            hits++;
        }
        return hits;
    }
}
