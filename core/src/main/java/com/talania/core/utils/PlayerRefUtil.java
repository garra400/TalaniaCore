package com.talania.core.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Utility helpers for resolving {@link PlayerRef} from entity refs.
 */
public final class PlayerRefUtil {
    private PlayerRefUtil() {}

    /**
     * Resolve a {@link PlayerRef} from an entity ref by using its UUID.
     *
     * <p>Returns {@code null} if the entity has no UUID or the player is not online.</p>
     */
    public static PlayerRef resolve(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return null;
        }
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return null;
        }
        return Universe.get().getPlayer(uuidComponent.getUuid());
    }
}
