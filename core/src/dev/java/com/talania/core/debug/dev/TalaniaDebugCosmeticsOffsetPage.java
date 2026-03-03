package com.talania.core.debug.dev;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.AttachedToType;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.PositionType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.io.PacketHandler;
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
    private static final float OFFSET_STEP_LARGE = 5.0f;
    private static final float FRONT_YAW = 3.1415927f;
    private final PlayerRef playerRef;
    private final String cosmeticId;
    private boolean frontView = false;

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
        forceThirdPerson(ref, store, frontView);
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
                resetCamera(ref, store);
                return;
            }
            case "ToggleView" -> {
                frontView = !frontView;
                forceThirdPerson(ref, store, frontView);
            }
            case "OffsetXMinus5" -> adjustOffset(-OFFSET_STEP_LARGE, 0, 0);
            case "OffsetXMinus" -> adjustOffset(-OFFSET_STEP, 0, 0);
            case "OffsetXPlus" -> adjustOffset(OFFSET_STEP, 0, 0);
            case "OffsetXPlus5" -> adjustOffset(OFFSET_STEP_LARGE, 0, 0);
            case "OffsetYMinus5" -> adjustOffset(0, -OFFSET_STEP_LARGE, 0);
            case "OffsetYMinus" -> adjustOffset(0, -OFFSET_STEP, 0);
            case "OffsetYPlus" -> adjustOffset(0, OFFSET_STEP, 0);
            case "OffsetYPlus5" -> adjustOffset(0, OFFSET_STEP_LARGE, 0);
            case "OffsetZMinus5" -> adjustOffset(0, 0, -OFFSET_STEP_LARGE);
            case "OffsetZMinus" -> adjustOffset(0, 0, -OFFSET_STEP);
            case "OffsetZPlus" -> adjustOffset(0, 0, OFFSET_STEP);
            case "OffsetZPlus5" -> adjustOffset(0, 0, OFFSET_STEP_LARGE);
            case "OffsetReset" -> TalaniaCosmetics.resetDebugOffset(playerRef, cosmeticId);
            default -> {
            }
        }
        refresh(ref, store);
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#BackButton",
                new EventData().append("Action", "Back"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ViewToggleButton",
                new EventData().append("Action", "ToggleView"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetXMinus",
                new EventData().append("Action", "OffsetXMinus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetXPlus",
                new EventData().append("Action", "OffsetXPlus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetXMinus5",
                new EventData().append("Action", "OffsetXMinus5"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetXPlus5",
                new EventData().append("Action", "OffsetXPlus5"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetYMinus",
                new EventData().append("Action", "OffsetYMinus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetYPlus",
                new EventData().append("Action", "OffsetYPlus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetYMinus5",
                new EventData().append("Action", "OffsetYMinus5"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetYPlus5",
                new EventData().append("Action", "OffsetYPlus5"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetZMinus",
                new EventData().append("Action", "OffsetZMinus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetZPlus",
                new EventData().append("Action", "OffsetZPlus"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetZMinus5",
                new EventData().append("Action", "OffsetZMinus5"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetZPlus5",
                new EventData().append("Action", "OffsetZPlus5"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OffsetReset",
                new EventData().append("Action", "OffsetReset"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Cosmetic Offset");
        commandBuilder.set("#SelectedLabel.Text", "Selected:");
        commandBuilder.set("#SelectedNameLabel.Text", cosmeticId == null ? "none" : cosmeticId);
        commandBuilder.set("#ViewToggleButton.Text", frontView ? "View: Front" : "View: Back");
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

    private void forceThirdPerson(Ref<EntityStore> ref, Store<EntityStore> store, boolean front) {
        if (ref == null || store == null || playerRef == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            PacketHandler handler = playerRef.getPacketHandler();
            if (handler == null) {
                return;
            }
            ServerCameraSettings settings = new ServerCameraSettings();
            settings.isFirstPerson = false;
            settings.attachedToType = AttachedToType.LocalPlayer;
            settings.positionType = PositionType.AttachedToPlusOffset;
            settings.rotationType = RotationType.AttachedToPlusOffset;
            settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;
            settings.distance = 3.5f;
            settings.eyeOffset = true;
            settings.rotationOffset = new Direction(front ? FRONT_YAW : 0.0f, 0.0f, 0.0f);
            handler.writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, settings));
        });
    }

    private void resetCamera(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null || playerRef == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            PacketHandler handler = playerRef.getPacketHandler();
            if (handler == null) {
                return;
            }
            handler.writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, null));
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
