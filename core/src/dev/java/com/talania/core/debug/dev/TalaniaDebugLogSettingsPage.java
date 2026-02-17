package com.talania.core.debug.dev;

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
import com.talania.core.debug.DebugCategory;
import com.talania.core.debug.TalaniaDebug;

import javax.annotation.Nonnull;
import java.util.Map;

public final class TalaniaDebugLogSettingsPage extends InteractiveCustomUIPage {
    private static final Map<DebugCategory, String> PREFIX = Map.ofEntries(
            Map.entry(DebugCategory.DAMAGE, "Damage"),
            Map.entry(DebugCategory.MODIFIERS, "Modifiers"),
            Map.entry(DebugCategory.COOLDOWN, "Cooldown"),
            Map.entry(DebugCategory.INPUT, "Input"),
            Map.entry(DebugCategory.ACTIVATION, "Activation"),
            Map.entry(DebugCategory.PROFILE, "Profile"),
            Map.entry(DebugCategory.SYSTEM, "System"),
            Map.entry(DebugCategory.UI, "Ui"),
            Map.entry(DebugCategory.PROJECTILES, "Projectiles"),
            Map.entry(DebugCategory.EFFECTS, "Effects"),
            Map.entry(DebugCategory.COMBAT_LOG, "CombatLog")
    );

    private final PlayerRef playerRef;

    public TalaniaDebugLogSettingsPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaDebugLogSettingsEventData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaDebugLogSettingsPage.ui");
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaDebugLogSettingsEventData eventData)) {
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

        if ("Toggle".equals(eventData.action)) {
            DebugCategory category = DebugCategory.fromId(eventData.value);
            if (category != null) {
                TalaniaDebug.logs().toggle(playerRef.getUuid(), category);
            }
        }

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(commandBuilder);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        for (Map.Entry<DebugCategory, String> entry : PREFIX.entrySet()) {
            String prefix = entry.getValue();
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#" + prefix + "Toggle",
                    new EventData().append("Action", "Toggle").append("Value", entry.getKey().id()), false);
        }
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                new EventData().append("Action", "Close"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Talania Debug");
        commandBuilder.set("#SubtitleLabel.Text", "Dev build only. Toggle per-player debug categories.");

        for (Map.Entry<DebugCategory, String> entry : PREFIX.entrySet()) {
            DebugCategory category = entry.getKey();
            String prefix = entry.getValue();
            boolean enabled = TalaniaDebug.logs().isEnabled(playerRef.getUuid(), category);
            commandBuilder.set("#" + prefix + "Description.Text", category.description());
            commandBuilder.set("#" + prefix + "Value.Text", enabled ? "Enabled" : "Disabled");
        }
    }

    public static final class TalaniaDebugLogSettingsEventData {
        public static final BuilderCodec<TalaniaDebugLogSettingsEventData> CODEC;
        private String action;
        private String value;

        static {
            BuilderCodec.Builder<TalaniaDebugLogSettingsEventData> builder =
                    BuilderCodec.builder(TalaniaDebugLogSettingsEventData.class,
                            TalaniaDebugLogSettingsEventData::new);
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
