package com.talania.core.combat.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.combat.targeting.AreaOfEffect;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility for AOE damage in a sphere.
 *
 * <p>How it works:</p>
 * <ul>
 *   <li>Collects entities within a radius using {@link AreaOfEffect}.</li>
 *   <li>Optionally filters and/or excludes players.</li>
 *   <li>Executes {@code DamageSystems.executeDamage} for each target.</li>
 * </ul>
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
        List<Ref<EntityStore>> targets =
                AreaOfEffect.collectSphere(sourceRef, store, center, radius, includePlayers, filter);
        int hits = 0;
        for (Ref<EntityStore> targetRef : targets) {
            if (targetRef == null || !targetRef.isValid()) {
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
        List<Ref<EntityStore>> targets =
                AreaOfEffect.collectSphere(sourceRef, accessor, center, radius, includePlayers, filter);
        int hits = 0;
        for (Ref<EntityStore> targetRef : targets) {
            if (targetRef == null || !targetRef.isValid()) {
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
