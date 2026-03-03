package com.talania.races.debug.dev;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.cosmetics.TalaniaCosmetics;
import com.talania.races.TalaniaRacesPlugin;

import javax.annotation.Nonnull;
import java.util.List;

public final class TalaniaRacesDebugCosmeticsStatusPage extends InteractiveCustomUIPage {
    private final PlayerRef playerRef;
    private final TalaniaRacesPlugin plugin;

    public TalaniaRacesDebugCosmeticsStatusPage(PlayerRef playerRef, TalaniaRacesPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaRacesDebugCosmeticsStatusEventData.CODEC);
        this.playerRef = playerRef;
        this.plugin = plugin;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaRacesDebugCosmeticsStatusPage.ui");
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaRacesDebugCosmeticsStatusEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        if ("Return".equals(eventData.action)) {
            TalaniaRacesDebugMenuPage.open(ref, store, plugin);
        }
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        List<String> registered = TalaniaCosmetics.getRegisteredIds();
        List<String> active = TalaniaCosmetics.getOverrides(playerRef);
        String registeredText = registered.isEmpty() ? "none" : String.join(", ", registered);
        String activeText = active.isEmpty() ? "none" : String.join(", ", active);
        commandBuilder.set("#RegisteredList.Text", registeredText);
        commandBuilder.set("#ActiveList.Text", activeText);
    }

    public static void open(Ref<EntityStore> ref, Store<EntityStore> store, TalaniaRacesPlugin plugin) {
        if (ref == null || store == null || plugin == null) {
            return;
        }
        PlayerRef resolved = com.talania.core.utils.PlayerRefUtil.resolve(ref, store);
        if (resolved == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new TalaniaRacesDebugCosmeticsStatusPage(resolved, plugin));
            }
        });
    }

    public static final class TalaniaRacesDebugCosmeticsStatusEventData {
        public static final BuilderCodec<TalaniaRacesDebugCosmeticsStatusEventData> CODEC;
        private String action;

        static {
            BuilderCodec.Builder<TalaniaRacesDebugCosmeticsStatusEventData> builder =
                    BuilderCodec.builder(TalaniaRacesDebugCosmeticsStatusEventData.class,
                            TalaniaRacesDebugCosmeticsStatusEventData::new);
            builder.addField(new KeyedCodec("Action", Codec.STRING),
                    (entry, s) -> entry.action = s,
                    (entry) -> entry.action);
            CODEC = builder.build();
        }
    }
}
