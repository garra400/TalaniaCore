package com.talania.core.utils.text;

import java.awt.Color;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing and processing color codes in text.
 * 
 * <p>Supports Minecraft-style color codes ({@code &0-&f}) and hex colors ({@code &#RRGGBB}).
 * Can be extended to work with any message system.
 * 
 * <p>Color Codes:
 * <ul>
 *   <li>{@code &0-&9, &a-&f} - Standard colors</li>
 *   <li>{@code &#RRGGBB} - Hex colors</li>
 *   <li>{@code &l} - Bold</li>
 *   <li>{@code &o} - Italic</li>
 *   <li>{@code &m} - Monospace</li>
 *   <li>{@code &r} - Reset</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * // Parse color codes to segments
 * List<TextSegment> segments = ColorParser.parse("&6Gold &cRed &lBold");
 * 
 * // Strip color codes
 * String plain = ColorParser.strip("&6Colored &cText");
 * // Result: "Colored Text"
 * 
 * // Convert codes (e.g., for ANSI terminals)
 * String ansi = ColorParser.toAnsi("&cError!");
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class ColorParser {

    private static final Pattern CODE_PATTERN =
            Pattern.compile("&(#?[0-9a-fA-F]{6}|[0-9a-fA-FlOoMmRr])");

    private static final Map<String, Color> COLORS = Map.ofEntries(
            Map.entry("&0", new Color(0, 0, 0)),
            Map.entry("&1", new Color(0, 0, 170)),
            Map.entry("&2", new Color(0, 170, 0)),
            Map.entry("&3", new Color(0, 170, 170)),
            Map.entry("&4", new Color(170, 0, 0)),
            Map.entry("&5", new Color(170, 0, 170)),
            Map.entry("&6", new Color(255, 170, 0)),
            Map.entry("&7", new Color(170, 170, 170)),
            Map.entry("&8", new Color(85, 85, 85)),
            Map.entry("&9", new Color(85, 85, 255)),
            Map.entry("&a", new Color(85, 255, 85)),
            Map.entry("&b", new Color(85, 255, 255)),
            Map.entry("&c", new Color(255, 85, 85)),
            Map.entry("&d", new Color(255, 85, 255)),
            Map.entry("&e", new Color(255, 255, 85)),
            Map.entry("&f", new Color(255, 255, 255))
    );

    // ANSI escape codes for terminal output
    private static final Map<String, String> ANSI_CODES = Map.ofEntries(
            Map.entry("&0", "\u001B[30m"),
            Map.entry("&1", "\u001B[34m"),
            Map.entry("&2", "\u001B[32m"),
            Map.entry("&3", "\u001B[36m"),
            Map.entry("&4", "\u001B[31m"),
            Map.entry("&5", "\u001B[35m"),
            Map.entry("&6", "\u001B[33m"),
            Map.entry("&7", "\u001B[37m"),
            Map.entry("&8", "\u001B[90m"),
            Map.entry("&9", "\u001B[94m"),
            Map.entry("&a", "\u001B[92m"),
            Map.entry("&b", "\u001B[96m"),
            Map.entry("&c", "\u001B[91m"),
            Map.entry("&d", "\u001B[95m"),
            Map.entry("&e", "\u001B[93m"),
            Map.entry("&f", "\u001B[97m"),
            Map.entry("&l", "\u001B[1m"),
            Map.entry("&o", "\u001B[3m"),
            Map.entry("&r", "\u001B[0m")
    );

    private ColorParser() {}

    // ==================== PARSING ====================

    /**
     * Parse text into segments with color and style information.
     * 
     * @param input Text with color codes
     * @return List of text segments
     */
    public static java.util.List<TextSegment> parse(String input) {
        java.util.List<TextSegment> segments = new java.util.ArrayList<>();
        
        if (input == null || input.isEmpty()) {
            return segments;
        }

        int lastIndex = 0;
        Color currentColor = Color.WHITE;
        boolean bold = false;
        boolean italic = false;
        boolean mono = false;

        Matcher matcher = CODE_PATTERN.matcher(input);

        while (matcher.find()) {
            // Add text before this code
            if (matcher.start() > lastIndex) {
                String text = input.substring(lastIndex, matcher.start());
                segments.add(new TextSegment(text, currentColor, bold, italic, mono));
            }

            String code = matcher.group().toLowerCase();

            if (code.startsWith("&#")) {
                try {
                    currentColor = Color.decode(code.substring(1));
                } catch (NumberFormatException ignored) {}
            } else {
                switch (code) {
                    case "&l" -> bold = true;
                    case "&o" -> italic = true;
                    case "&m" -> mono = true;
                    case "&r" -> {
                        currentColor = Color.WHITE;
                        bold = italic = mono = false;
                    }
                    default -> currentColor = COLORS.getOrDefault(code, currentColor);
                }
            }

            lastIndex = matcher.end();
        }

        // Add remaining text
        if (lastIndex < input.length()) {
            segments.add(new TextSegment(input.substring(lastIndex), currentColor, bold, italic, mono));
        }

        return segments;
    }

    /**
     * Strip all color codes from text.
     * 
     * @param input Text with color codes
     * @return Plain text without codes
     */
    public static String strip(String input) {
        if (input == null) return null;
        return CODE_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Convert color codes to ANSI escape sequences for terminal output.
     * 
     * @param input Text with color codes
     * @return Text with ANSI codes
     */
    public static String toAnsi(String input) {
        if (input == null) return null;
        
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;
        Matcher matcher = CODE_PATTERN.matcher(input);

        while (matcher.find()) {
            result.append(input, lastIndex, matcher.start());
            
            String code = matcher.group().toLowerCase();
            String ansi = ANSI_CODES.get(code);
            
            if (ansi != null) {
                result.append(ansi);
            } else if (code.startsWith("&#")) {
                // Convert hex to nearest ANSI (simplified)
                result.append("\u001B[38;2;");
                try {
                    Color c = Color.decode(code.substring(1));
                    result.append(c.getRed()).append(";")
                          .append(c.getGreen()).append(";")
                          .append(c.getBlue()).append("m");
                } catch (NumberFormatException e) {
                    result.append("255;255;255m");
                }
            }
            
            lastIndex = matcher.end();
        }

        result.append(input.substring(lastIndex));
        result.append("\u001B[0m"); // Reset at end
        
        return result.toString();
    }

    /**
     * Get the Color for a code like {@code "&6"} or {@code "&#FF0000"}.
     * 
     * @param code The color code
     * @return The Color, or WHITE if invalid
     */
    public static Color getColor(String code) {
        if (code == null) return Color.WHITE;
        
        code = code.toLowerCase();
        if (!code.startsWith("&")) {
            code = "&" + code;
        }

        if (code.startsWith("&#")) {
            try {
                return Color.decode(code.substring(1));
            } catch (NumberFormatException e) {
                return Color.WHITE;
            }
        }

        return COLORS.getOrDefault(code, Color.WHITE);
    }

    /**
     * Convert a Color to a hex code string.
     */
    public static String toHex(Color color) {
        return String.format("&#%02X%02X%02X", 
                color.getRed(), color.getGreen(), color.getBlue());
    }

    // ==================== TEXT SEGMENT ====================

    /**
     * Represents a segment of text with style information.
     */
    public static class TextSegment {
        private final String text;
        private final Color color;
        private final boolean bold;
        private final boolean italic;
        private final boolean monospace;

        public TextSegment(String text, Color color, boolean bold, boolean italic, boolean monospace) {
            this.text = text;
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.monospace = monospace;
        }

        public String getText() { return text; }
        public Color getColor() { return color; }
        public boolean isBold() { return bold; }
        public boolean isItalic() { return italic; }
        public boolean isMonospace() { return monospace; }

        @Override
        public String toString() {
            return String.format("TextSegment[text='%s', color=%s, bold=%s, italic=%s, mono=%s]",
                    text, toHex(color), bold, italic, monospace);
        }
    }
}
