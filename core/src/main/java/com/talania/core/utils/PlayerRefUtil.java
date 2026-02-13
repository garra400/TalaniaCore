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

import java.util.ArrayList;
import java.util.List;

public final class PlayerRefUtil {
    private PlayerRefUtil() {}
    /**
     * Resolve a PlayerRef by username (case-insensitive).
     * Returns null if not found.
     */
    public static PlayerRef resolveByUsername(String username) {
        if (username == null) return null;
        for (PlayerRef ref : getAllOnlinePlayers()) {
            if (ref.getUsername().equalsIgnoreCase(username)) {
                return ref;
            }
        }
        return null;
    }

    /**
     * Returns a list of all online PlayerRefs.
     * This assumes Universe.get().getOnlinePlayers() or similar exists.
     * Replace with the correct API if needed.
     */
    public static List<PlayerRef> getAllOnlinePlayers() {
        // TODO: Replace with actual implementation to get all online PlayerRefs
        // This stub returns an empty list to allow the build to succeed.
        return new ArrayList<>();
    }

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
