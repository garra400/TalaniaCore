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

TalaniaCore is a **public domain shared library** designed for the Hytale modding ecosystem. It provides a collection of reusable utilities and systems that serve as the foundation for the "Orbis and Dungeons" mod series.

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| **Core Stats** | HP, mana, and global attribute modification system | 🚧 In Progress |
| **Localization** | JSON-based translation system with fallback support | ✅ Ready |
| **Technical Utilities** | Animation, input, and model modification helpers | 🚧 In Progress |
| **UI Wrapper** | Abstraction layer for UI libraries (Simple UI, HyUI) | 📋 Planned |
| **Config System** | Centralized configuration with hot-reload | 📋 Planned |
| **Event System** | Inter-module event communication | 📋 Planned |

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
import com.talania.core.stats.StatsManager;
import com.talania.core.stats.StatType;

// Get entity stats
EntityStats stats = StatsManager.getStats(entity);

// Modify HP
stats.setMaxHealth(100);
stats.modifyAttribute(StatType.HEALTH, 1.5f, ModifierType.MULTIPLY);
```

### Localization System

```java
import com.talania.core.localization.T;

// Simple translation
String text = T.get("ui.welcome_message");

// With parameters
String formatted = T.get("combat.damage_dealt", damage, targetName);

// Change language
T.setLocale("pt_br");
```

### UI Wrapper

```java
import com.talania.core.ui.UIFactory;
import com.talania.core.ui.ComponentBuilder;

// Create a button with the abstraction layer
UIComponent button = UIFactory.button()
    .text(T.get("ui.confirm"))
    .onClick(this::handleConfirm)
    .build();
```

## Project Structure

```
TalaniaCore/
├── src/main/java/com/talania/core/
│   ├── stats/          # Core stats system
│   ├── localization/   # Translation system
│   ├── utils/          # Technical utilities
│   │   ├── animation/  # Animation helpers
│   │   ├── input/      # Input management
│   │   └── model/      # Model modification
│   ├── ui/             # UI abstraction layer
│   ├── config/         # Configuration system
│   └── events/         # Event bus
├── src/main/resources/
│   ├── languages/      # Default translations
│   └── schemas/        # JSON validation schemas
├── docs/               # Documentation
├── examples/           # Usage examples
└── tests/              # Test suite
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
