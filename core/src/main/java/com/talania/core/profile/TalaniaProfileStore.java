package com.talania.core.profile;

import com.talania.core.progression.LevelProgress;
import com.talania.core.stats.StatType;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * JSON-backed profile store for Talania player data.
 *
 * <p>Uses BSON documents for structured serialization, matching the existing
 * multiclass-leveling approach.</p>
 */
public final class TalaniaProfileStore {
    private static final int CURRENT_VERSION = 2;
    private final Path profilesDirectory;

    public TalaniaProfileStore(Path dataDirectory) {
        this.profilesDirectory = dataDirectory.resolve("talania");
    }

    /**
     * Load a player profile from disk, or create a default if missing.
     */
    public TalaniaPlayerProfile loadProfile(UUID playerId) {
        Path file = profilePath(playerId);
        if (!Files.isRegularFile(file)) {
            return createDefaultProfile(playerId);
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            BsonDocument document = BsonDocument.parse(json);
            return readProfile(document, playerId);
        } catch (Exception ex) {
            return createDefaultProfile(playerId);
        }
    }

    /**
     * Save a player profile to disk (best-effort).
     */
    public void saveProfile(TalaniaPlayerProfile profile) {
        Path file = profilePath(profile.playerId());
        try {
            Files.createDirectories(file.getParent());
            BsonDocument document = writeProfile(profile);
            Files.writeString(file, document.toJson(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            // Intentionally ignored; persistence failures should not crash gameplay
        }
    }

    private Path profilePath(UUID playerId) {
        return profilesDirectory.resolve(playerId.toString().toLowerCase(Locale.ROOT) + ".json");
    }

    /**
     * Build a new profile with default base stats.
     */
    private TalaniaPlayerProfile createDefaultProfile(UUID playerId) {
        TalaniaPlayerProfile profile = new TalaniaPlayerProfile(playerId);
        profile.setProfileVersion(CURRENT_VERSION);
        for (StatType stat : StatType.values()) {
            profile.setBaseStat(stat, stat.getDefaultValue());
        }
        return profile;
    }

    private TalaniaPlayerProfile readProfile(BsonDocument document, UUID playerId) {
        TalaniaPlayerProfile profile = createDefaultProfile(playerId);
        int version = readInt(document.get("version"), CURRENT_VERSION);
        profile.setProfileVersion(version);
        profile.setRaceId(readString(document.get("race"), null));
        profile.setClassId(readString(document.get("class"), null));

        BsonDocument statsDoc = readDocument(document.get("baseStats"));
        if (statsDoc != null) {
            for (Map.Entry<String, BsonValue> entry : statsDoc.entrySet()) {
                StatType stat = StatType.fromId(entry.getKey());
                if (stat == null || entry.getValue() == null || !entry.getValue().isNumber()) {
                    continue;
                }
                profile.setBaseStat(stat, (float) entry.getValue().asNumber().doubleValue());
            }
        }

        BsonDocument classDoc = readDocument(document.get("classLevels"));
        if (classDoc != null) {
            for (Map.Entry<String, BsonValue> entry : classDoc.entrySet()) {
                String classId = entry.getKey();
                BsonDocument progressDoc = readDocument(entry.getValue());
                if (progressDoc == null || classId == null || classId.isBlank()) {
                    continue;
                }
                int level = readInt(progressDoc.get("level"), 0);
                long xp = readLong(progressDoc.get("xp"), 0L);
                profile.classProgress().put(classId, new LevelProgress(level, xp));
            }
        }
        return profile;
    }

    /**
     * Serialize profile to BSON-backed JSON.
     */
    private BsonDocument writeProfile(TalaniaPlayerProfile profile) {
        BsonDocument document = new BsonDocument();
        document.put("version", new BsonInt32(CURRENT_VERSION));
        if (profile.raceId() != null) {
            document.put("race", new BsonString(profile.raceId()));
        }
        if (profile.classId() != null) {
            document.put("class", new BsonString(profile.classId()));
        }

        BsonDocument stats = new BsonDocument();
        for (StatType stat : StatType.values()) {
            stats.put(stat.getId(), new BsonDouble(profile.getBaseStat(stat, 0.0f)));
        }
        document.put("baseStats", stats);

        BsonDocument classLevels = new BsonDocument();
        for (Map.Entry<String, LevelProgress> entry : profile.classProgress().entrySet()) {
            LevelProgress progress = entry.getValue();
            if (progress == null) {
                continue;
            }
            BsonDocument progressDoc = new BsonDocument();
            progressDoc.put("level", new BsonInt32(progress.level()));
            progressDoc.put("xp", new BsonInt64(progress.xp()));
            classLevels.put(entry.getKey(), progressDoc);
        }
        if (!classLevels.isEmpty()) {
            document.put("classLevels", classLevels);
        }
        return document;
    }

    /**
     * Read an int from a BSON value with fallback.
     */
    private int readInt(BsonValue value, int fallback) {
        if (value == null || !value.isNumber()) {
            return fallback;
        }
        return value.asNumber().intValue();
    }

    /**
     * Read a long from a BSON value with fallback.
     */
    private long readLong(BsonValue value, long fallback) {
        if (value == null || !value.isNumber()) {
            return fallback;
        }
        return value.asNumber().longValue();
    }

    /**
     * Read a string from a BSON value with fallback.
     */
    private String readString(BsonValue value, String fallback) {
        if (value == null || !value.isString()) {
            return fallback;
        }
        return value.asString().getValue();
    }

    /**
     * Read a BSON document from a value if present.
     */
    private BsonDocument readDocument(BsonValue value) {
        if (value instanceof BsonDocument doc) {
            return doc;
        }
        return null;
    }
}
