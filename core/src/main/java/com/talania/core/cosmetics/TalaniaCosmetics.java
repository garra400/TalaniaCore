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

    public static java.util.List<String> getRegisteredIds() {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core == null) {
            return java.util.List.of();
        }
        return core.getRegisteredIds();
    }

    public static java.util.List<String> getOverrides(PlayerRef playerRef) {
        if (playerRef == null) {
            return java.util.List.of();
        }
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core == null) {
            return java.util.List.of();
        }
        return core.getOverrides(playerRef.getUuid());
    }

    public static boolean isDebugHideBase(PlayerRef playerRef) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core == null || playerRef == null) {
            return false;
        }
        return core.isDebugHideBase(playerRef.getUuid());
    }

    public static boolean isDebugStripBase(PlayerRef playerRef) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core == null || playerRef == null) {
            return false;
        }
        return core.isDebugStripBase(playerRef.getUuid());
    }

    public static java.util.List<String> getDebugVisible(PlayerRef playerRef) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core == null || playerRef == null) {
            return java.util.List.of();
        }
        return core.getDebugVisible(playerRef.getUuid());
    }

    public static TalaniaCosmeticCore.Offset getDebugOffset(PlayerRef playerRef, String cosmeticId) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core == null || playerRef == null) {
            return TalaniaCosmeticCore.Offset.ZERO;
        }
        return core.getDebugOffset(playerRef.getUuid(), cosmeticId);
    }

    public static void setDebugHideBase(PlayerRef playerRef, boolean hideBase) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.setDebugHideBase(playerRef, hideBase);
        }
    }

    public static void setDebugStripBase(PlayerRef playerRef, boolean stripBase) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.setDebugStripBase(playerRef, stripBase);
        }
    }

    public static void toggleDebugVisible(PlayerRef playerRef, String cosmeticId) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.toggleDebugVisible(playerRef, cosmeticId);
        }
    }

    public static void setDebugOffset(PlayerRef playerRef, String cosmeticId, TalaniaCosmeticCore.Offset offset) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.setDebugOffset(playerRef, cosmeticId, offset);
        }
    }

    public static void resetDebugOffset(PlayerRef playerRef, String cosmeticId) {
        TalaniaCosmeticCore core = TalaniaCosmeticCore.get();
        if (core != null) {
            core.resetDebugOffset(playerRef, cosmeticId);
        }
    }
}
