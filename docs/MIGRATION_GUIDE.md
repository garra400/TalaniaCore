# Migration Guide - TalaniaCore

> **Current Version:** 0.1.0  
> **Last updated:** February 2026

This guide documents changes between versions and how to migrate your code.

---

## Migration to 0.1.0 (Initial)

### Overview

This is the first public release of TalaniaCore. If you were using internal code from Orbis and Dungeons, follow these changes:

### Package Changes

```java
// Before (internal)
import com.garra400.racas.i18n.TranslationManager;
import com.garra400.racas.stats.StatsManager;

// After (TalaniaCore)
import com.talania.core.localization.TranslationManager;
import com.talania.core.stats.StatsManager;
```

### Stats System

#### Before (Orbis internal)

```java
// Direct modifiers
EntityStatMap stats = EntityStatsModule.get(player);
stats.putModifier(healthIdx, "race_mod", new StaticModifier(...));
```

#### After (TalaniaCore)

```java
// Abstracted API
EntityStats stats = StatsManager.getOrCreate(playerUUID);
stats.addModifier(StatModifier.add("race:orc", StatType.HEALTH, 75));
```

### Translation System

#### Before

```java
TranslationManager.translate("key", args);
```

#### After

```java
// Same method available
TranslationManager.translate("key", args);

// Or the short helper
T.t("key", args);
```

### Event System

#### New

The event system is completely new in TalaniaCore:

```java
// Register
EventBus.subscribe(MyEvent.class, event -> {
    // handler
});

// Publish
EventBus.publish(new MyEvent());
```

---

## Hytale API Compatibility

### DefaultEntityStatTypes

TalaniaCore uses its own `StatType` internally but syncs with Hytale:

| TalaniaCore | Hytale |
|-------------|--------|
| `StatType.HEALTH` | `DefaultEntityStatTypes.getHealth()` |
| `StatType.STAMINA` | `DefaultEntityStatTypes.getStamina()` |
| `StatType.MANA` | `DefaultEntityStatTypes.getMana()` |

### Automatic Synchronization

When using the Orbis and Dungeons integration, stats are synchronized automatically.

For manual synchronization:

```java
HytaleBridge.syncToHytale(playerRef, store, stats);
```

---

## Future Versions

### Planned for 0.2.0

- Skills system
- Cooldown system
- Stats persistence to file

### Planned for 0.3.0

- Temporary buffs/debuffs system
- Inventory integration
- Progression API

---

## Support

If you encounter issues during migration:

1. Check the [API Reference](API_REFERENCE.md)
2. Open a [GitHub Issue](https://github.com/garra400/TalaniaCore/issues)
3. See the [Examples](../examples/)

---

*Last updated: February 2026*
