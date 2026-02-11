# TalaniaCore - Documentation

> **Version:** 0.1.0  
> **License:** Public Domain (Unlicense)

Welcome to the TalaniaCore documentation, the shared library for the Orbis and Dungeons ecosystem.

---

## Document Index

| Document | Description |
|----------|-------------|
| [GETTING_STARTED.md](GETTING_STARTED.md) | Quick start guide |
| [API_REFERENCE.md](API_REFERENCE.md) | Complete API reference |
| [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) | Version migration guide |

---

## Modules

### Core Stats
Attribute system with 17+ stat types and modifiers.

```java
EntityStats stats = StatsManager.getOrCreate(uuid);
stats.addModifier(StatModifier.add("buff", StatType.HEALTH, 50));
```

### Localization
Translation system with fallback and hot-reload.

```java
String text = T.t("ui.welcome");
T.setLang("pt_br");
```

### Events
Event bus with priorities and cancellation.

```java
EventBus.subscribe(MyEvent.class, e -> handle(e));
```

### Config
JSON loader with cache.

```java
MyConfig cfg = ConfigManager.load("config.json", MyConfig.class);
```

### UI Wrapper
Fluent API for interfaces.

```java
UIFactory.button().text("OK").onClick(() -> {}).build();
```

---

## Links

- [Main README](../README.md)
- [Contributing](../CONTRIBUTING.md)
- [Examples](../examples/)
- [HytaleModding.dev](https://hytalemodding.dev)

---

*Documentation generated February 2026*
