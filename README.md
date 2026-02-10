# TalaniaCore

<p align="center">
  <strong>Shared Library for Orbis and Dungeons Ecosystem</strong>
</p>

<p align="center">
  <a href="#modules">Modules</a> •
  <a href="#installation">Installation</a> •
  <a href="#usage">Usage</a> •
  <a href="#contributing">Contributing</a>
</p>

---

## Overview

TalaniaCore is a **public domain shared library** designed for the Hytale modding ecosystem. It provides reusable utilities and systems that serve as the foundation for the Talania server mods. The repo is now a Gradle multi-project with a shared `core` module and optional gameplay modules.

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| **core** | Shared library with stats, combat, events, input, movement, projectiles, entity utilities, profiles | ✅ Active |
| **races** | Race definitions and modifiers (based on design docs) | ✅ Active |
| **boss-fights** | Placeholder module for future boss systems | 🚧 Planned |

### Core Submodules

| Submodule | Description | Status |
|-----------|-------------|--------|
| **stats** | Base stats + modifiers | ✅ Active |
| **combat** | Damage modifiers, attack/damage typing, settings | 🧪 In Progress |
| **events** | Event bus + entity events | ✅ Active |
| **input** | Input pattern helpers | 🧪 In Progress |
| **movement** | Movement utilities (jump, flight) | 🧪 In Progress |
| **projectiles** | Projectile helpers + detection systems | 🧪 In Progress |
| **entities** | Temporary entity effects + animation manager | 🧪 In Progress |
| **profile** | Player profile storage + API | 🧪 In Progress |
| **hytale** | Hytale API bridges (stats sync, teleport, etc.) | 🧪 In Progress |
| **localization** | Translation system | ✅ Active |
| **config** | JSON config + hot-reload | ✅ Active |
| **ui** | UI wrapper abstractions | ✅ Active |
| **utils** | Animation/model/text helpers | ✅ Active |

## Installation

### Gradle (Recommended)

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.garra400:TalaniaCore:VERSION'
}
```

### Manual

1. Download the latest release from [Releases](https://github.com/garra400/TalaniaCore/releases)
2. Add the JAR to your mod's `libs/` folder
3. Include it in your `build.gradle`

## Usage

### Core Stats System

```java
import com.talania.core.stats.*;

// Get or create stats for an entity
EntityStats stats = StatsManager.getOrCreate(entityUUID);

// Set base values
stats.setBase(StatType.HEALTH, 100);
stats.setBase(StatType.STAMINA, 10);

// Add modifiers (race bonuses, buffs, etc.)
stats.addModifier(StatModifier.add("race:orc", StatType.HEALTH, 100));
stats.addModifier(StatModifier.multiplyBase("buff:strength", StatType.ATTACK, 1.5f));

// Get final calculated value
float maxHealth = stats.get(StatType.HEALTH); // 200 (100 base + 100 from orc)
```

### Localization System

```java
import com.talania.core.localization.T;
import com.talania.core.localization.TranslationManager;

// Initialize once at startup
TranslationManager.initialize(modsDirectory);

// Simple translation
String text = T.t("ui.welcome");

// With parameters
String formatted = T.t("combat.damage_dealt", 50, "Zombie");

// Change language
T.setLang("pt_br");

// Set custom formatter (e.g., for color codes)
T.setFormatter(ColorParser::strip);
```

### Event System

```java
import com.talania.core.events.EventBus;

// Subscribe to events
EventBus.subscribe(PlayerDamageEvent.class, event -> {
    if (event.getAmount() > 100) {
        event.setCancelled(true); // Block excessive damage
    }
});

// Publish events
PlayerDamageEvent event = new PlayerDamageEvent(player, 50);
EventBus.publish(event);
```

### Config System

```java
import com.talania.core.config.ConfigManager;

// Initialize
ConfigManager.initialize(modsDirectory);

// Load a config class
MyConfig config = ConfigManager.load("my_config.json", MyConfig.class);

// Save changes
ConfigManager.save("my_config.json", config);
```

### UI Wrapper

```java
import com.talania.core.ui.UIFactory;

// Create a button with the fluent API
UIComponent button = UIFactory.button()
    .text(T.t("ui.confirm"))
    .position(100, 50)
    .size(120, 40)
    .onClick(() -> System.out.println("Clicked!"))
    .build();
```

### Model Modification (e.g., Elf Ears)

```java
import com.talania.core.utils.model.ModelModifier;

// Attach elf ears to player head
ModelModifier.Attachment ears = ModelModifier.attach(
    playerId, 
    "head", 
    "elf_ears",
    AttachOptions.defaults().offset(0, 0.1f, 0)
);

// Later: remove ears
ears.detach();
```

### Color Parsing

```java
import com.talania.core.utils.text.ColorParser;

// Parse color codes
List<TextSegment> segments = ColorParser.parse("&6Gold &cRed &lBold");

// Strip colors for plain text
String plain = ColorParser.strip("&6Colored"); // "Colored"

// Convert to ANSI for terminal
String ansi = ColorParser.toAnsi("&cError!");
```

## Project Structure

```
TalaniaCore/
├── core/                       # Shared library module
│   ├── src/main/java/com/talania/core/
│   │   ├── stats/              # Core stats system
│   │   ├── combat/             # Damage modifiers and combat helpers
│   │   ├── events/             # Event bus + entity events
│   │   ├── input/              # Input pattern helpers
│   │   ├── movement/           # Movement utilities
│   │   ├── projectiles/        # Projectile helpers
│   │   ├── entities/           # Temporary entity effects
│   │   ├── profile/            # Player profile storage + API
│   │   ├── localization/       # Translation system
│   │   ├── utils/              # Technical utilities
│   │   ├── ui/                 # UI abstraction layer
│   │   └── config/             # Configuration system
│   ├── src/main/resources/
│   │   ├── languages/          # Default translations
│   │   └── schemas/            # JSON validation schemas
│   └── tests/                  # Test suite
├── races/                      # Races module
├── boss-fights/                # Boss fights module
├── docs/                       # Documentation
└── examples/                   # Usage examples
```

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. Clone the repository
2. Run `./gradlew build` to compile
3. Run `./gradlew test` to execute tests

### Code Style

- Follow Java naming conventions
- Include Javadoc for public APIs
- Write tests for new functionality

## License

This project is released into the **Public Domain** under the [Unlicense](LICENSE).

---

<p align="center">
  Part of the <strong>Orbis and Dungeons</strong> ecosystem
</p>
