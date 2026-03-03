package com.talania.core.debug.dev;

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
import com.talania.core.cosmetics.TalaniaCosmeticCore;
import com.talania.core.cosmetics.TalaniaCosmetics;

import javax.annotation.Nonnull;

public final class TalaniaDebugCosmeticsOffsetPage extends InteractiveCustomUIPage {
    private static final float OFFSET_STEP = 1.0f;
    private final PlayerRef playerRef;
    private final String cosmeticId;

    public TalaniaDebugCosmeticsOffsetPage(PlayerRef playerRef, String cosmeticId) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaDebugCosmeticsOffsetEventData.CODEC);
        this.playerRef = playerRef;
        this.cosmeticId = cosmeticId;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaDebugCosmeticsOffsetPage.ui");
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaDebugCosmeticsOffsetEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        switch (eventData.action) {
            case "Back" -> {
                openMain(ref, store);
                return;
            }
            case "OffsetXMinus" -> adjustOffset(-OFFSET_STEP, 0, 0);
            case "OffsetXPlus" -> adjustOffset(OFFSET_STEP, 0, 0);
            case "OffsetYMinus" -> adjustOffset(0, -OFFSET_STEP, 0);
            case "OffsetYPlus" -> adjustOffset(0, OFFSET_STEP, 0);
            case "OffsetZMinus" -> adjustOffset(0, 0, -OFFSET_STEP);
            case "OffsetZPlus" -> adjustOffset(0, 0, OFFSET_STEP);
            case "OffsetReset" -> TalaniaCosmetics.resetDebugOffset(playerRef, cosmeticId);
            default -> {
            }
        }
        refresh(ref, store);
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton",
                new EventData().append("Action", "Back"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetXMinus",
                new EventData().append("Action", "OffsetXMinus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetXPlus",
                new EventData().append("Action", "OffsetXPlus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetYMinus",
                new EventData().append("Action", "OffsetYMinus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetYPlus",
                new EventData().append("Action", "OffsetYPlus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetZMinus",
                new EventData().append("Action", "OffsetZMinus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetZPlus",
                new EventData().append("Action", "OffsetZPlus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetReset",
                new EventData().append("Action", "OffsetReset"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Cosmetic Offset");
        commandBuilder.set("#SelectedLabel.Text", "Selected:");
        commandBuilder.set("#SelectedNameLabel.Text", cosmeticId == null ? "none" : cosmeticId);
        TalaniaCosmeticCore.Offset offset = TalaniaCosmetics.getDebugOffset(playerRef, cosmeticId);
        commandBuilder.set("#OffsetValue.Text",
                "X: " + format(offset.x) + "  Y: " + format(offset.y) + "  Z: " + format(offset.z));
    }

    private void adjustOffset(float dx, float dy, float dz) {
        TalaniaCosmeticCore.Offset current = TalaniaCosmetics.getDebugOffset(playerRef, cosmeticId);
        TalaniaCosmetics.setDebugOffset(playerRef, cosmeticId, current.add(dx, dy, dz));
    }

    private void refresh(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(commandBuilder);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void openMain(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new TalaniaDebugCosmeticsPage(playerRef));
            }
        });
    }

    private String format(float value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.format("%.2f", value);
    }

    public static final class TalaniaDebugCosmeticsOffsetEventData {
        public static final BuilderCodec<TalaniaDebugCosmeticsOffsetEventData> CODEC;
        private String action;

        static {
            BuilderCodec.Builder<TalaniaDebugCosmeticsOffsetEventData> builder =
                    BuilderCodec.builder(TalaniaDebugCosmeticsOffsetEventData.class,
                            TalaniaDebugCosmeticsOffsetEventData::new);
            builder.addField(new KeyedCodec("Action", Codec.STRING),
                    (entry, s) -> entry.action = s,
                    (entry) -> entry.action);
            CODEC = builder.build();
        }
    }
}
