package com.talania.core;

import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.combat.damage.TalaniaDamageModifierSystem;
import com.talania.core.entities.EntityAnimationSystem;
import com.talania.core.events.entity.npc.NpcDeathEventSystem;
import com.talania.core.events.entity.npc.NpcDeathHandledComponent;
import com.talania.core.projectiles.ProjectileDetectSystem;
import com.talania.core.projectiles.ProjectileOwnerDetectSystem;

import javax.annotation.Nonnull;

/**
 * Core Talania plugin that registers shared ECS systems.
 */
public final class TalaniaCorePlugin extends JavaPlugin {
    private ComponentType<EntityStore, NpcDeathHandledComponent> npcDeathHandledType;

    public TalaniaCorePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ComponentRegistryProxy<EntityStore> registry = getEntityStoreRegistry();
        this.npcDeathHandledType = registry.registerComponent(
                NpcDeathHandledComponent.class, NpcDeathHandledComponent::new);

        registry.registerSystem(new TalaniaDamageModifierSystem());
        registry.registerSystem(new ProjectileDetectSystem());
        registry.registerSystem(new ProjectileOwnerDetectSystem());
        registry.registerSystem(new NpcDeathEventSystem(npcDeathHandledType));
        registry.registerSystem(new EntityAnimationSystem());
    }
}
