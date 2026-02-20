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
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.debug.combat.CombatLogEntry;
import com.talania.core.debug.combat.CombatLogFormatter;

import javax.annotation.Nonnull;
import java.util.List;

public final class TalaniaCombatLogPage extends InteractiveCustomUIPage {
    private static final int MAX_ROWS = 30;
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
        if ("Return".equals(eventData.action)) {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new TalaniaDebugMenuPage(playerRef));
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
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Combat Log");
        commandBuilder.set("#SubtitleLabel.Text", "Dev build only. Recent combat events.");
        applyRows(commandBuilder);
    }

    private void applyRows(UICommandBuilder commandBuilder) {
        List<CombatLogEntry> entries = TalaniaDebug.combatLog().recent(playerRef.getUuid(), MAX_ROWS);
        commandBuilder.set("#EmptyLabel.Visible", entries.isEmpty());
        int count = entries.size();
        int startIndex = Math.max(0, count - MAX_ROWS);
        for (int i = 0; i < MAX_ROWS; i++) {
            String index = String.valueOf(i + 1);
            String rowId = "#Row" + index;
            int entryIndex = startIndex + i;
            if (entryIndex >= count) {
                commandBuilder.set(rowId + ".Visible", false);
                continue;
            }
            CombatLogEntry entry = entries.get(entryIndex);
            commandBuilder.set(rowId + ".Visible", true);
            commandBuilder.set(rowId + "Summary.Text",
                    CombatLogFormatter.actorSummaryFor(playerRef.getUuid(), entry, null, null));
            commandBuilder.set(rowId + "Damage.Text", CombatLogFormatter.damageText(entry));
            commandBuilder.set(rowId + "Damage.TooltipText", CombatLogFormatter.damageTooltip(entry));
        }
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
