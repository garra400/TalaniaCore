# Getting Started with TalaniaCore

> **Version:** 0.1.0  
> **Estimated time:** 10 minutes

This guide shows how to integrate TalaniaCore into your Hytale mod.

---

## Prerequisites

- Java 21+
- Gradle 8.0+
- Hytale Server API

---

## Installation

### Via Gradle (Recommended)

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.garra400:TalaniaCore:0.1.0'
}
```

### Via Manual JAR

1. Download the JAR from [Releases](https://github.com/garra400/TalaniaCore/releases)
2. Copy to your project's `libs/` folder
3. Add to `build.gradle`:

```gradle
dependencies {
    implementation files('libs/TalaniaCore-0.1.0.jar')
}
```

---

## Initial Setup

### 1. Initialize in Plugin

```java
import com.talania.core.localization.TranslationManager;
import com.talania.core.config.ConfigManager;

public class MyPlugin extends JavaPlugin {
    
    @Override
    protected void start() {
        // Initialize systems
        TranslationManager.initialize(getDataDirectory());
        ConfigManager.initialize(getDataDirectory());
    }
}
```

### 2. Create Language Files

Create the `languages/` folder in the mod's data directory:

```
mods/MyMod/
└── languages/
    ├── en.json
    └── pt_br.json
```

**en.json:**
```json
{
  "language.name": "English",
  "ui.welcome": "Welcome!",
  "ui.confirm": "Confirm"
}
```

---

## Basic Usage

### Translations

```java
import com.talania.core.localization.T;

// Simple translation
String text = T.t("ui.welcome"); // "Welcome!"

// With parameters
String msg = T.t("player.damage", 50, "Zombie");
```

### Entity Stats

```java
import com.talania.core.stats.*;

// Get stats
EntityStats stats = StatsManager.getOrCreate(playerUUID);

// Add bonus
stats.addModifier(StatModifier.add("race:orc", StatType.HEALTH, 100));

// Read final value
float maxHP = stats.get(StatType.HEALTH); // 200 (100 base + 100 bonus)
```

### Events

```java
import com.talania.core.events.EventBus;

// Listen to event
EventBus.subscribe(MyEvent.class, event -> {
    // Handle
});

// Publish event
EventBus.publish(new MyEvent());
```

---

## Project Structure

```
your-mod/
├── build.gradle
└── src/main/
    ├── java/
    │   └── com/example/
    │       └── MyPlugin.java
    └── resources/
        └── languages/
            ├── en.json
            └── pt_br.json
```

---

## Next Steps

- [API Reference](API_REFERENCE.md) - Complete API documentation
- [Migration Guide](MIGRATION_GUIDE.md) - Migrate from previous versions
- [Examples](../examples/) - Sample code

---

## Support

- [GitHub Issues](https://github.com/garra400/TalaniaCore/issues)
- [HytaleModding.dev Documentation](https://hytalemodding.dev)

---

*Last updated: February 2026*
