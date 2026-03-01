package com.talania.races.debug.dev;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
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
import java.util.UUID;

public final class TalaniaRacesDebugPage extends InteractiveCustomUIPage {
    private final PlayerRef playerRef;
    private final TalaniaRacesPlugin plugin;
    private UUID playerId;

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
        playerId = resolvePlayerId(ref, store);
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
        if ("Return".equals(eventData.action)) {
            TalaniaRacesDebugMenuPage.open(ref, store, plugin);
            return;
        }
        if ("SetRace".equals(eventData.action) && eventData.value != null) {
            RaceType race = RaceType.fromId(eventData.value);
            if (race != null) {
                UUID resolvedId = resolvePlayerId(ref, store);
                if (resolvedId != null) {
                    playerId = resolvedId;
                    plugin.setRace(resolvedId, race);
                }
            }
        }

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(commandBuilder);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
        for (RaceType race : RaceType.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#" + uiId(race.id()) + "Button",
                    new EventData().append("Action", "SetRace").append("Value", race.id()), false);
        }
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Races: Select Race");
        UUID resolvedId = playerId != null ? playerId : playerRef.getUuid();
        RaceType current = plugin.raceService().getRace(resolvedId);
        String currentLabel = current != null ? singularLabel(current) : "None";
        commandBuilder.set("#CurrentRaceLabel.Text", "Current: " + currentLabel);
        for (RaceType race : RaceType.values()) {
            String id = uiId(race.id());
            commandBuilder.set("#" + id + "Label.Text", singularLabel(race));
            commandBuilder.set("#" + id + "Button.Text", "Set");
        }
    }

    private static String uiId(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        StringBuilder sb = new StringBuilder(id.length());
        boolean upperNext = true;
        for (int i = 0; i < id.length(); i++) {
            char ch = id.charAt(i);
            if (!Character.isLetterOrDigit(ch)) {
                upperNext = true;
                continue;
            }
            if (upperNext) {
                sb.append(Character.toUpperCase(ch));
                upperNext = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String singularLabel(RaceType race) {
        if (race == null) {
            return "";
        }
        return switch (race) {
            case HUMAN -> "Human";
            case HIGH_ELF -> "High Elf";
            case ORC -> "Orc";
            case DWARF -> "Dwarf";
            case NIGHTWALKER -> "Nightwalker";
            case BEASTKIN -> "Beastkin";
            case STARBORN -> "Starborn";
        };
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
                        new TalaniaRacesDebugPage(resolved, plugin));
            }
        });
    }

    private void openRacesMenu(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        TalaniaRacesDebugMenuPage.open(ref, store, plugin);
    }

    private UUID resolvePlayerId(Ref ref, Store store) {
        if (ref == null || store == null) {
            return playerRef != null ? playerRef.getUuid() : null;
        }
        UUIDComponent uuidComponent = (UUIDComponent) store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent != null && uuidComponent.getUuid() != null) {
            return uuidComponent.getUuid();
        }
        return playerRef != null ? playerRef.getUuid() : null;
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
