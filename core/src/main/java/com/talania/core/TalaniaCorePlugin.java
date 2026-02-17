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
import com.talania.core.input.InputPatternMovementSystem;
import com.talania.core.input.InputPatternPlaceBlockSystem;
import com.talania.core.projectiles.ProjectileDetectSystem;
import com.talania.core.projectiles.ProjectileOwnerDetectSystem;
import com.talania.core.runtime.TalaniaCoreRuntime;
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.module.TalaniaModuleRegistry;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

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
        TalaniaCoreRuntime runtime = TalaniaCoreRuntime.init(getDataDirectory());
        ComponentRegistryProxy<EntityStore> registry = getEntityStoreRegistry();
        this.npcDeathHandledType = registry.registerComponent(
                NpcDeathHandledComponent.class, NpcDeathHandledComponent::new);

        registry.registerSystem(new TalaniaDamageModifierSystem());
        registry.registerSystem(new ProjectileDetectSystem());
        registry.registerSystem(new ProjectileOwnerDetectSystem());
        registry.registerSystem(new NpcDeathEventSystem(npcDeathHandledType));
        registry.registerSystem(new EntityAnimationSystem());
        registry.registerSystem(new InputPatternMovementSystem(runtime.inputPatternTracker()));
        registry.registerSystem(new InputPatternPlaceBlockSystem(runtime.inputPatternTracker()));

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, runtime::handlePlayerReady);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, runtime::handlePlayerDisconnect);
        getEventRegistry().registerGlobal(PlayerMouseButtonEvent.class, runtime::handleMouseButton);

        TalaniaModuleRegistry.get().initModules(this);
        TalaniaDebug.tryRegisterDev(this);
    }
}
