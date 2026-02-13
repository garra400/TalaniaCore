# Core Stats System

## Purpose

Base system for modifying health (HP), mana, and global entity attributes.

## Files

- `StatsManager.java` - Central manager
- `StatType.java` - Enum with 17+ stat types
- `StatModifier.java` - Modifiers (additive, multiplicative)
- `EntityStats.java` - Stats container per entity
- `DamageType.java` - Damage type definitions

## Quick Usage

```java
import com.talania.core.stats.*;

// Get stats for an entity
EntityStats stats = StatsManager.getOrCreate(entityUUID);

// Set base value
stats.setBase(StatType.HEALTH, 100);

// Add modifier
stats.addModifier(StatModifier.add("race:orc", StatType.HEALTH, 75));

// Get final value
float maxHP = stats.get(StatType.HEALTH); // 175
```

## API Reference

See [API_REFERENCE.md](../../../../../docs/API_REFERENCE.md#stats-system) for complete documentation.
