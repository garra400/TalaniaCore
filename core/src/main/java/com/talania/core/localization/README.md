# Localization System

## Purpose

JSON-based translation system with multiple language support and fallback.

## Files

- `TranslationManager.java` - Central manager
- `LanguageLoader.java` - Loads JSON files
- `T.java` - Static helper for quick translations
- `LocaleConfig.java` - Locale configuration

## Quick Usage

```java
import com.talania.core.localization.T;

// Simple translation
String text = T.t("ui.welcome");

// With parameters
String msg = T.t("combat.damage", 50, "Zombie");

// Change language
T.setLang("pt_br");

// With custom formatter (colors)
T.setFormatter(ColorParser::process);
```

## API Reference

See [API_REFERENCE.md](../../../../../docs/API_REFERENCE.md#localization-system) for complete documentation.
