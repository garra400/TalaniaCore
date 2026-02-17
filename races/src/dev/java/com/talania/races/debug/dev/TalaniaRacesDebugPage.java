package com.talania.races.debug.dev;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.races.RaceType;
import com.talania.races.TalaniaRacesPlugin;

import javax.annotation.Nonnull;

public final class TalaniaRacesDebugPage extends InteractiveCustomUIPage {
    private final PlayerRef playerRef;
    private final TalaniaRacesPlugin plugin;

    public TalaniaRacesDebugPage(PlayerRef playerRef, TalaniaRacesPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaRacesDebugEventData.CODEC);
        this.playerRef = playerRef;
        this.plugin = plugin;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaRacesDebugPage.ui");
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaRacesDebugEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        if ("Close".equals(eventData.action)) {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().setPage(ref, store, Page.None);
            }
            return;
        }
        if ("SetRace".equals(eventData.action) && eventData.value != null) {
            RaceType race = RaceType.fromId(eventData.value);
            if (race != null) {
                plugin.setRace(playerRef.getUuid(), race);
            }
        }

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(commandBuilder);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                new EventData().append("Action", "Close"), false);
        for (RaceType race : RaceType.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#" + race.id() + "Button",
                    new EventData().append("Action", "SetRace").append("Value", race.id()), false);
        }
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Races Debug");
        RaceType current = plugin.raceService().getRace(playerRef.getUuid());
        String currentLabel = current != null ? current.displayName() : "None";
        commandBuilder.set("#CurrentRaceLabel.Text", "Current: " + currentLabel);
        for (RaceType race : RaceType.values()) {
            commandBuilder.set("#" + race.id() + "Label.Text", race.displayName());
            commandBuilder.set("#" + race.id() + "Button.Text", "Set");
        }
    }

    public static void open(PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store,
                            TalaniaRacesPlugin plugin) {
        if (playerRef == null || ref == null || store == null || plugin == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new TalaniaRacesDebugPage(playerRef, plugin));
            }
        });
    }

    public static final class TalaniaRacesDebugEventData {
        public static final BuilderCodec<TalaniaRacesDebugEventData> CODEC;
        private String action;
        private String value;

        static {
            BuilderCodec.Builder<TalaniaRacesDebugEventData> builder =
                    BuilderCodec.builder(TalaniaRacesDebugEventData.class, TalaniaRacesDebugEventData::new);
            builder.addField(new KeyedCodec("Action", Codec.STRING),
                    (entry, s) -> entry.action = s,
                    (entry) -> entry.action);
            builder.addField(new KeyedCodec("Value", Codec.STRING),
                    (entry, s) -> entry.value = s,
                    (entry) -> entry.value);
            CODEC = builder.build();
        }
    }
}
