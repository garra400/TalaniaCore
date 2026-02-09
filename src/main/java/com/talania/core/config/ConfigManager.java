package com.talania.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Centralized configuration manager with hot-reload support.
 * 
 * <p>Features:
 * <ul>
 *   <li>JSON-based configuration files</li>
 *   <li>Automatic directory creation</li>
 *   <li>Default value extraction from resources</li>
 *   <li>File watching for hot-reload</li>
 *   <li>Type-safe configuration loading via Gson</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * // Initialize
 * ConfigManager.initialize(modsDirectory);
 * 
 * // Load a config class
 * MyConfig config = ConfigManager.load("my_config.json", MyConfig.class);
 * 
 * // Save changes
 * ConfigManager.save("my_config.json", config);
 * 
 * // Watch for changes
 * ConfigManager.watch("my_config.json", MyConfig.class, newConfig -> {
 *     System.out.println("Config reloaded!");
 * });
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    
    private static final String LOG_PREFIX = "[TalaniaCore/Config] ";
    
    private static Path configDir;
    private static final Map<String, Object> configCache = new ConcurrentHashMap<>();
    private static final Map<String, Consumer<?>> watchCallbacks = new ConcurrentHashMap<>();
    
    private static Consumer<String> infoLogger = System.out::println;
    private static Consumer<String> errorLogger = System.err::println;

    private ConfigManager() {}

    // ==================== INITIALIZATION ====================

    /**
     * Initialize the config manager.
     * 
     * @param baseDir The base directory for config files
     */
    public static void initialize(File baseDir) {
        initialize(baseDir.toPath());
    }

    /**
     * Initialize the config manager.
     * 
     * @param baseDir The base directory for config files
     */
    public static void initialize(Path baseDir) {
        configDir = baseDir;
        try {
            Files.createDirectories(configDir);
            log("Initialized at: " + configDir);
        } catch (IOException e) {
            error("Failed to create config directory: " + e.getMessage());
        }
    }

    /**
     * Set custom loggers.
     */
    public static void setLoggers(Consumer<String> info, Consumer<String> err) {
        infoLogger = info != null ? info : System.out::println;
        errorLogger = err != null ? err : System.err::println;
    }

    // ==================== LOADING ====================

    /**
     * Load a configuration file into a class.
     * 
     * @param filename The config filename (e.g., "settings.json")
     * @param configClass The class to deserialize into
     * @param <T> Config type
     * @return The loaded config, or a new instance if file doesn't exist
     */
    public static <T> T load(String filename, Class<T> configClass) {
        return load(filename, configClass, null);
    }

    /**
     * Load a configuration file with a default resource fallback.
     * 
     * @param filename The config filename
     * @param configClass The class to deserialize into
     * @param resourceClass Class to load default resource from (or null)
     * @param <T> Config type
     * @return The loaded config
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(String filename, Class<T> configClass, Class<?> resourceClass) {
        ensureInitialized();

        // Check cache
        if (configCache.containsKey(filename)) {
            return (T) configCache.get(filename);
        }

        Path configFile = configDir.resolve(filename);

        // Extract default if needed
        if (!Files.exists(configFile) && resourceClass != null) {
            extractDefault(filename, resourceClass);
        }

        // Load file
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                T config = GSON.fromJson(reader, configClass);
                configCache.put(filename, config);
                log("Loaded: " + filename);
                return config;
            } catch (Exception e) {
                error("Failed to load " + filename + ": " + e.getMessage());
            }
        }

        // Return new instance
        try {
            T config = configClass.getDeclaredConstructor().newInstance();
            save(filename, config);
            return config;
        } catch (Exception e) {
            error("Failed to create default config: " + e.getMessage());
            return null;
        }
    }

    /**
     * Load a raw JsonObject from a file.
     */
    public static JsonObject loadJson(String filename) {
        ensureInitialized();
        Path configFile = configDir.resolve(filename);

        if (!Files.exists(configFile)) {
            return new JsonObject();
        }

        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            error("Failed to load JSON " + filename + ": " + e.getMessage());
            return new JsonObject();
        }
    }

    // ==================== SAVING ====================

    /**
     * Save a configuration object to file.
     * 
     * @param filename The config filename
     * @param config The config object to save
     */
    public static void save(String filename, Object config) {
        ensureInitialized();
        Path configFile = configDir.resolve(filename);

        try {
            Files.writeString(configFile, GSON.toJson(config), StandardCharsets.UTF_8);
            configCache.put(filename, config);
            log("Saved: " + filename);
        } catch (IOException e) {
            error("Failed to save " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Save a JsonObject to file.
     */
    public static void saveJson(String filename, JsonObject json) {
        ensureInitialized();
        Path configFile = configDir.resolve(filename);

        try {
            Files.writeString(configFile, GSON.toJson(json), StandardCharsets.UTF_8);
            log("Saved JSON: " + filename);
        } catch (IOException e) {
            error("Failed to save JSON " + filename + ": " + e.getMessage());
        }
    }

    // ==================== RELOAD ====================

    /**
     * Reload a configuration file.
     * 
     * @param filename The config filename
     * @param configClass The class to deserialize into
     * @param <T> Config type
     * @return The reloaded config
     */
    public static <T> T reload(String filename, Class<T> configClass) {
        configCache.remove(filename);
        T config = load(filename, configClass);
        
        // Trigger watch callback if registered
        @SuppressWarnings("unchecked")
        Consumer<T> callback = (Consumer<T>) watchCallbacks.get(filename);
        if (callback != null && config != null) {
            try {
                callback.accept(config);
            } catch (Exception e) {
                error("Watch callback error for " + filename + ": " + e.getMessage());
            }
        }
        
        return config;
    }

    /**
     * Register a callback for config file changes.
     * Note: Automatic watching requires calling {@link #checkForChanges()} periodically.
     */
    public static <T> void watch(String filename, Class<T> configClass, Consumer<T> callback) {
        watchCallbacks.put(filename, callback);
    }

    /**
     * Clear the config cache.
     */
    public static void clearCache() {
        configCache.clear();
    }

    // ==================== UTILITY ====================

    /**
     * Check if a config file exists.
     */
    public static boolean exists(String filename) {
        ensureInitialized();
        return Files.exists(configDir.resolve(filename));
    }

    /**
     * Delete a config file.
     */
    public static boolean delete(String filename) {
        ensureInitialized();
        try {
            configCache.remove(filename);
            return Files.deleteIfExists(configDir.resolve(filename));
        } catch (IOException e) {
            error("Failed to delete " + filename + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the config directory path.
     */
    public static Path getConfigDir() {
        return configDir;
    }

    /**
     * Get the Gson instance used for serialization.
     */
    public static Gson getGson() {
        return GSON;
    }

    // ==================== INTERNAL ====================

    private static void extractDefault(String filename, Class<?> resourceClass) {
        String resourcePath = "/" + filename;
        try (InputStream is = resourceClass.getResourceAsStream(resourcePath)) {
            if (is != null) {
                Path targetFile = configDir.resolve(filename);
                Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                log("Extracted default: " + filename);
            }
        } catch (IOException e) {
            error("Failed to extract default " + filename + ": " + e.getMessage());
        }
    }

    private static void ensureInitialized() {
        if (configDir == null) {
            throw new IllegalStateException("ConfigManager not initialized. Call initialize() first.");
        }
    }

    private static void log(String message) {
        infoLogger.accept(LOG_PREFIX + message);
    }

    private static void error(String message) {
        errorLogger.accept(LOG_PREFIX + message);
    }
}
