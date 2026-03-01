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
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;
import com.talania.races.RaceType;
import com.talania.races.TalaniaRacesPlugin;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class TalaniaRacesDebugMenuPage extends InteractiveCustomUIPage {
    private final PlayerRef playerRef;
    private final TalaniaRacesPlugin plugin;
    private UUID playerId;

    public TalaniaRacesDebugMenuPage(PlayerRef playerRef, TalaniaRacesPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaRacesDebugMenuEventData.CODEC);
        this.playerRef = playerRef;
        this.plugin = plugin;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaRacesDebugMenuPage.ui");
        bindEvents(eventBuilder);
        playerId = resolvePlayerId(ref, store);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaRacesDebugMenuEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        if ("Return".equals(eventData.action)) {
            openDebugMenu(ref, store);
            return;
        }
        if ("OpenRaceList".equals(eventData.action)) {
            TalaniaRacesDebugPage.open(playerRef, ref, store, plugin);
            return;
        }
        if ("OpenCosmetics".equals(eventData.action)) {
            TalaniaRacesDebugCosmeticsPage.open(playerRef, ref, store, plugin);
        }
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OpenRaceListButton",
                new EventData().append("Action", "OpenRaceList"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OpenCosmeticsButton",
                new EventData().append("Action", "OpenCosmetics"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Races Debug");
        commandBuilder.set("#SubtitleLabel.Text", "Dev build only. Race tools and cosmetics testing.");

        UUID resolvedId = playerId != null ? playerId : playerRef.getUuid();
        RaceType current = plugin.raceService().getRace(resolvedId);
        String currentLabel = current != null ? current.displayName() : "None";
        commandBuilder.set("#CurrentRaceLabel.Text", "Current race: " + currentLabel);

        float scale = StatsManager.getStat(resolvedId, StatType.PLAYER_SCALE);
        commandBuilder.set("#CurrentScaleLabel.Text", "Current size: " + format(scale));
    }

    private String format(float value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private void openDebugMenu(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("com.talania.core.debug.dev.TalaniaDebugMenuPage");
            Object page = clazz.getDeclaredConstructor(PlayerRef.class).newInstance(playerRef);
            if (page instanceof CustomUIPage customPage) {
                player.getPageManager().openCustomPage(ref, store, customPage);
            }
        } catch (ClassNotFoundException ignored) {
            // Dev-only classes not present in release build.
        } catch (Exception ignored) {
            // Swallow to avoid breaking debug UI flow.
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
                        new TalaniaRacesDebugMenuPage(playerRef, plugin));
            }
        });
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

    public static final class TalaniaRacesDebugMenuEventData {
        public static final BuilderCodec<TalaniaRacesDebugMenuEventData> CODEC;
        private String action;

        static {
            BuilderCodec.Builder<TalaniaRacesDebugMenuEventData> builder =
                    BuilderCodec.builder(TalaniaRacesDebugMenuEventData.class, TalaniaRacesDebugMenuEventData::new);
            builder.addField(new KeyedCodec("Action", Codec.STRING),
                    (entry, s) -> entry.action = s,
                    (entry) -> entry.action);
            CODEC = builder.build();
        }
    }
}
