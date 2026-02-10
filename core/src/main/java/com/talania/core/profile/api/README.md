# Profile API

## Purpose

API types for interacting with Talania player data.

## Files

- `TalaniaApi.java`
- `TalaniaApiProvider.java`
- `TalaniaPlayerInfo.java`
- `TalaniaProfileApi.java`
- `TalaniaProfileApiProvider.java`
- `TalaniaProfileInfo.java`

## Usage

```java
TalaniaApi api = TalaniaApiProvider.get();
if (api != null) {
    TalaniaPlayerInfo info = api.getPlayerInfo(playerUuid);
}
```

## API Reference

See the main [API Reference](../../docs/API_REFERENCE.md) for detailed documentation.
