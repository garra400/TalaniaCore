package com.talania.core.localization;

import java.util.function.Function;

/**
 * Shorthand helper for translations.
 * 
 * <p>Provides convenient static methods for common translation operations.
 * Can be extended with custom formatters (e.g., color codes for Hytale).
 * 
 * <p>Usage:
 * <pre>{@code
 * // Simple translation
 * String text = T.t("ui.welcome");
 * 
 * // With parameters
 * String msg = T.t("player.damage", 50, "Zombie");
 * 
 * // Raw (no formatting)
 * String raw = T.raw("tooltip.description");
 * 
 * // With custom formatter (e.g., color codes)
 * T.setFormatter(ColorConverter::process);
 * String colored = T.t("ui.title"); // Now applies colors
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class T {

    private static Function<String, String> formatter = Function.identity();

    private T() {}

    /**
     * Translate a key and apply the current formatter.
     * 
     * @param key Translation key
     * @param args Format arguments
     * @return Translated and formatted string
     */
    public static String t(String key, Object... args) {
        String translated = TranslationManager.translate(key, args);
        return formatter.apply(translated);
    }

    /**
     * Translate a key without applying any formatter.
     * 
     * @param key Translation key
     * @param args Format arguments
     * @return Raw translated string
     */
    public static String raw(String key, Object... args) {
        return TranslationManager.translate(key, args);
    }

    /**
     * Alias for {@link #t(String, Object...)}.
     */
    public static String get(String key, Object... args) {
        return t(key, args);
    }

    /**
     * Check if a translation key exists.
     */
    public static boolean has(String key) {
        return TranslationManager.hasKey(key);
    }

    /**
     * Set a custom formatter to apply to all translations.
     * Useful for processing color codes, markdown, etc.
     * 
     * @param format Function that transforms translated strings
     */
    public static void setFormatter(Function<String, String> format) {
        formatter = format != null ? format : Function.identity();
    }

    /**
     * Reset the formatter to identity (no transformation).
     */
    public static void resetFormatter() {
        formatter = Function.identity();
    }

    /**
     * Get the current language code.
     */
    public static String lang() {
        return TranslationManager.getCurrentLanguage();
    }

    /**
     * Change the current language.
     * 
     * @param langCode Language code (e.g., "en", "pt_br")
     * @return true if successful
     */
    public static boolean setLang(String langCode) {
        return TranslationManager.setLanguage(langCode);
    }
}
