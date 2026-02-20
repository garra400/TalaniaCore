package com.talania.races;

import com.talania.core.stats.StatModifier;
import com.talania.core.stats.StatType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Canonical race definitions based on `.local/game-design/racestalania.docx`.
 *
 * <p>Each race provides a set of always-on stat modifiers and a list of
 * conditional effects that must be implemented by gameplay systems.</p>
 */
public enum RaceType {
    HUMAN(
            "human",
            "Humans",
            "Balance and Will",
            modifiersForHuman(),
            List.of("Magic susceptibility: take 5% more damage from magic attacks.")),
    HIGH_ELF(
            "high_elf",
            "High Elves",
            "Magic and Hubris",
            modifiersForHighElf(),
            Collections.emptyList()),
    ORC(
            "orc",
            "Orcs",
            "Strength and Honor",
            modifiersForOrc(),
            List.of("Magic usage slowdown (casting speed or costs not implemented).")),
    DWARF(
            "dwarf",
            "Dwarves",
            "Stone and Metal",
            modifiersForDwarf(),
            List.of("Heavy steps: -10% movement speed (swim speed not implemented).")),
    NIGHTWALKER(
            "nightwalker",
            "Nightwalkers",
            "Shadow and Curse",
            Collections.emptyList(),
            List.of(
                    "Shadow Meld: +15% move speed at night/in darkness.",
                    "Sun Curse: -10% HP regen while in sunlight."
            )),
    BEASTKIN(
            "beastkin",
            "Beastkin",
            "Nature and Instinct",
            modifiersForBeastkin(),
            List.of(
                    "Predator's Agility: jump height increased by 20%.",
                    "Primitive Technology: accuracy penalty with firearms/wands."
            )),
    STARBORN(
            "starborn",
            "Starborn",
            "Cosmos and Mystery",
            modifiersForStarborn(),
            List.of(
                    "Energy Shield: regenerating shield when out of combat.",
                    "Healing Difficulty: potions/food 50% less effective."
            ));

    private final String id;
    private final String displayName;
    private final String theme;
    private final List<ModifierSpec> baseModifiers;
    private final List<String> conditionalNotes;
    private static final java.util.Map<String, RaceType> BY_ID = buildIdMap();

    RaceType(String id, String displayName, String theme,
             List<ModifierSpec> baseModifiers, List<String> conditionalNotes) {
        this.id = id;
        this.displayName = displayName;
        this.theme = theme;
        this.baseModifiers = baseModifiers;
        this.conditionalNotes = conditionalNotes;
    }

    public String id() {
        return id;
    }

    /**
     * Resolve a race by its string ID.
     */
    public static RaceType fromId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return BY_ID.get(id.trim().toLowerCase(java.util.Locale.ROOT));
    }

    /**
     * Human-readable display name for UI and logs.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Short thematic label (from design docs).
     */
    public String theme() {
        return theme;
    }

    /**
     * Returns fresh modifiers for this race (modifiers have unique IDs).
     */
    public List<StatModifier> createBaseModifiers() {
        List<StatModifier> refreshed = new ArrayList<>(baseModifiers.size());
        for (ModifierSpec spec : baseModifiers) {
            refreshed.add(new StatModifier(spec.source, spec.type, spec.value, spec.operation));
        }
        return refreshed;
    }

    /**
     * Notes for conditional effects that require dedicated systems to implement.
     */
    public List<String> conditionalNotes() {
        return conditionalNotes;
    }

    /**
     * Modifier source key used for add/remove operations.
     */
    public String sourceKey() {
        return "race:" + id;
    }

    /**
     * Base modifiers for the Human race.
     */
    private static List<ModifierSpec> modifiersForHuman() {
        String source = "race:human";
        List<ModifierSpec> mods = new ArrayList<>();
        mods.add(new ModifierSpec(source, StatType.XP_BONUS, 1.10f, StatModifier.Operation.MULTIPLY_TOTAL));
        mods.add(new ModifierSpec(source, StatType.MAGIC_DAMAGE_TAKEN_MULT, 1.05f, StatModifier.Operation.MULTIPLY_TOTAL));
        return mods;
    }

    /**
     * Base modifiers for the High Elf race.
     */
    private static List<ModifierSpec> modifiersForHighElf() {
        String source = "race:high_elf";
        List<ModifierSpec> mods = new ArrayList<>();
        mods.add(new ModifierSpec(source, StatType.MANA, 1.20f, StatModifier.Operation.MULTIPLY_TOTAL));
        mods.add(new ModifierSpec(source, StatType.HEALTH, 0.90f, StatModifier.Operation.MULTIPLY_TOTAL));
        return mods;
    }

    /**
     * Base modifiers for the Orc race.
     */
    private static List<ModifierSpec> modifiersForOrc() {
        String source = "race:orc";
        List<ModifierSpec> mods = new ArrayList<>();
        mods.add(new ModifierSpec(source, StatType.MELEE_DAMAGE_MULT, 1.15f, StatModifier.Operation.MULTIPLY_TOTAL));
        mods.add(new ModifierSpec(source, StatType.MANA_REGEN, 0.80f, StatModifier.Operation.MULTIPLY_TOTAL));
        return mods;
    }

    /**
     * Base modifiers for the Dwarf race.
     */
    private static List<ModifierSpec> modifiersForDwarf() {
        String source = "race:dwarf";
        List<ModifierSpec> mods = new ArrayList<>();
        mods.add(new ModifierSpec(source, StatType.ARMOR, 0.10f, StatModifier.Operation.ADD));
        mods.add(new ModifierSpec(source, StatType.POISON_RESISTANCE, 0.10f, StatModifier.Operation.ADD));
        mods.add(new ModifierSpec(source, StatType.MOVE_SPEED, 0.90f, StatModifier.Operation.MULTIPLY_TOTAL));
        return mods;
    }

    /**
     * Base modifiers for the Beastkin race.
     */
    private static List<ModifierSpec> modifiersForBeastkin() {
        String source = "race:beastkin";
        List<ModifierSpec> mods = new ArrayList<>();
        mods.add(new ModifierSpec(source, StatType.FALL_RESISTANCE, 0.50f, StatModifier.Operation.ADD));
        mods.add(new ModifierSpec(source, StatType.JUMP_HEIGHT, 1.20f, StatModifier.Operation.MULTIPLY_TOTAL));
        return mods;
    }

    /**
     * Base modifiers for the Starborn race.
     */
    private static List<ModifierSpec> modifiersForStarborn() {
        String source = "race:starborn";
        List<ModifierSpec> mods = new ArrayList<>();
        mods.add(new ModifierSpec(source, StatType.ENERGY_SHIELD_MAX, 10.0f, StatModifier.Operation.ADD));
        mods.add(new ModifierSpec(source, StatType.ENERGY_SHIELD_RECHARGE, 2.0f, StatModifier.Operation.ADD));
        mods.add(new ModifierSpec(source, StatType.ENERGY_SHIELD_RECHARGE_DELAY, 5.0f, StatModifier.Operation.ADD));
        return mods;
    }

    private static java.util.Map<String, RaceType> buildIdMap() {
        java.util.Map<String, RaceType> map = new java.util.HashMap<>();
        for (RaceType type : values()) {
            map.put(type.id, type);
        }
        return map;
    }

    /**
     * Lightweight definition used to build fresh StatModifiers per player.
     */
    private static final class ModifierSpec {
        private final String source;
        private final StatType type;
        private final float value;
        private final StatModifier.Operation operation;

        private ModifierSpec(String source, StatType type, float value, StatModifier.Operation operation) {
            this.source = source;
            this.type = type;
            this.value = value;
            this.operation = operation;
        }
    }
}
