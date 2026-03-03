package com.talania.races.cosmetics;

import com.goodwitchlalya.lalyan_cosmetic_core.util.AttachmentsRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.races.RaceType;

import java.util.UUID;

public final class RaceCosmeticOverrides {
    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger("TalaniaRaces/Cosmetics");

    public static final String ID_BEASTKIN_EARS = "Talania_Beastkin_Ears";
    public static final String ID_ORC_TEETH = "Talania_Orc_Teeth";
    public static final String ID_STARBORN_GEM = "Talania_Starborn_Gem";

    private RaceCosmeticOverrides() {}

    public static void ensureRegistered() {
        // Lalyan loads cosmetics from asset folders; nothing to register here.
    }

    public static void apply(PlayerRef playerRef, RaceType race) {
        if (playerRef == null) {
            return;
        }
        applyInternal(playerRef, race);
    }

    public static void clear(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }
        applyInternal(playerRef, null);
    }

    public static void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        PlayerRef playerRef = Universe.get().getPlayer(playerId);
        if (playerRef != null) {
            clear(playerRef);
        }
    }

    public static void refreshBase(PlayerRef playerRef) {
        // No-op for Lalyan path.
    }

    public static void restoreBase(PlayerRef playerRef) {
        // No-op for Lalyan path.
    }

    public static void clearBase(PlayerRef playerRef) {
        // No-op for Lalyan path.
    }

    public static void clearBase(UUID playerId) {
        // No-op for Lalyan path.
    }

    private static void applyInternal(PlayerRef playerRef, RaceType race) {
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = ref != null ? ref.getStore() : null;
        if (ref == null || store == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            AttachmentsRegistry registry = AttachmentsRegistry.get();
            if (registry == null) {
                LOG.warning("Lalyan Cosmetic Core not available; cannot apply race cosmetics.");
                return;
            }

            removeCosmetic(registry, ref, ID_BEASTKIN_EARS);
            removeCosmetic(registry, ref, ID_ORC_TEETH);
            removeCosmetic(registry, ref, ID_STARBORN_GEM);

            if (race == RaceType.BEASTKIN) {
                addCosmetic(registry, ref, ID_BEASTKIN_EARS);
            } else if (race == RaceType.ORC) {
                addCosmetic(registry, ref, ID_ORC_TEETH);
            } else if (race == RaceType.STARBORN) {
                addCosmetic(registry, ref, ID_STARBORN_GEM);
            }
        });
    }

    private static void addCosmetic(AttachmentsRegistry registry, Ref<EntityStore> ref, String id) {
        if (registry.getAttachmentsRegistry().get(id) == null) {
            LOG.warning("Lalyan cosmetic not found: " + id);
            return;
        }
        registry.addCosmetic(ref, id, false);
    }

    private static void removeCosmetic(AttachmentsRegistry registry, Ref<EntityStore> ref, String id) {
        if (registry.getAttachmentsRegistry().get(id) == null) {
            return;
        }
        registry.removeCosmetic(ref, id);
    }

    // Kept for dev UI compatibility; Lalyan handles model rebuild internally.
    public static void applySkinAttachments(Ref<EntityStore> ref, Store<EntityStore> store, PlayerSkin skin) {
        // No-op in Lalyan-only mode.
    }
}
