package com.talania.core.cosmetics;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Collection;

public final class TalaniaCosmetics {
    private TalaniaCosmetics() {
    }

    public static void register(CosmeticDefinition definition) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.register(definition);
        }
    }

    public static void registerAll(Collection<CosmeticDefinition> definitions) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.registerAll(definitions);
        }
    }

    public static void handlePlayerReady(PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.handlePlayerReady(playerRef, ref, store);
        }
    }

    public static void handlePlayerDisconnect(PlayerRef playerRef) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.handlePlayerDisconnect(playerRef);
        }
    }

    public static void setOverrides(PlayerRef playerRef, Collection<String> cosmeticIds) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.setOverrides(playerRef, cosmeticIds);
        }
    }

    public static void clearOverrides(PlayerRef playerRef) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.clearOverrides(playerRef);
        }
    }

    public static void refreshBase(PlayerRef playerRef) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.refreshBase(playerRef);
        }
    }
}
