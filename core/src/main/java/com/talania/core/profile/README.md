# Player Profiles

## Purpose

Persistent storage for player-specific Talania data (race, base stats, etc.).

## Files

- `TalaniaPlayerProfile.java`
- `TalaniaProfileStore.java`
- `TalaniaProfileRuntime.java`
- `api/` (Talania profile API types)

## Usage

```java
TalaniaProfileStore store = new TalaniaProfileStore(dataDirectory);
TalaniaPlayerProfile profile = store.loadProfile(playerUuid);
profile.setRaceId("human");
store.saveProfile(profile);
```

## API Reference

See the main [API Reference](../../docs/API_REFERENCE.md) for detailed documentation.

## Integration Notes

- Profiles are JSON files under the Talania data directory (see `TalaniaProfileStore`).
- Base stats are stored as floats and should be mapped to Talania stat values.
- Race IDs are stored as string identifiers (e.g., "human").
