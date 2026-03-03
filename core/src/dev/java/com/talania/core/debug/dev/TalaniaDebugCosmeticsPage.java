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
import com.talania.core.cosmetics.TalaniaCosmetics;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class TalaniaDebugCosmeticsPage extends InteractiveCustomUIPage {
    private static final int ROWS = 10;
    private final PlayerRef playerRef;
    private List<String> cosmeticIds = List.of();

    public TalaniaDebugCosmeticsPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaDebugCosmeticsEventData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaDebugCosmeticsPage.ui");
        cosmeticIds = new ArrayList<>(TalaniaCosmetics.getOverrides(playerRef));
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaDebugCosmeticsEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        switch (eventData.action) {
            case "Return" -> {
                openMenu(ref, store);
                return;
            }
            case "OpenView" -> {
                openView(ref, store);
                return;
            }
            case "ToggleHideBase" -> TalaniaCosmetics.setDebugHideBase(playerRef,
                    !TalaniaCosmetics.isDebugHideBase(playerRef));
            case "ToggleStripBase" -> TalaniaCosmetics.setDebugStripBase(playerRef,
                    !TalaniaCosmetics.isDebugStripBase(playerRef));
            case "ToggleVisible" -> {
                String id = cosmeticIdForIndex(eventData.value);
                if (id != null) {
                    TalaniaCosmetics.toggleDebugVisible(playerRef, id);
                }
            }
            default -> {
            }
        }
        refresh(ref, store);
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ViewModeButton",
                new EventData().append("Action", "OpenView"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#HideBaseButton",
                new EventData().append("Action", "ToggleHideBase"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StripBaseButton",
                new EventData().append("Action", "ToggleStripBase"), false);
        for (int i = 1; i <= ROWS; i++) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Cosmetic" + i + "Toggle",
                    new EventData().append("Action", "ToggleVisible").append("Value", String.valueOf(i)), false);
        }
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Talania Cosmetics");
        commandBuilder.set("#SubtitleLabel.Text", "Dev build only. Toggle cosmetics and isolate attachments.");

        boolean hideBase = TalaniaCosmetics.isDebugHideBase(playerRef);
        boolean stripBase = TalaniaCosmetics.isDebugStripBase(playerRef);
        commandBuilder.set("#HideBaseButton.Text", hideBase ? "Hide Base: On" : "Hide Base: Off");
        commandBuilder.set("#StripBaseButton.Text", stripBase ? "Strip Base: On" : "Strip Base: Off");

        List<String> visible = TalaniaCosmetics.getDebugVisible(playerRef);
        for (int i = 1; i <= ROWS; i++) {
            int index = i - 1;
            if (index >= cosmeticIds.size()) {
                commandBuilder.set("#Cosmetic" + i + "Container.Visible", false);
                continue;
            }
            String id = cosmeticIds.get(index);
            commandBuilder.set("#Cosmetic" + i + "Container.Visible", true);
            commandBuilder.set("#Cosmetic" + i + "Label.Text", id);
            boolean active = visible.contains(id);
            commandBuilder.set("#Cosmetic" + i + "Toggle.Text", active ? "Hide" : "Show");
        }

    }

    private void refresh(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(commandBuilder);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void openMenu(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new TalaniaDebugMenuPage(playerRef));
            }
        });
    }

    private String cosmeticIdForIndex(String value) {
        if (value == null) {
            return null;
        }
        try {
            int index = Integer.parseInt(value) - 1;
            if (index >= 0 && index < cosmeticIds.size()) {
                return cosmeticIds.get(index);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private void openView(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (ref == null || store == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new TalaniaDebugCosmeticsOffsetPage(playerRef, true));
            }
        });
    }

    public static final class TalaniaDebugCosmeticsEventData {
        public static final BuilderCodec<TalaniaDebugCosmeticsEventData> CODEC;
        private String action;
        private String value;

        static {
            BuilderCodec.Builder<TalaniaDebugCosmeticsEventData> builder =
                    BuilderCodec.builder(TalaniaDebugCosmeticsEventData.class, TalaniaDebugCosmeticsEventData::new);
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
