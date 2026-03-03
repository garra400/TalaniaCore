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
    // Offset editor is disabled in the main menu. Runtime offsets require model patching.
    private static final float OFFSET_STEP = 1.0f;
    private static final float OFFSET_STEP_LARGE = 5.0f;
    private static final float FRONT_YAW = 3.1415927f;
    private final PlayerRef playerRef;
    private final String cosmeticId;
    private boolean frontView = false;
    private final boolean viewOnly;
    private java.util.Timer cameraTimer;
    private volatile boolean cameraActive;

    public TalaniaDebugCosmeticsOffsetPage(PlayerRef playerRef, String cosmeticId) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaDebugCosmeticsOffsetEventData.CODEC);
        this.playerRef = playerRef;
        this.cosmeticId = cosmeticId;
        this.viewOnly = false;
    }

    public TalaniaDebugCosmeticsOffsetPage(PlayerRef playerRef, boolean viewOnly) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaDebugCosmeticsOffsetEventData.CODEC);
        this.playerRef = playerRef;
        this.cosmeticId = null;
        this.viewOnly = viewOnly;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaDebugCosmeticsOffsetPage.ui");
        bindEvents(eventBuilder);
        applyState(commandBuilder);
        cameraActive = true;
        forceThirdPerson(ref, store, frontView);
        startCameraLock();
    }

    @Override
    public void onDismiss(@Nonnull Ref ref, @Nonnull Store store) {
        cameraActive = false;
        stopCameraLock();
        resetCamera(ref, store);
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
                cameraActive = false;
                stopCameraLock();
                resetCamera(ref, store);
                return;
            }
            case "ToggleView" -> {
                frontView = !frontView;
                forceThirdPerson(ref, store, frontView);
            }
            case "ToggleHideBase" -> TalaniaCosmetics.setDebugHideBase(playerRef,
                    !TalaniaCosmetics.isDebugHideBase(playerRef));
            case "ToggleStripBase" -> TalaniaCosmetics.setDebugStripBase(playerRef,
                    !TalaniaCosmetics.isDebugStripBase(playerRef));
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
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#HideBaseButton",
                new EventData().append("Action", "ToggleHideBase"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StripBaseButton",
                new EventData().append("Action", "ToggleStripBase"), false);
        if (viewOnly) {
            return;
        }
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
        boolean hideBase = TalaniaCosmetics.isDebugHideBase(playerRef);
        boolean stripBase = TalaniaCosmetics.isDebugStripBase(playerRef);
        commandBuilder.set("#HideBaseButton.Text", hideBase ? "Hide Base: On" : "Hide Base: Off");
        commandBuilder.set("#StripBaseButton.Text", stripBase ? "Strip Base: On" : "Strip Base: Off");
        if (viewOnly) {
            commandBuilder.set("#TitleLabel.Text", "Cosmetics View");
            commandBuilder.set("#SelectedLabel.Visible", false);
            commandBuilder.set("#SelectedNameLabel.Visible", false);
            commandBuilder.set("#OffsetValue.Visible", false);
            commandBuilder.set("#OffsetRowX.Visible", false);
            commandBuilder.set("#OffsetRowY.Visible", false);
            commandBuilder.set("#OffsetRowZ.Visible", false);
            commandBuilder.set("#OffsetResetRow.Visible", false);
        }
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
            settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
            settings.distance = 1.4f;
            settings.eyeOffset = true;
            settings.rotationOffset = new Direction(front ? FRONT_YAW : 0.0f, 0.0f, 0.0f);
            settings.sendMouseMotion = false;
            settings.displayCursor = false;
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

    private void startCameraLock() {
        if (cameraTimer != null) {
            return;
        }
        cameraTimer = new java.util.Timer("TalaniaDebugCameraLock", true);
        cameraTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (!cameraActive) {
                    stopCameraLock();
                    return;
                }
                Ref<EntityStore> ref = getRef();
                Store<EntityStore> store = getStore();
                if (ref == null || store == null) {
                    return;
                }
                store.getExternalData().getWorld().execute(() -> {
                    if (!cameraActive || !ref.isValid()) {
                        stopCameraLock();
                        return;
                    }
                    forceThirdPerson(ref, store, frontView);
                });
            }
        }, 250L, 250L);
    }

    private void stopCameraLock() {
        if (cameraTimer != null) {
            cameraTimer.cancel();
            cameraTimer = null;
        }
    }


    private Ref<EntityStore> getRef() {
        return playerRef != null ? playerRef.getReference() : null;
    }

    private Store<EntityStore> getStore() {
        Ref<EntityStore> ref = getRef();
        return ref != null ? ref.getStore() : null;
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
