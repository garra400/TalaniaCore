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
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.debug.combat.CombatLogEntry;
import com.talania.core.debug.combat.CombatLogFormatter;

import javax.annotation.Nonnull;
import java.util.List;

public final class TalaniaCombatLogPage extends InteractiveCustomUIPage {
    private final PlayerRef playerRef;

    public TalaniaCombatLogPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaCombatLogPageEventData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaCombatLogPage.ui");
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaCombatLogPageEventData eventData)) {
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
        if ("Refresh".equals(eventData.action)) {
            UICommandBuilder commandBuilder = new UICommandBuilder();
            applyState(commandBuilder);
            sendUpdate(commandBuilder, new UIEventBuilder(), false);
        }
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#RefreshButton",
                new EventData().append("Action", "Refresh"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                new EventData().append("Action", "Close"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Combat Log");
        commandBuilder.set("#SubtitleLabel.Text", "Dev build only. Recent combat events.");
        commandBuilder.set("#LogText.Text", buildLogText());
    }

    private String buildLogText() {
        List<CombatLogEntry> entries = TalaniaDebug.combatLog().recent(playerRef.getUuid(), 50);
        if (entries.isEmpty()) {
            return "No combat log entries yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (CombatLogEntry entry : entries) {
            sb.append(CombatLogFormatter.summaryFor(playerRef.getUuid(), entry, null, null)).append("\n");
            for (String line : CombatLogFormatter.modifierLines(entry)) {
                sb.append("  - ").append(line).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    public static final class TalaniaCombatLogPageEventData {
        public static final BuilderCodec<TalaniaCombatLogPageEventData> CODEC;
        private String action;

        static {
            BuilderCodec.Builder<TalaniaCombatLogPageEventData> builder =
                    BuilderCodec.builder(TalaniaCombatLogPageEventData.class,
                            TalaniaCombatLogPageEventData::new);
            builder.addField(new KeyedCodec("Action", Codec.STRING),
                    (entry, s) -> entry.action = s,
                    (entry) -> entry.action);
            CODEC = builder.build();
        }
    }
}
