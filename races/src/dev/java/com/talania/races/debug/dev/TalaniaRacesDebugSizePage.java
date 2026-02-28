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
import com.talania.core.debug.DebugStatModifierStore;
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.runtime.TalaniaCoreRuntime;
import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;
import com.talania.races.TalaniaRacesPlugin;

import javax.annotation.Nonnull;

public final class TalaniaRacesDebugSizePage extends InteractiveCustomUIPage {
    private static final float STEP = 0.05f;
    private final PlayerRef playerRef;
    private final TalaniaRacesPlugin plugin;

    public TalaniaRacesDebugSizePage(PlayerRef playerRef, TalaniaRacesPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaRacesDebugSizeEventData.CODEC);
        this.playerRef = playerRef;
        this.plugin = plugin;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaRacesDebugSizePage.ui");
        DebugStatModifierStore.load().applyTo(TalaniaDebug.statModifiers(), playerRef.getUuid());
        applyModifiersToStats(ref, store);
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaRacesDebugSizeEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        if ("Return".equals(eventData.action)) {
            TalaniaRacesDebugMenuPage.open(playerRef, ref, store, plugin);
            return;
        }
        if ("Adjust".equals(eventData.action) && eventData.value != null) {
            Float delta = parse(eventData.value);
            if (delta != null) {
                float current = StatsManager.getStat(playerRef.getUuid(), StatType.PLAYER_SCALE);
                setTargetScale(ref, store, current + delta);
            }
            return;
        }
        if ("Set".equals(eventData.action) && eventData.value != null) {
            Float target = parse(eventData.value);
            if (target != null) {
                setTargetScale(ref, store, target);
            }
        }
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ScaleMinusButton",
                new EventData().append("Action", "Adjust").append("Value", String.valueOf(-STEP)), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ScalePlusButton",
                new EventData().append("Action", "Adjust").append("Value", String.valueOf(STEP)), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ScaleDwarfButton",
                new EventData().append("Action", "Set").append("Value", "0.60"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ScaleHumanButton",
                new EventData().append("Action", "Set").append("Value", "1.00"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ScaleOrcButton",
                new EventData().append("Action", "Set").append("Value", "1.40"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ScaleResetButton",
                new EventData().append("Action", "Set").append("Value", "1.00"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        float base = TalaniaDebug.statModifiers().baseValue(playerRef.getUuid(), StatType.PLAYER_SCALE);
        float multiplier = TalaniaDebug.statModifiers().getMultiplier(playerRef.getUuid(), StatType.PLAYER_SCALE);
        float finalValue = StatsManager.getStat(playerRef.getUuid(), StatType.PLAYER_SCALE);

        commandBuilder.set("#TitleLabel.Text", "Races: Player Size");
        commandBuilder.set("#SubtitleLabel.Text", "Sets the final PLAYER_SCALE value for this player.");
        commandBuilder.set("#BaseScaleLabel.Text", "Base (race+mods): " + format(base));
        commandBuilder.set("#DebugScaleLabel.Text", "Debug multiplier: x" + format(multiplier));
        commandBuilder.set("#FinalScaleLabel.Text", "Final size: " + format(finalValue));
    }

    private void setTargetScale(Ref<EntityStore> ref, Store<EntityStore> store, float desired) {
        float clamped = StatType.PLAYER_SCALE.clamp(desired);
        float base = TalaniaDebug.statModifiers().baseValue(playerRef.getUuid(), StatType.PLAYER_SCALE);
        if (base <= 0.0001f) {
            base = 1.0f;
        }
        float multiplier = clamped / base;
        TalaniaDebug.statModifiers().setMultiplier(playerRef.getUuid(), StatType.PLAYER_SCALE, multiplier);
        DebugStatModifierStore.load().saveFrom(TalaniaDebug.statModifiers(), playerRef.getUuid());
        applyModifiersToStats(ref, store);
        refresh();
    }

    private void applyModifiersToStats(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStats stats = StatsManager.getOrCreate(playerRef.getUuid());
        TalaniaDebug.statModifiers().applyToStats(playerRef.getUuid(), stats);
        TalaniaCoreRuntime core = TalaniaCoreRuntime.get();
        if (core != null) {
            core.statSyncService().applyAll(ref, store, playerRef.getUuid(), stats);
        }
    }

    private void refresh() {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(commandBuilder);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private Float parse(String value) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String format(float value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
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
                        new TalaniaRacesDebugSizePage(playerRef, plugin));
            }
        });
    }

    public static final class TalaniaRacesDebugSizeEventData {
        public static final BuilderCodec<TalaniaRacesDebugSizeEventData> CODEC;
        private String action;
        private String value;

        static {
            BuilderCodec.Builder<TalaniaRacesDebugSizeEventData> builder =
                    BuilderCodec.builder(TalaniaRacesDebugSizeEventData.class, TalaniaRacesDebugSizeEventData::new);
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
