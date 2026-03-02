package com.talania.races.ui;

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
import com.talania.core.localization.T;
import com.talania.races.RaceType;
import com.talania.races.TalaniaRacesPlugin;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class TalaniaRaceSelectionPage extends InteractiveCustomUIPage<TalaniaRaceSelectionPage.EventDataPayload> {
    private final PlayerRef playerRef;
    private final TalaniaRacesPlugin plugin;
    private final boolean respec;
    private UUID playerId;
    private RaceType selectedRace;

    public TalaniaRaceSelectionPage(PlayerRef playerRef, TalaniaRacesPlugin plugin, boolean respec) {
        super(playerRef, CustomPageLifetime.CanDismiss, EventDataPayload.CODEC);
        this.playerRef = playerRef;
        this.plugin = plugin;
        this.respec = respec;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaRaceSelectionPage.ui");
        bindEvents(eventBuilder);
        playerId = resolvePlayerId(ref, store);
        if (selectedRace == null) {
            UUID resolvedId = playerId != null ? playerId : playerRef.getUuid();
            selectedRace = resolvedId != null ? plugin.raceService().getRace(resolvedId) : null;
            if (selectedRace == null) {
                selectedRace = RaceType.HUMAN;
            }
        }
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull EventDataPayload payload) {
        if (payload == null) {
            return;
        }
        if (payload.action == null) {
            return;
        }
        if ("ShowRace".equals(payload.action) && payload.value != null) {
            RaceType race = RaceType.fromId(payload.value);
            if (race != null) {
                selectedRace = race;
                UICommandBuilder commandBuilder = new UICommandBuilder();
                UIEventBuilder eventBuilder = new UIEventBuilder();
                bindEvents(eventBuilder);
                applyState(commandBuilder);
                sendUpdate(commandBuilder, eventBuilder, false);
            }
            return;
        }
        if ("SelectRace".equals(payload.action)) {
            RaceType race = selectedRace;
            UUID resolvedId = resolvePlayerId(ref, store);
            if (race != null && resolvedId != null) {
                playerId = resolvedId;
                plugin.setRace(resolvedId, race);
            }
            close();
        }
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        for (RaceType race : RaceType.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#" + uiId(race.id()) + "TabButton",
                    new EventData().append("Action", "ShowRace").append("Value", race.id()), false);
        }
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SelectButton",
                new EventData().append("Action", "SelectRace"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", tr("ui.race_select.title", "Choose a race"));
        commandBuilder.set("#SelectButton.Text", tr("ui.race_select.confirm", "Confirm"));

        for (RaceType race : RaceType.values()) {
            String label = tr("races." + race.id() + ".name", race.displayName());
            if (race == selectedRace) {
                label = "• " + label;
            }
            commandBuilder.set("#" + uiId(race.id()) + "TabButton.Text", label);
        }
        applyRaceDetails(commandBuilder, selectedRace);
    }

    private void applyRaceDetails(UICommandBuilder commandBuilder, RaceType race) {
        if (race == null) {
            return;
        }
        String key = "races." + race.id();
        commandBuilder.set("#RaceName.Text", tr(key + ".name", race.displayName()));
        commandBuilder.set("#RaceTheme.Text", tr(key + ".theme", race.theme()));
        commandBuilder.set("#RaceHistory.Text", tr(key + ".history", ""));
        commandBuilder.set("#RaceBuffName.Text", tr(key + ".buff_name", ""));
        commandBuilder.set("#RaceBuffDesc.Text", tr(key + ".buff_desc", ""));
        commandBuilder.set("#RaceDebuffName.Text", tr(key + ".debuff_name", ""));
        commandBuilder.set("#RaceDebuffDesc.Text", tr(key + ".debuff_desc", ""));
    }

    private String tr(String key, String fallback) {
        if (T.has(key)) {
            return T.t(key);
        }
        return fallback != null ? fallback : key;
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

    public static void open(Ref<EntityStore> ref, Store<EntityStore> store,
                            TalaniaRacesPlugin plugin, boolean respec) {
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
                        new TalaniaRaceSelectionPage(resolved, plugin, respec));
            }
        });
    }

    public static final class EventDataPayload {
        public static final BuilderCodec<EventDataPayload> CODEC;
        private String action;
        private String value;

        static {
            BuilderCodec.Builder<EventDataPayload> builder =
                    BuilderCodec.builder(EventDataPayload.class, EventDataPayload::new);
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
