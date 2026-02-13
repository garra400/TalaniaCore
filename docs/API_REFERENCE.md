# TalaniaCore API Reference

> **Version:** 0.1.0  
> **License:** Public Domain (Unlicense)  
> **Last updated:** February 2026

Complete API reference for the TalaniaCore library for the Orbis and Dungeons ecosystem.

---

## Table of Contents

1. [Stats System](#stats-system)
2. [Progression System](#progression-system)
3. [Input System](#input-system)
4. [Localization System](#localization-system)
5. [Event System](#event-system)
6. [Configuration System](#configuration-system)
7. [Profile System](#profile-system)
8. [Hytale Integration](#hytale-integration)
9. [Utilities](#utilities)
10. [UI Wrapper](#ui-wrapper)

---

## Stats System

The stats module provides a flexible system for managing entity attributes.

### StatType (Enum)

Available stat types organized by category:

#### Vitals

| Stat | ID | Default | Min | Max |
|------|-----|---------|-----|-----|
| `HEALTH` | `health` | 100.0 | 0.0 | 10000.0 |
| `MANA` | `mana` | 100.0 | 0.0 | 10000.0 |
| `STAMINA` | `stamina` | 10.0 | 0.0 | 1000.0 |

#### Offensive

| Stat | ID | Default | Min | Max |
|------|-----|---------|-----|-----|
| `ATTACK` | `attack` | 1.0 | 0.0 | 100.0 |
| `MAGIC_ATTACK` | `magic_attack` | 1.0 | 0.0 | 100.0 |
| `CRIT_CHANCE` | `crit_chance` | 0.05 | 0.0 | 1.0 |
| `CRIT_DAMAGE` | `crit_damage` | 1.5 | 1.0 | 10.0 |
| `ATTACK_SPEED` | `attack_speed` | 1.0 | 0.1 | 10.0 |

#### Defensive

| Stat | ID | Default | Min | Max |
|------|-----|---------|-----|-----|
| `ARMOR` | `armor` | 0.0 | 0.0 | 1.0 |
| `MAGIC_RESIST` | `magic_resist` | 0.0 | 0.0 | 1.0 |
| `FALL_RESISTANCE` | `fall_resistance` | 0.0 | 0.0 | 1.0 |
| `FIRE_RESISTANCE` | `fire_resistance` | 0.0 | 0.0 | 1.0 |

#### Mobility

| Stat | ID | Default | Min | Max |
|------|-----|---------|-----|-----|
| `MOVE_SPEED` | `move_speed` | 1.0 | 0.0 | 10.0 |
| `JUMP_HEIGHT` | `jump_height` | 1.0 | 0.0 | 10.0 |

#### Utility

| Stat | ID | Default | Min | Max |
|------|-----|---------|-----|-----|
| `HEALTH_REGEN` | `health_regen` | 0.0 | 0.0 | 100.0 |
| `MANA_REGEN` | `mana_regen` | 0.0 | 0.0 | 100.0 |
| `STAMINA_REGEN` | `stamina_regen` | 1.0 | 0.0 | 100.0 |
| `XP_BONUS` | `xp_bonus` | 1.0 | 0.0 | 10.0 |
| `LUCK` | `luck` | 1.0 | 0.0 | 10.0 |

### StatsManager

Central stats manager for entities.

```java
import com.talania.core.stats.*;

// Get or create stats for an entity
EntityStats stats = StatsManager.getOrCreate(entityUUID);

// Set base values
stats.setBase(StatType.HEALTH, 100);
stats.setBase(StatType.STAMINA, 10);

// Get calculated value (base + modifiers)
float maxHealth = stats.get(StatType.HEALTH);

// Remove stats from an entity
StatsManager.remove(entityUUID);
```

### StatModifier

Modifier system for dynamically altering stats.

```java
import com.talania.core.stats.StatModifier;

// Additive modifier (+100 HP)
StatModifier orcBonus = StatModifier.add("race:orc", StatType.HEALTH, 100);

// Multiplicative modifier (base × 1.5)
StatModifier strengthBuff = StatModifier.multiplyBase("buff:strength", StatType.ATTACK, 1.5f);

// Percentage modifier (total × 1.2)
StatModifier finalBonus = StatModifier.multiplyTotal("equipment:ring", StatType.LUCK, 1.2f);

// Apply modifiers
stats.addModifier(orcBonus);
stats.addModifier(strengthBuff);

// Remove modifier by ID
stats.removeModifier("race:orc");

// Remove all modifiers from a source
stats.removeModifiersBySource("buff:");
```

### EntityStats

Stats container for a specific entity.

```java
// Base values
stats.setBase(StatType.HEALTH, 150);
float baseHealth = stats.getBase(StatType.HEALTH);

// Calculated values
float finalHealth = stats.get(StatType.HEALTH); // base + modifiers

// Check limits
float clamped = stats.getClamped(StatType.CRIT_CHANCE); // 0.0-1.0

// List active modifiers
List<StatModifier> mods = stats.getModifiers(StatType.HEALTH);

// Reset to default values
stats.reset();
```

---

## Progression System

Leveling and XP management for any progression context (classes, skills, etc.).

### LevelingCurve (Interface)

Defines how much XP is required per level.

```java
public interface LevelingCurve {
    long xpForLevel(int level);  // XP needed to go from level to level+1
    int maxLevel();              // Maximum reachable level
}
```

### LinearLevelingCurve

Linear curve: each level requires `baseXp + (level * stepXp)`.

```java
import com.talania.core.progression.LinearLevelingCurve;

// Level 0->1 needs 100 XP, level 1->2 needs 150 XP, level 2->3 needs 200 XP, etc.
LevelingCurve curve = new LinearLevelingCurve(100, 100L, 50L);
//                                            max  base  step
```

### LevelProgress

Mutable tracker for current level and XP.

```java
import com.talania.core.progression.LevelProgress;

LevelProgress progress = new LevelProgress();      // level=0, xp=0
LevelProgress progress = new LevelProgress(5, 200); // level=5, xp=200

progress.level();        // current level
progress.xp();           // current XP within level
progress.addXp(100);     // add XP manually
progress.setLevel(10);   // set level directly
```

### LevelingService

Applies XP to progress and handles automatic level-ups.

```java
import com.talania.core.progression.*;

LevelingService service = new LevelingService();
LevelingCurve curve = new LinearLevelingCurve(100, 100L, 50L);
LevelProgress progress = new LevelProgress();

// Add 250 XP - will auto-level from 0 to 1 (needs 100) and keep 150 excess
LevelingResult result = service.addXp(progress, 250L, curve);
```

### LevelingResult

Immutable result of an XP operation.

```java
result.oldLevel();      // level before XP was added
result.newLevel();      // level after XP was added
result.levelsGained();  // how many levels were gained
result.xpAdded();       // how much XP was added
result.leveledUp();     // true if at least one level was gained
```

---

## Input System

Input action events and pattern detection.

### InputAction (Enum)

Discrete action keys mapped to abilities.

| Value | Description |
|-------|-------------|
| `E` | Primary action key |
| `R` | Secondary action key |

### InputActionEvent

Event published when an action key is pressed. Subscribe via `EventBus`.

```java
import com.talania.core.input.*;
import com.talania.core.events.EventBus;

EventBus.subscribe(InputActionEvent.class, event -> {
    UUID playerId = event.playerId();       // who pressed the key
    InputAction action = event.action();    // E or R
    long timestamp = event.timestamp();     // when it was pressed
    Ref<EntityStore> ref = event.ref();     // entity reference (nullable)
    Store<EntityStore> store = event.store(); // entity store (nullable)
});
```

### InputPattern (Enum)

High-level movement/input patterns detected by `InputPatternTracker`.

| Pattern | Description |
|---------|-------------|
| `DOUBLE_TAP_CROUCH` | Crouched twice quickly |
| `DOUBLE_TAP_SPRINT` | Sprinted twice quickly |
| `DOUBLE_TAP_RIGHT_CLICK` | Right-clicked twice quickly |
| `JUMP_LOOK_UP` | Jumped while looking up |
| `CROUCH_JUMP` | Crouched then jumped |
| `RIGHT_CLICK_CROUCH` | Right-clicked while crouching |
| `PLACE_BLOCK_CROUCH` | Placed block while crouching |

---

## Localization System

JSON-based translation system with automatic fallback.

### TranslationManager

Central translation manager.

```java
import com.talania.core.localization.TranslationManager;

// Initialize (once at startup)
TranslationManager.initialize(modsDirectory);

// Translate
String text = TranslationManager.translate("ui.welcome");

// Translate with parameters
String msg = TranslationManager.translate("combat.damage", 50, "Zombie");
// "You dealt 50 damage to Zombie"

// Change language
TranslationManager.setLanguage("pt_br");

// Check current language
String lang = TranslationManager.getCurrentLanguage(); // "pt_br"

// Check if language exists
boolean exists = TranslationManager.isLanguageAvailable("es");

// List available languages
Map<String, String> langs = TranslationManager.getAvailableLanguages();
// {"en": "English", "pt_br": "Português (Brasil)", ...}
```

### T (Shorthand Helper)

Static helper for quick translations.

```java
import com.talania.core.localization.T;

// Simple translation
String text = T.t("ui.welcome");

// With parameters
String msg = T.t("player.level_up", 10);

// Without formatting (raw)
String raw = T.raw("tooltip.description");

// Check existence
if (T.has("custom.key")) {
    // key exists
}

// Get current language
String lang = T.lang();

// Change language
T.setLang("es");
```

### Custom Formatters

```java
import com.talania.core.localization.T;
import com.talania.core.utils.text.ColorParser;

// Automatically apply colors
T.setFormatter(ColorParser::process);

// Now all translations have colors processed
String colored = T.t("ui.title"); // "&6Title" → colors applied

// Reset formatter
T.resetFormatter();
```

### Language File Structure

```
resources/languages/
├── en.json
├── pt_br.json
├── es.json
└── ru.json
```

**JSON Format:**

```json
{
  "language.name": "English",
  "language.code": "en",
  
  "ui.welcome": "Welcome to the game!",
  "ui.confirm": "Confirm",
  "ui.cancel": "Cancel",
  
  "combat.damage": "You dealt {0} damage to {1}",
  "player.level_up": "You reached level {0}!"
}
```

---

## Event System

Event bus with priorities and cancellation support.

### EventBus

```java
import com.talania.core.events.EventBus;

// Register listener
EventBus.subscribe(PlayerDamageEvent.class, event -> {
    if (event.getAmount() > 100) {
        event.setCancelled(true); // Block excessive damage
    }
});

// Register with priority
EventBus.subscribe(PlayerDamageEvent.class, EventPriority.HIGH, event -> {
    // Processes before normal listeners
});

// Publish event
PlayerDamageEvent event = new PlayerDamageEvent(player, 50);
EventBus.publish(event);

// Check if cancelled
if (!event.isCancelled()) {
    // Apply damage
}
```

### Event Priorities

| Priority | Order | Use |
|----------|-------|-----|
| `LOWEST` | 1st | Initial monitoring |
| `LOW` | 2nd | Primary processing |
| `NORMAL` | 3rd | Default |
| `HIGH` | 4th | Important modifications |
| `HIGHEST` | 5th | Final decisions |

### Creating Custom Events

```java
import com.talania.core.events.Event;
import com.talania.core.events.Cancellable;

public class MyCustomEvent extends Event implements Cancellable {
    private final Player player;
    private boolean cancelled = false;
    
    public MyCustomEvent(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() { return player; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    
    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}
```

---

## Configuration System

JSON loading with cache and hot-reload.

### ConfigManager

```java
import com.talania.core.config.ConfigManager;

// Initialize
ConfigManager.initialize(modsDirectory);

// Load config
MyConfig config = ConfigManager.load("my_config.json", MyConfig.class);

// Save changes
config.setSomeSetting(true);
ConfigManager.save("my_config.json", config);

// Reload from disk
config = ConfigManager.reload("my_config.json", MyConfig.class);

// Check if exists
boolean exists = ConfigManager.exists("my_config.json");
```

### Configuration Class

```java
public class MyConfig {
    private boolean enabled = true;
    private int maxPlayers = 100;
    private List<String> allowedRaces = Arrays.asList("elf", "orc", "human");
    
    // Getters and Setters...
}
```

**Generated JSON:**

```json
{
  "enabled": true,
  "maxPlayers": 100,
  "allowedRaces": ["elf", "orc", "human"]
}
```

---

## Utilities

### ColorParser

Processes color codes in Minecraft/Hytale style.

```java
import com.talania.core.utils.text.ColorParser;

// Parse colors
List<TextSegment> segments = ColorParser.parse("&6Gold &cRed &lBold");

// Remove colors (plain text)
String plain = ColorParser.strip("&6Colored Text"); // "Colored Text"

// Convert to ANSI (terminal)
String ansi = ColorParser.toAnsi("&cError!");

// Convert to Hytale format
String hytale = ColorParser.toHytale("&aSuccess!");
```

**Color Codes:**

| Code | Color |
|------|-------|
| `&0` | Black |
| `&1` | Dark Blue |
| `&2` | Dark Green |
| `&3` | Dark Cyan |
| `&4` | Dark Red |
| `&5` | Purple |
| `&6` | Gold |
| `&7` | Gray |
| `&8` | Dark Gray |
| `&9` | Blue |
| `&a` | Green |
| `&b` | Cyan |
| `&c` | Red |
| `&d` | Pink |
| `&e` | Yellow |
| `&f` | White |

**Formatting:**

| Code | Effect |
|------|--------|
| `&l` | Bold |
| `&o` | Italic |
| `&n` | Underline |
| `&m` | Strikethrough |
| `&r` | Reset |

### ModelModifier

Entity model modification.

```java
import com.talania.core.utils.model.ModelModifier;

// Attach model (e.g., elf ears)
ModelModifier.Attachment ears = ModelModifier.attach(
    playerId, 
    "head",           // target bone
    "elf_ears",       // model to attach
    AttachOptions.defaults().offset(0, 0.1f, 0)
);

// Remove attachment
ears.detach();

// Scale model part
ModelModifier.scale(playerId, "body", 1.2f);

// Reset model
ModelModifier.reset(playerId);
```

---

## UI Wrapper

Fluent abstraction for interface creation.

### UIFactory

```java
import com.talania.core.ui.UIFactory;

// Create button
UIComponent button = UIFactory.button()
    .text(T.t("ui.confirm"))
    .position(100, 50)
    .size(120, 40)
    .onClick(() -> System.out.println("Clicked!"))
    .build();

// Create panel
UIComponent panel = UIFactory.panel()
    .position(0, 0)
    .size(300, 200)
    .backgroundColor("#1a1a1a")
    .add(button)
    .build();

// Create text
UIComponent label = UIFactory.text()
    .content(T.t("ui.welcome"))
    .position(50, 20)
    .fontSize(16)
    .color("#ffffff")
    .build();
```

---

## Profile System

Player profile persistence with class progress tracking.

### TalaniaPlayerProfile

```java
import com.talania.core.profile.TalaniaPlayerProfile;
import com.talania.core.progression.LevelProgress;

TalaniaPlayerProfile profile = new TalaniaPlayerProfile(playerUUID);

// Race
profile.setRaceId("elf");
String race = profile.raceId(); // "elf"

// Class (for ClassRPG integration)
profile.setClassId("warrior");
String classId = profile.classId(); // "warrior"

// Base stats
profile.setBaseStat(StatType.HEALTH, 150f);
float hp = profile.getBaseStat(StatType.HEALTH, 100f); // 150

// Class progression
LevelProgress warrior = profile.getOrCreateClassProgress("warrior");
// Returns existing or creates new LevelProgress(0, 0)

// Access all class progress
Map<String, LevelProgress> all = profile.classProgress();
```

### TalaniaProfileRuntime

```java
import com.talania.core.profile.TalaniaProfileRuntime;

TalaniaProfileRuntime runtime = new TalaniaProfileRuntime(dataDirectory);

// Load a player profile (creates if not exists)
TalaniaPlayerProfile profile = runtime.load(playerUUID);

// Save and unload
runtime.unload(playerUUID, true); // true = save before unload
```

### TalaniaCoreRuntime

Singleton that wires all core systems together.

```java
import com.talania.core.runtime.TalaniaCoreRuntime;

// Initialize once at plugin start
TalaniaCoreRuntime runtime = TalaniaCoreRuntime.init(dataDirectory);

// Access from anywhere
TalaniaCoreRuntime rt = TalaniaCoreRuntime.get();
rt.profileRuntime();        // profile management
rt.statSyncService();       // Hytale stat sync
rt.inputPatternTracker();   // input pattern detection
```

---

## Hytale Integration

### EntityEffectService

Apply Hytale entity effects by string ID.

```java
import com.talania.core.hytale.effects.EntityEffectService;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;

// Apply a speed potion effect for 5 seconds
EntityEffectService.apply(targetRef, store, "Potion_Speed", 5000L, OverlapBehavior.OVERWRITE);

// Apply a custom effect
EntityEffectService.apply(targetRef, store, "EntityEffect/MyCustomEffect", 3000L, OverlapBehavior.OVERWRITE);
```

The service automatically resolves both direct IDs and `EntityEffect/` prefixed IDs.

### Stats Mapping

TalaniaCore maps its stats to the native Hytale system:

| TalaniaCore | Hytale |
|-------------|--------|
| `StatType.HEALTH` | `DefaultEntityStatTypes.getHealth()` |
| `StatType.STAMINA` | `DefaultEntityStatTypes.getStamina()` |
| `StatType.MANA` | `DefaultEntityStatTypes.getMana()` |

### Synchronization

```java
import com.talania.core.stats.HytaleBridge;

// Sync TalaniaCore stats to Hytale
HytaleBridge.syncToHytale(playerRef, store, stats);

// Sync from Hytale to TalaniaCore
HytaleBridge.syncFromHytale(playerRef, store, stats);
```

---

## Dependencies

TalaniaCore has no required external dependencies. Works standalone or integrated with:

- **Orbis and Dungeons** - Race and class mod
- **Hytale Server API** - For native integration

---

## Links

- [Main README](../README.md)
- [Getting Started](GETTING_STARTED.md)
- [Migration Guide](MIGRATION_GUIDE.md)
- [Contributing](../CONTRIBUTING.md)

---

*Documentation generated February 2026*
