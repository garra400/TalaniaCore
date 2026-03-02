package com.talania.races.cosmetics;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.races.RaceType;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RaceCosmeticOverrides {
    private static final String DEFAULT_SKIN_TONE = "07";

    public static final String ID_BEASTKIN_EARS = "Talania_Beastkin_Ears";
    public static final String ID_ORC_TEETH = "Talania_Orc_Teeth";
    public static final String ID_STARBORN_GEM = "Talania_Starborn_Gem";
    public static final String ID_NIGHTWALKER_BODY = "Talania_Nightwalker_Body";

    private static volatile boolean registered = false;

    private RaceCosmeticOverrides() {}

    public static void ensureRegistered() {
        if (registered) {
            return;
        }
        CosmeticsModule cosmetics = CosmeticsModule.get();
        if (cosmetics == null || cosmetics.getRegistry() == null) {
            return;
        }
        synchronized (RaceCosmeticOverrides.class) {
            if (registered) {
                return;
            }
            registerCustomParts(cosmetics.getRegistry());
            registered = true;
        }
    }

    public static void apply(PlayerRef playerRef, RaceType race) {
        if (playerRef == null || race == null) {
            return;
        }
        applyInternal(playerRef, race);
    }

    public static void clear(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }
        applyInternal(playerRef, null);
    }

    public static void clear(java.util.UUID playerId) {
        if (playerId == null) {
            return;
        }
        PlayerRef playerRef = Universe.get().getPlayer(playerId);
        if (playerRef != null) {
            clear(playerRef);
        }
    }

    private static void applyInternal(PlayerRef playerRef, RaceType race) {
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = ref != null ? ref.getStore() : null;
        if (ref == null || store == null) {
            return;
        }
        CosmeticsModule cosmetics = CosmeticsModule.get();
        if (cosmetics == null) {
            return;
        }
        ensureRegistered();
        PlayerSkin current = getOrCreateSkin(ref, store, cosmetics);
        PlayerSkin updated = new PlayerSkin(current);

        String bodyTone = extractTextureKey(current.bodyCharacteristic, DEFAULT_SKIN_TONE);
        String mouthTone = extractTextureKey(current.mouth, bodyTone);
        String earsTone = extractTextureKey(current.ears, bodyTone);

        applyBody(updated, race, bodyTone);
        applyEars(updated, race, earsTone);
        applyMouth(updated, race, mouthTone);
        applyFaceAccessory(updated, race);

        if (isValidSkin(cosmetics, updated)) {
            setPlayerSkin(ref, store, updated);
        }
    }

    private static void applyBody(PlayerSkin skin, RaceType race, String tone) {
        if (race == RaceType.NIGHTWALKER) {
            skin.bodyCharacteristic = ID_NIGHTWALKER_BODY + "." + tone;
            return;
        }
        if (skin.bodyCharacteristic != null && skin.bodyCharacteristic.startsWith(ID_NIGHTWALKER_BODY + ".")) {
            skin.bodyCharacteristic = "Default." + tone;
        }
    }

    private static void applyEars(PlayerSkin skin, RaceType race, String tone) {
        if (race == RaceType.BEASTKIN) {
            skin.ears = ID_BEASTKIN_EARS + ".Default";
            return;
        }
        if (skin.ears != null && skin.ears.startsWith(ID_BEASTKIN_EARS + ".")) {
            skin.ears = "Default." + tone;
        }
    }

    private static void applyMouth(PlayerSkin skin, RaceType race, String tone) {
        if (race == RaceType.ORC) {
            skin.mouth = ID_ORC_TEETH + ".Default";
            return;
        }
        if (skin.mouth != null && skin.mouth.startsWith(ID_ORC_TEETH + ".")) {
            skin.mouth = "Mouth_Default." + tone;
        }
    }

    private static void applyFaceAccessory(PlayerSkin skin, RaceType race) {
        if (race == RaceType.STARBORN) {
            skin.faceAccessory = ID_STARBORN_GEM + ".Default";
            return;
        }
        if (skin.faceAccessory != null && skin.faceAccessory.startsWith(ID_STARBORN_GEM + ".")) {
            skin.faceAccessory = null;
        }
    }

    private static String extractTextureKey(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String[] parts = value.split("\\.");
        if (parts.length > 1 && !parts[1].isBlank()) {
            return parts[1];
        }
        return fallback;
    }

    private static PlayerSkin getOrCreateSkin(Ref<EntityStore> ref, Store<EntityStore> store, CosmeticsModule cosmetics) {
        PlayerSkinComponent component =
                (PlayerSkinComponent) store.getComponent(ref, PlayerSkinComponent.getComponentType());
        if (component != null && component.getPlayerSkin() != null) {
            return component.getPlayerSkin();
        }
        PlayerSkin skin = cosmetics.generateRandomSkin(new java.util.Random());
        setPlayerSkin(ref, store, skin);
        return skin;
    }

    private static boolean isValidSkin(CosmeticsModule cosmetics, PlayerSkin skin) {
        try {
            cosmetics.validateSkin(skin);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void setPlayerSkin(Ref<EntityStore> ref, Store<EntityStore> store, PlayerSkin skin) {
        store.getExternalData().getWorld().execute(() -> {
            if (ref == null || !ref.isValid()) {
                return;
            }
            PlayerSkinComponent component = new PlayerSkinComponent(skin);
            component.setNetworkOutdated();
            store.replaceComponent(ref, PlayerSkinComponent.getComponentType(), component);

            CosmeticsModule cosmetics = CosmeticsModule.get();
            if (cosmetics == null) {
                return;
            }
            float scale = 1.0f;
            ModelComponent modelComponent =
                    (ModelComponent) store.getComponent(ref, ModelComponent.getComponentType());
            if (modelComponent != null && modelComponent.getModel() != null) {
                scale = modelComponent.getModel().getScale();
            } else {
                EntityScaleComponent scaleComponent =
                        (EntityScaleComponent) store.getComponent(ref, EntityScaleComponent.getComponentType());
                if (scaleComponent != null) {
                    scale = scaleComponent.getScale();
                }
            }
            com.hypixel.hytale.server.core.asset.type.model.config.Model model =
                    cosmetics.createModel(skin, scale);
            if (model != null) {
                store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
            }
        });
    }

    private static void registerCustomParts(CosmeticRegistry registry) {
        addPart(registry, "ears", createPart(
                ID_BEASTKIN_EARS,
                "talania.cosmetics.ears.beastkin",
                "Cosmetics/Talania/Head/Beastman_Ears_Feline.blockymodel",
                textureDoc("Cosmetics/Talania/Head/Beastman_Ears_Brown.png")));

        addPart(registry, "mouths", createPart(
                ID_ORC_TEETH,
                "talania.cosmetics.mouth.orc_teeth",
                "Cosmetics/Talania/Mouth/Orc_Teeth.blockymodel",
                textureDoc("Cosmetics/Talania/Mouth/Orc_Teeth_Texture.png")));

        addPart(registry, "faceAccessory", createPart(
                ID_STARBORN_GEM,
                "talania.cosmetics.face.starborn_gem",
                "Cosmetics/Talania/Face/Starborn_Face_Gem.blockymodel",
                textureDoc("Cosmetics/Talania/Face/Starborn_Gem_Texture.png")));

        addPart(registry, "bodyCharacteristics", createPart(
                ID_NIGHTWALKER_BODY,
                "talania.cosmetics.body.nightwalker",
                "Characters/Player.blockymodel",
                "Cosmetics/Talania/Body/Player_Greyscale_Nightwalker.png",
                "Skin"));
    }

    private static void addPart(CosmeticRegistry registry, String fieldName, PlayerSkinPart part) {
        if (registry == null || part == null || fieldName == null) {
            return;
        }
        try {
            Field field = CosmeticRegistry.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, PlayerSkinPart> current = (Map<String, PlayerSkinPart>) field.get(registry);
            Map<String, PlayerSkinPart> updated = new LinkedHashMap<>(current);
            updated.put(part.getId(), part);
            field.set(registry, java.util.Collections.unmodifiableMap(updated));
        } catch (Exception ignored) {
            // Ignore to keep cosmetics registration best-effort.
        }
    }

    private static PlayerSkinPart createPart(String id, String name, String model, BsonDocument textures) {
        BsonDocument doc = new BsonDocument();
        doc.put("Id", new BsonString(id));
        doc.put("Name", new BsonString(name));
        doc.put("Model", new BsonString(model));
        doc.put("Textures", textures);
        return buildPart(doc);
    }

    private static PlayerSkinPart createPart(String id, String name, String model, String greyscaleTexture,
                                             String gradientSet) {
        BsonDocument doc = new BsonDocument();
        doc.put("Id", new BsonString(id));
        doc.put("Name", new BsonString(name));
        doc.put("Model", new BsonString(model));
        doc.put("GreyscaleTexture", new BsonString(greyscaleTexture));
        doc.put("GradientSet", new BsonString(gradientSet));
        return buildPart(doc);
    }

    private static BsonDocument textureDoc(String texturePath) {
        BsonDocument texture = new BsonDocument();
        texture.put("Texture", new BsonString(texturePath));
        BsonArray colors = new BsonArray(List.of(new BsonString("#ffffff")));
        texture.put("BaseColor", colors);
        BsonDocument textures = new BsonDocument();
        textures.put("Default", texture);
        return textures;
    }

    private static PlayerSkinPart buildPart(BsonDocument doc) {
        try {
            Constructor<PlayerSkinPart> ctor = PlayerSkinPart.class.getDeclaredConstructor(BsonDocument.class);
            ctor.setAccessible(true);
            return ctor.newInstance(doc);
        } catch (Exception e) {
            return null;
        }
    }
}
