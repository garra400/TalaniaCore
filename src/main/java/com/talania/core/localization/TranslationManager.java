package com.talania.core.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Core Translation Manager for TalaniaCore
 * 
 * <p>Provides a centralized, thread-safe localization system with:
 * <ul>
 *   <li>JSON-based language files</li>
 *   <li>Automatic fallback to default language</li>
 *   <li>Hot-reload support</li>
 *   <li>Parameter substitution</li>
 *   <li>Language change listeners</li>
 * </ul>
 * 
 * <p>Usage example:
 * <pre>{@code
 * // Initialize once at startup
 * TranslationManager.initialize(modsDirectory);
 * 
 * // Get translations
 * String text = TranslationManager.translate("ui.welcome");
 * String formatted = TranslationManager.translate("player.damage", 50, "Enemy");
 * 
 * // Change language
 * TranslationManager.setLanguage("pt_br");
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public class TranslationManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String CONFIG_FILE = "language_config.json";
    private static final String LOG_PREFIX = "[TalaniaCore] ";

    // Thread-safe storage
    private static final Map<String, Map<String, String>> translations = new ConcurrentHashMap<>();
    private static final List<Consumer<String>> languageChangeListeners = new ArrayList<>();
    
    private static volatile String currentLanguage = DEFAULT_LANGUAGE;
    private static volatile Path languagesDir;
    private static volatile Path configDir;
    private static volatile boolean initialized = false;

    // Logger interface for mod integration
    private static Consumer<String> infoLogger = System.out::println;
    private static Consumer<String> errorLogger = System.err::println;

    private TranslationManager() {}

    // ==================== INITIALIZATION ====================

    /**
     * Initialize the translation manager.
     * 
     * @param modsDir The mods directory containing a "languages" subfolder
     */
    public static void initialize(File modsDir) {
        initialize(modsDir.toPath());
    }

    /**
     * Initialize the translation manager.
     * 
     * @param modsDir The mods directory containing a "languages" subfolder
     */
    public static void initialize(Path modsDir) {
        if (initialized) {
            log("TranslationManager already initialized, reloading...");
            reload();
            return;
        }

        configDir = modsDir;
        languagesDir = modsDir.resolve("languages");

        // Create languages directory if needed
        try {
            Files.createDirectories(languagesDir);
        } catch (IOException e) {
            error("Failed to create languages directory: " + e.getMessage());
        }

        // Extract bundled language files
        extractBundledLanguages();

        // Load all translations
        loadAllTranslations();

        // Load user language preference
        loadLanguagePreference();

        initialized = true;
        log("TranslationManager initialized with language: " + currentLanguage);
    }

    /**
     * Set custom loggers for mod integration.
     * 
     * @param info Logger for info messages
     * @param err Logger for error messages
     */
    public static void setLoggers(Consumer<String> info, Consumer<String> err) {
        infoLogger = info != null ? info : System.out::println;
        errorLogger = err != null ? err : System.err::println;
    }

    // ==================== TRANSLATION ====================

    /**
     * Translate a key to the current language.
     * Falls back to default language, then to the key itself.
     * 
     * @param key The translation key (e.g., "ui.welcome")
     * @param args Optional format arguments
     * @return The translated and formatted string
     */
    public static String translate(String key, Object... args) {
        String translation = getRawTranslation(key);

        if (args.length > 0) {
            try {
                return String.format(translation, args);
            } catch (IllegalFormatException e) {
                error("Format error for key '" + key + "': " + e.getMessage());
                return translation;
            }
        }

        return translation;
    }

    /**
     * Alias for {@link #translate(String, Object...)}.
     */
    public static String get(String key, Object... args) {
        return translate(key, args);
    }

    /**
     * Check if a translation key exists in the current or default language.
     * 
     * @param key The translation key
     * @return true if the key has a translation
     */
    public static boolean hasKey(String key) {
        Map<String, String> currentMap = translations.get(currentLanguage);
        if (currentMap != null && currentMap.containsKey(key)) {
            return true;
        }

        if (!currentLanguage.equals(DEFAULT_LANGUAGE)) {
            Map<String, String> defaultMap = translations.get(DEFAULT_LANGUAGE);
            return defaultMap != null && defaultMap.containsKey(key);
        }

        return false;
    }

    /**
     * Get all translation keys for the current language.
     * 
     * @return Unmodifiable set of translation keys
     */
    public static Set<String> getKeys() {
        Map<String, String> currentMap = translations.get(currentLanguage);
        if (currentMap != null) {
            return Collections.unmodifiableSet(currentMap.keySet());
        }
        return Collections.emptySet();
    }

    // ==================== LANGUAGE MANAGEMENT ====================

    /**
     * Set the current language.
     * 
     * @param langCode Language code (e.g., "en", "pt_br")
     * @return true if language was changed, false if not available
     */
    public static boolean setLanguage(String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return false;
        }

        langCode = langCode.toLowerCase().trim();

        if (!translations.containsKey(langCode)) {
            error("Language not available: " + langCode);
            return false;
        }

        String oldLanguage = currentLanguage;
        currentLanguage = langCode;
        saveLanguagePreference();

        log("Language changed: " + oldLanguage + " -> " + langCode);

        // Notify listeners
        for (Consumer<String> listener : languageChangeListeners) {
            try {
                listener.accept(langCode);
            } catch (Exception e) {
                error("Language change listener error: " + e.getMessage());
            }
        }

        return true;
    }

    /**
     * Get the current language code.
     */
    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Get the default fallback language code.
     */
    public static String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    /**
     * Get all available languages.
     * 
     * @return Map of language code to display name
     */
    public static Map<String, String> getAvailableLanguages() {
        Map<String, String> result = new LinkedHashMap<>();

        for (String langCode : translations.keySet()) {
            String displayName = translations.get(langCode).getOrDefault("language.name", langCode);
            result.put(langCode, displayName);
        }

        return result;
    }

    /**
     * Check if a language is available.
     */
    public static boolean isLanguageAvailable(String langCode) {
        return langCode != null && translations.containsKey(langCode.toLowerCase());
    }

    /**
     * Add a listener for language changes.
     * 
     * @param listener Consumer that receives the new language code
     */
    public static void addLanguageChangeListener(Consumer<String> listener) {
        if (listener != null) {
            languageChangeListeners.add(listener);
        }
    }

    /**
     * Remove a language change listener.
     */
    public static void removeLanguageChangeListener(Consumer<String> listener) {
        languageChangeListeners.remove(listener);
    }

    // ==================== RELOAD ====================

    /**
     * Reload all translation files from disk.
     */
    public static void reload() {
        if (languagesDir == null) {
            error("Cannot reload: TranslationManager not initialized");
            return;
        }

        translations.clear();
        loadAllTranslations();
        loadLanguagePreference();
        log("Translations reloaded");
    }

    /**
     * Register additional bundled languages from a mod's resources.
     * Call this after initialize() to add mod-specific translations.
     * 
     * @param resourceClass A class from the mod JAR to load resources from
     * @param languageCodes Language codes to extract (e.g., "en", "pt_br")
     */
    public static void registerBundledLanguages(Class<?> resourceClass, String... languageCodes) {
        for (String langCode : languageCodes) {
            String resourcePath = "/languages/" + langCode + ".json";
            try (InputStream is = resourceClass.getResourceAsStream(resourcePath)) {
                if (is != null) {
                    Map<String, String> bundled = parseLanguageStream(is);
                    Map<String, String> existing = translations.getOrDefault(langCode, new HashMap<>());
                    
                    // Bundled values are defaults, don't override user customizations
                    for (Map.Entry<String, String> entry : bundled.entrySet()) {
                        existing.putIfAbsent(entry.getKey(), entry.getValue());
                    }
                    
                    translations.put(langCode, existing);
                    log("Registered bundled language: " + langCode);
                }
            } catch (IOException e) {
                error("Failed to load bundled language " + langCode + ": " + e.getMessage());
            }
        }
    }

    // ==================== INTERNAL ====================

    private static String getRawTranslation(String key) {
        // Try current language
        Map<String, String> currentMap = translations.get(currentLanguage);
        if (currentMap != null) {
            String value = currentMap.get(key);
            if (value != null) {
                return value;
            }
        }

        // Fallback to default language
        if (!currentLanguage.equals(DEFAULT_LANGUAGE)) {
            Map<String, String> defaultMap = translations.get(DEFAULT_LANGUAGE);
            if (defaultMap != null) {
                String value = defaultMap.get(key);
                if (value != null) {
                    return value;
                }
            }
        }

        // Return key as fallback
        error("Missing translation: " + key);
        return key;
    }

    private static void extractBundledLanguages() {
        String[] defaultLanguages = {"en", "pt_br"};

        for (String lang : defaultLanguages) {
            Path targetFile = languagesDir.resolve(lang + ".json");
            
            // Only extract if file doesn't exist
            if (Files.exists(targetFile)) {
                continue;
            }

            String resourcePath = "/languages/" + lang + ".json";
            try (InputStream is = TranslationManager.class.getResourceAsStream(resourcePath)) {
                if (is != null) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    Files.writeString(targetFile, content, StandardCharsets.UTF_8);
                    log("Extracted default language: " + lang);
                } else {
                    // Create empty template
                    createEmptyLanguageFile(targetFile, lang);
                }
            } catch (IOException e) {
                error("Failed to extract language " + lang + ": " + e.getMessage());
                createEmptyLanguageFile(targetFile, lang);
            }
        }
    }

    private static void createEmptyLanguageFile(Path file, String langCode) {
        try {
            JsonObject template = new JsonObject();
            template.addProperty("language.name", langCode.toUpperCase());
            template.addProperty("language.code", langCode);
            
            Files.writeString(file, GSON.toJson(template), StandardCharsets.UTF_8);
        } catch (IOException e) {
            error("Failed to create empty language file: " + e.getMessage());
        }
    }

    private static void loadAllTranslations() {
        translations.clear();

        if (languagesDir == null || !Files.isDirectory(languagesDir)) {
            error("Languages directory not found");
            return;
        }

        try {
            Files.list(languagesDir)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(TranslationManager::loadLanguageFile);
        } catch (IOException e) {
            error("Failed to list language files: " + e.getMessage());
        }
    }

    private static void loadLanguageFile(Path file) {
        String langCode = file.getFileName().toString().replace(".json", "");

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Map<String, String> langMap = parseLanguageReader(reader);
            translations.put(langCode, new ConcurrentHashMap<>(langMap));
            log("Loaded language: " + langCode + " (" + langMap.size() + " keys)");
        } catch (Exception e) {
            error("Failed to load language " + langCode + ": " + e.getMessage());
        }
    }

    private static Map<String, String> parseLanguageReader(Reader reader) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        return parseJsonObject(json, "");
    }

    private static Map<String, String> parseLanguageStream(InputStream is) throws IOException {
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return parseLanguageReader(reader);
        }
    }

    private static Map<String, String> parseJsonObject(JsonObject json, String prefix) {
        Map<String, String> result = new HashMap<>();

        for (String key : json.keySet()) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            var element = json.get(key);

            if (element.isJsonPrimitive()) {
                result.put(fullKey, element.getAsString());
            } else if (element.isJsonObject()) {
                // Support nested objects: {"ui": {"welcome": "Hello"}} -> "ui.welcome"
                result.putAll(parseJsonObject(element.getAsJsonObject(), fullKey));
            }
        }

        return result;
    }

    private static void loadLanguagePreference() {
        if (configDir == null) return;

        Path configFile = configDir.resolve(CONFIG_FILE);
        if (!Files.exists(configFile)) return;

        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();

            if (config.has("current_language")) {
                String lang = config.get("current_language").getAsString();
                if (translations.containsKey(lang)) {
                    currentLanguage = lang;
                }
            }
        } catch (Exception e) {
            error("Failed to load language preference: " + e.getMessage());
        }
    }

    private static void saveLanguagePreference() {
        if (configDir == null) return;

        Path configFile = configDir.resolve(CONFIG_FILE);

        try {
            JsonObject config = new JsonObject();
            config.addProperty("current_language", currentLanguage);
            Files.writeString(configFile, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            error("Failed to save language preference: " + e.getMessage());
        }
    }

    private static void log(String message) {
        infoLogger.accept(LOG_PREFIX + message);
    }

    private static void error(String message) {
        errorLogger.accept(LOG_PREFIX + message);
    }
}
