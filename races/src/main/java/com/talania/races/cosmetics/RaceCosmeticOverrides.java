package com.talania.races.cosmetics;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.cosmetics.CosmeticDefinition;
import com.talania.core.cosmetics.TalaniaCosmetics;
import com.talania.races.RaceType;

import java.util.List;
import java.util.UUID;

public final class RaceCosmeticOverrides {

    public static final String ID_BEASTKIN_EARS = "Talania_Beastkin_Ears";
    public static final String ID_ORC_TEETH = "Talania_Orc_Teeth";
    public static final String ID_STARBORN_GEM = "Talania_Starborn_Gem";

    private RaceCosmeticOverrides() {}

    public static void ensureRegistered() {
        TalaniaCosmetics.register(CosmeticDefinition.builder(
                ID_BEASTKIN_EARS,
                "Ears",
                "Resources/Characters/Ears/Talania_Beastkin_Ears/Talania_Beastkin_Ears.blockymodel",
                "Resources/Characters/Ears/Talania_Beastkin_Ears/Talania_Beastkin_Ears.png")
                .icon("Resources/Characters/Ears/Talania_Beastkin_Ears/Icon/Talania_Beastkin_Ears.png")
                .gradientSet("Skin")
                .build());
        TalaniaCosmetics.register(CosmeticDefinition.builder(
                ID_ORC_TEETH,
                "Mouths",
                "Resources/Characters/Mouth/Talania_Orc_Teeth/Talania_Orc_Teeth.blockymodel",
                "Resources/Characters/Mouth/Talania_Orc_Teeth/Talania_Orc_Teeth.png")
                .icon("Resources/Characters/Mouth/Talania_Orc_Teeth/Icon/Talania_Orc_Teeth.png")
                .gradientSet("Skin")
                .build());
        TalaniaCosmetics.register(CosmeticDefinition.builder(
                ID_STARBORN_GEM,
                "Face_Accessories",
                "Resources/Characters/Face_Details/Talania_Starborn_Gem/Talania_Starborn_Gem.blockymodel",
                "Resources/Characters/Face_Details/Talania_Starborn_Gem/Talania_Starborn_Gem.png")
                .icon("Resources/Characters/Face_Details/Talania_Starborn_Gem/Icon/Talania_Starborn_Gem.png")
                .build());
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

    private static void applyInternal(PlayerRef playerRef, RaceType race) {
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = ref != null ? ref.getStore() : null;
        if (ref == null || store == null) {
            return;
        }
        List<String> overrides = new java.util.ArrayList<>();
        if (race == RaceType.BEASTKIN) {
            overrides.add(ID_BEASTKIN_EARS);
        } else if (race == RaceType.ORC) {
            overrides.add(ID_ORC_TEETH);
        } else if (race == RaceType.STARBORN) {
            overrides.add(ID_STARBORN_GEM);
        }
        TalaniaCosmetics.setOverrides(playerRef, overrides);
    }

    // Kept for dev UI compatibility; TalaniaCosmeticCore rebuilds models on overrides.
    public static void applySkinAttachments(Ref<EntityStore> ref, Store<EntityStore> store, PlayerSkin skin) {
        // No-op in TalaniaCosmeticCore mode.
    }

    public static void refreshBase(PlayerRef playerRef) {
        TalaniaCosmetics.refreshBase(playerRef);
    }

    public static void restoreBase(PlayerRef playerRef) {
        TalaniaCosmetics.clearOverrides(playerRef);
    }

    public static void clearBase(PlayerRef playerRef) {
        TalaniaCosmetics.refreshBase(playerRef);
    }

    public static void clearBase(UUID playerId) {
        if (playerId == null) {
            return;
        }
        PlayerRef playerRef = Universe.get().getPlayer(playerId);
        if (playerRef != null) {
            clearBase(playerRef);
        }
    }
}
