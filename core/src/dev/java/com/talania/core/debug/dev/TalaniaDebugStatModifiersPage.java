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
import com.talania.core.debug.DebugStatModifierService;
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.runtime.TalaniaCoreRuntime;
import com.talania.core.stats.EntityStats;
import com.talania.core.stats.StatType;
import com.talania.core.stats.StatsManager;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class TalaniaDebugStatModifiersPage extends InteractiveCustomUIPage {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
    private static final int UI_ROW_COUNT = 25;
    private final PlayerRef playerRef;
    private final List<RowPair> rowPairs;

    public TalaniaDebugStatModifiersPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaDebugStatModifiersEventData.CODEC);
        this.playerRef = playerRef;
        this.rowPairs = buildRowPairs();
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaDebugStatModifiersPage.ui");
        bindEvents(eventBuilder);
        applyState(commandBuilder, true);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaDebugStatModifiersEventData eventData)) {
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
        if ("AdjustAdd".equals(eventData.action) && eventData.value != null && eventData.delta != null) {
            adjustAdd(ref, store, eventData.value, eventData.delta);
            refresh();
            return;
        }
        if ("AdjustMult".equals(eventData.action) && eventData.value != null && eventData.delta != null) {
            adjustMult(ref, store, eventData.value, eventData.delta);
            refresh();
        }
    }

    private void refresh() {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(commandBuilder, false);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
        for (int i = 0; i < rowPairs.size(); i++) {
            RowPair pair = rowPairs.get(i);
            if (pair.type == RowType.HEADER) {
                continue;
            }
            String index = String.valueOf(i + 1);
            bindSlotEvents(eventBuilder, index, "", pair.left);
            if (pair.right != null) {
                bindSlotEvents(eventBuilder, index, "R", pair.right);
            }
        }
    }

    private void bindSlotEvents(UIEventBuilder eventBuilder, String index, String suffix, RowDefinition row) {
        if (row == null || row.type != RowType.STAT) {
            return;
        }
        StatType stat = row.stat;
        ModKind kind = row.modKind;
        float step = kind == ModKind.ADD ? stepForAdd(stat) : stepForMult(stat);
        String action = kind == ModKind.ADD ? "AdjustAdd" : "AdjustMult";
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Row" + index + suffix + "Minus",
                new EventData().append("Action", action).append("Value", stat.getId())
                        .append("Delta", String.valueOf(-step)), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Row" + index + suffix + "Plus",
                new EventData().append("Action", action).append("Value", stat.getId())
                        .append("Delta", String.valueOf(step)), false);
    }

    private void applyState(UICommandBuilder commandBuilder, boolean setTooltips) {
        commandBuilder.set("#TitleLabel.Text", "Stat Modifiers");
        commandBuilder.set("#SubtitleLabel.Text", "Dev build only. Debug stat modifiers by category.");
        commandBuilder.set("#ToggleLabel.Text", "Debug modifiers: Enabled");

        for (int i = 0; i < UI_ROW_COUNT; i++) {
            String index = String.valueOf(i + 1);
            String rowId = "#Row" + index;
            if (i >= rowPairs.size()) {
                commandBuilder.set(rowId + ".Visible", false);
                continue;
            }
            RowPair pair = rowPairs.get(i);
            commandBuilder.set(rowId + ".Visible", true);
            if (pair.type == RowType.HEADER) {
                applyHeaderRow(commandBuilder, index, pair.header);
            } else {
                applyStatRow(commandBuilder, index, "", pair.left, setTooltips);
                if (pair.right != null) {
                    applyStatRow(commandBuilder, index, "R", pair.right, setTooltips);
                } else {
                    setStatRowVisible(commandBuilder, index, "R", false);
                }
                commandBuilder.set("#Row" + index + "Header.Visible", false);
            }
        }
    }

    private void applyHeaderRow(UICommandBuilder commandBuilder, String index, String title) {
        String headerId = "#Row" + index + "Header";
        commandBuilder.set(headerId + ".Text", title);
        commandBuilder.set(headerId + ".Visible", true);
        setStatRowVisible(commandBuilder, index, "", false);
        setStatRowVisible(commandBuilder, index, "R", false);
    }

    private void applyStatRow(UICommandBuilder commandBuilder, String index, String suffix, RowDefinition row,
                              boolean setTooltips) {
        if (row == null || row.type != RowType.STAT) {
            setStatRowVisible(commandBuilder, index, suffix, false);
            return;
        }
        DebugStatModifierService modifiers = TalaniaDebug.statModifiers();
        float baseValue = modifiers.baseValue(playerRef.getUuid(), row.stat);
        float modValue = row.modKind == ModKind.ADD
                ? modifiers.getDelta(playerRef.getUuid(), row.stat)
                : modifiers.getMultiplier(playerRef.getUuid(), row.stat);
        StatCategory category = categoryFor(row.stat);
        String categoryLabel = categoryLabel(category);
        String statLabel = labelFor(row.stat, row.modKind);
        String baseLabel = row.modKind == ModKind.ADD
                ? "Base: " + formatSigned(baseValue)
                : "Base: x" + format(baseValue);
        String modLabel = row.modKind == ModKind.ADD
                ? "Mod: " + formatSigned(modValue)
                : "Mod: x" + format(modValue);

        commandBuilder.set("#Row" + index + "Header.Visible", false);
        setStatRowVisible(commandBuilder, index, suffix, true);
        commandBuilder.set("#Row" + index + suffix + "Name.Text", statLabel);
        commandBuilder.set("#Row" + index + suffix + "Base.Text", baseLabel);
        commandBuilder.set("#Row" + index + suffix + "Mod.Text", modLabel);

        if (setTooltips) {
            commandBuilder.set("#Row" + index + suffix + "Name.TooltipText",
                    categoryLabel + " stat. " + (row.modKind == ModKind.ADD ? "Additive" : "Multiplicative")
                            + " modifier.");
            commandBuilder.set("#Row" + index + suffix + "Base.TooltipText",
                    "Base value without debug modifiers.");
            if (row.modKind == ModKind.ADD) {
                String addHint = isPercentStat(row.stat)
                        ? "Additive modifier (0.0 - 1.0)."
                        : "Additive modifier (flat delta).";
                commandBuilder.set("#Row" + index + suffix + "Mod.TooltipText", addHint);
            } else {
                commandBuilder.set("#Row" + index + suffix + "Mod.TooltipText",
                        "Multiplicative modifier (1.0 = no change).\nExample: 1.10 = +10%.");
            }
        }
    }

    private void setStatRowVisible(UICommandBuilder commandBuilder, String index, String suffix, boolean visible) {
        for (String field : new String[]{"Name", "Base", "Mod", "Minus", "Plus"}) {
            commandBuilder.set("#Row" + index + suffix + field + ".Visible", visible);
        }
        commandBuilder.set("#Row" + index + (suffix.isEmpty() ? "Left" : "Right") + ".Visible", visible);
    }

    private void adjustAdd(Ref<EntityStore> ref, Store<EntityStore> store, String statId, String deltaRaw) {
        StatType stat = StatType.fromId(statId);
        if (stat == null) {
            return;
        }
        Float delta = parseDelta(deltaRaw);
        if (delta == null) {
            return;
        }
        DebugStatModifierService modifiers = TalaniaDebug.statModifiers();
        modifiers.addDelta(playerRef.getUuid(), stat, delta);
        applyModifiersToStats(ref, store);
    }

    private void adjustMult(Ref<EntityStore> ref, Store<EntityStore> store, String statId, String deltaRaw) {
        StatType stat = StatType.fromId(statId);
        if (stat == null) {
            return;
        }
        Float delta = parseDelta(deltaRaw);
        if (delta == null) {
            return;
        }
        DebugStatModifierService modifiers = TalaniaDebug.statModifiers();
        modifiers.addMultiplier(playerRef.getUuid(), stat, delta);
        applyModifiersToStats(ref, store);
    }

    private Float parseDelta(String deltaRaw) {
        try {
            return Float.parseFloat(deltaRaw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void applyModifiersToStats(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStats stats = StatsManager.getOrCreate(playerRef.getUuid());
        TalaniaDebug.statModifiers().applyToStats(playerRef.getUuid(), stats);
        TalaniaCoreRuntime core = TalaniaCoreRuntime.get();
        if (core != null) {
            core.statSyncService().applyAll(ref, store, playerRef.getUuid(), stats);
        }
    }

    private List<RowPair> buildRowPairs() {
        List<RowDefinition> rows = buildRows();
        List<RowPair> result = new ArrayList<>();
        int i = 0;
        while (i < rows.size()) {
            RowDefinition row = rows.get(i);
            if (row.type == RowType.HEADER) {
                result.add(RowPair.header(row.header));
                i++;
                continue;
            }
            RowDefinition left = row;
            RowDefinition right = null;
            if (i + 1 < rows.size()) {
                RowDefinition next = rows.get(i + 1);
                if (next.type == RowType.STAT) {
                    right = next;
                    i += 2;
                } else {
                    i++;
                }
            } else {
                i++;
            }
            result.add(RowPair.stats(left, right));
        }
        return result;
    }

    private List<RowDefinition> buildRows() {
        List<RowDefinition> result = new ArrayList<>();
        addSection(result, "Vitals", StatCategory.VITALS);
        addSection(result, "Offense", StatCategory.OFFENSE);
        addSection(result, "Defense", StatCategory.DEFENSE);
        addSection(result, "Resistances", StatCategory.RESISTANCE);
        addSection(result, "Mobility", StatCategory.MOBILITY);
        addSection(result, "Utility", StatCategory.UTILITY);
        return result;
    }

    private void addSection(List<RowDefinition> rows, String title, StatCategory category) {
        rows.add(RowDefinition.header(title));
        for (StatType stat : StatType.values()) {
            if (categoryFor(stat) != category) {
                continue;
            }
            for (ModKind kind : modKindsFor(stat)) {
                rows.add(RowDefinition.stat(stat, kind));
            }
        }
    }

    private String labelFor(StatType stat, ModKind kind) {
        String raw = stat.getId().replace('_', ' ');
        StringBuilder sb = new StringBuilder(raw.length());
        boolean upperNext = true;
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == ' ') {
                sb.append(' ');
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
        if (modKindsFor(stat).size() > 1) {
            return sb + (kind == ModKind.ADD ? " (Add)" : " (Mult)");
        }
        return sb.toString();
    }

    private StatCategory categoryFor(StatType stat) {
        return switch (stat) {
            case HEALTH, MANA, STAMINA -> StatCategory.VITALS;
            case ATTACK, MAGIC_ATTACK, MELEE_DAMAGE_MULT, RANGED_DAMAGE_MULT, MAGIC_DAMAGE_MULT,
                 SPRINT_DAMAGE_MULT, CRIT_CHANCE, CRIT_DAMAGE, ATTACK_SPEED, LIFESTEAL -> StatCategory.OFFENSE;
            case ARMOR, MAGIC_RESIST, DODGE_CHANCE, BLOCKING_EFFICIENCY, FLAT_DAMAGE_REDUCTION,
                 STAMINA_DRAIN_MULT, MELEE_DAMAGE_TAKEN_MULT, RANGED_DAMAGE_TAKEN_MULT,
                 MAGIC_DAMAGE_TAKEN_MULT -> StatCategory.DEFENSE;
            case FALL_RESISTANCE, FIRE_RESISTANCE, POISON_RESISTANCE, LIGHTNING_RESISTANCE,
                 HOLY_RESISTANCE, VOID_RESISTANCE -> StatCategory.RESISTANCE;
            case MOVE_SPEED, JUMP_HEIGHT -> StatCategory.MOBILITY;
            default -> StatCategory.UTILITY;
        };
    }

    private String categoryLabel(StatCategory category) {
        return switch (category) {
            case VITALS -> "Vitals";
            case OFFENSE -> "Offense";
            case DEFENSE -> "Defense";
            case RESISTANCE -> "Resistance";
            case MOBILITY -> "Mobility";
            case UTILITY -> "Utility";
        };
    }

    private List<ModKind> modKindsFor(StatType stat) {
        return switch (stat) {
            case HEALTH, MANA, STAMINA, FLAT_DAMAGE_REDUCTION, CRIT_CHANCE, LIFESTEAL,
                 ARMOR, MAGIC_RESIST, DODGE_CHANCE, FALL_RESISTANCE, FIRE_RESISTANCE,
                 POISON_RESISTANCE, LIGHTNING_RESISTANCE, HOLY_RESISTANCE, VOID_RESISTANCE,
                 MANA_REGEN, STAMINA_REGEN -> List.of(ModKind.ADD);
            default -> List.of(ModKind.MULT);
        };
    }

    private boolean isPercentStat(StatType stat) {
        return switch (stat) {
            case CRIT_CHANCE, LIFESTEAL, ARMOR, MAGIC_RESIST, DODGE_CHANCE, FALL_RESISTANCE,
                 FIRE_RESISTANCE, POISON_RESISTANCE, LIGHTNING_RESISTANCE, HOLY_RESISTANCE,
                 VOID_RESISTANCE -> true;
            default -> false;
        };
    }

    private float stepForAdd(StatType stat) {
        if (isPercentStat(stat)) {
            return 0.05f;
        }
        float max = stat.getMaxValue();
        if (max <= 1.0f) {
            return 0.05f;
        }
        if (max <= 10.0f) {
            return 0.1f;
        }
        if (max <= 100.0f) {
            return 1.0f;
        }
        return 5.0f;
    }

    private float stepForMult(StatType stat) {
        return 0.05f;
    }

    private String format(float value) {
        return FORMAT.format(value);
    }

    private String formatSigned(float value) {
        String formatted = format(value);
        return value >= 0.0f ? "+" + formatted : formatted;
    }

    private enum StatCategory {
        VITALS,
        OFFENSE,
        DEFENSE,
        RESISTANCE,
        MOBILITY,
        UTILITY
    }

    private enum ModKind {
        ADD,
        MULT
    }

    private enum RowType {
        HEADER,
        STAT
    }

    private static final class RowDefinition {
        private final RowType type;
        private final StatType stat;
        private final ModKind modKind;
        private final String header;

        private RowDefinition(RowType type, StatType stat, ModKind modKind, String header) {
            this.type = type;
            this.stat = stat;
            this.modKind = modKind;
            this.header = header;
        }

        public static RowDefinition header(String title) {
            return new RowDefinition(RowType.HEADER, null, null, title);
        }

        public static RowDefinition stat(StatType stat, ModKind kind) {
            return new RowDefinition(RowType.STAT, stat, kind, null);
        }
    }

    private static final class RowPair {
        private final RowType type;
        private final RowDefinition left;
        private final RowDefinition right;
        private final String header;

        private RowPair(RowType type, RowDefinition left, RowDefinition right, String header) {
            this.type = type;
            this.left = left;
            this.right = right;
            this.header = header;
        }

        public static RowPair header(String title) {
            return new RowPair(RowType.HEADER, null, null, title);
        }

        public static RowPair stats(RowDefinition left, RowDefinition right) {
            return new RowPair(RowType.STAT, left, right, null);
        }
    }

    public static final class TalaniaDebugStatModifiersEventData {
        public static final BuilderCodec<TalaniaDebugStatModifiersEventData> CODEC;
        private String action;
        private String value;
        private String delta;

        static {
            BuilderCodec.Builder<TalaniaDebugStatModifiersEventData> builder =
                    BuilderCodec.builder(TalaniaDebugStatModifiersEventData.class,
                            TalaniaDebugStatModifiersEventData::new);
            builder.addField(new KeyedCodec("Action", Codec.STRING),
                    (entry, s) -> entry.action = s,
                    (entry) -> entry.action);
            builder.addField(new KeyedCodec("Value", Codec.STRING),
                    (entry, s) -> entry.value = s,
                    (entry) -> entry.value);
            builder.addField(new KeyedCodec("Delta", Codec.STRING),
                    (entry, s) -> entry.delta = s,
                    (entry) -> entry.delta);
            CODEC = builder.build();
        }
    }
}
