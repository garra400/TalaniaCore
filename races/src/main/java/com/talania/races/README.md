# Races

## Purpose

Race definitions and helper utilities for applying race stat modifiers.

## Files

- `RaceType.java`
- `RaceService.java`

## Usage

```java
RaceService races = new RaceService();

// Assign a race to a player
races.setRace(playerUuid, RaceType.HUMAN);

// Read current race
RaceType race = races.getRace(playerUuid);
```

## API Reference

See the main [API Reference](../../docs/API_REFERENCE.md) for detailed documentation.
