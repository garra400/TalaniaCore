# Text Utilities

## Purpose

Text processing utilities including color code parsing and text formatting.

## Files

- `ColorParser.java` - Parse and process Minecraft-style color codes (&0-&f, &#RRGGBB)

## Usage

```java
import com.talania.core.utils.text.ColorParser;

// Parse text into styled segments
List<TextSegment> segments = ColorParser.parse("&6Gold &cRed &lBold");

// Strip color codes
String plain = ColorParser.strip("&6Colored &cText");
// Result: "Colored Text"

// Convert to ANSI for terminal output
String ansi = ColorParser.toAnsi("&cError!");

// Get color for a code
Color gold = ColorParser.getColor("&6");
```

## Color Codes Reference

| Code | Color |
|------|-------|
| &0 | Black |
| &1 | Dark Blue |
| &2 | Dark Green |
| &3 | Dark Aqua |
| &4 | Dark Red |
| &5 | Dark Purple |
| &6 | Gold |
| &7 | Gray |
| &8 | Dark Gray |
| &9 | Blue |
| &a | Green |
| &b | Aqua |
| &c | Red |
| &d | Light Purple |
| &e | Yellow |
| &f | White |

### Format Codes

| Code | Effect |
|------|--------|
| &l | Bold |
| &o | Italic |
| &m | Monospace |
| &r | Reset |

### Hex Colors

Use `&#RRGGBB` for custom hex colors:

```java
String text = "&#FF5500Orange Text";
```

## Parent Module

This is part of the [utils](../) module.
