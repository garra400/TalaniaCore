package com.talania.core.cosmetics;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAttachment;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinGradientSet;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class TalaniaCosmeticCore {
    private static final Logger LOG = Logger.getLogger("TalaniaCore/Cosmetics");
    private static volatile TalaniaCosmeticCore instance;

    private final Map<String, CosmeticDefinition> registry = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerCosmeticState> playerState = new ConcurrentHashMap<>();
    private final Set<String> missingCosmeticWarnings = ConcurrentHashMap.newKeySet();

    private TalaniaCosmeticCore() {
    }

    public static TalaniaCosmeticCore init() {
        if (instance == null) {
            instance = new TalaniaCosmeticCore();
        }
        return instance;
    }

    public static TalaniaCosmeticCore get() {
        return instance;
    }

    public void register(CosmeticDefinition definition) {
        if (definition == null) {
            return;
        }
        registry.put(definition.id(), definition);
    }

    public void registerAll(Collection<CosmeticDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return;
        }
        for (CosmeticDefinition def : definitions) {
            register(def);
        }
    }

    public CosmeticDefinition getDefinition(String id) {
        if (id == null) {
            return null;
        }
        return registry.get(id);
    }

    public void handlePlayerReady(PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (playerRef == null || ref == null || store == null) {
            return;
        }
        captureBase(playerRef, ref, store, false);
    }

    public void handlePlayerDisconnect(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }
        clearPlayer(playerRef.getUuid());
    }

    public void captureBase(PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store, boolean force) {
        if (playerRef == null || ref == null || store == null) {
            return;
        }
        UUID playerId = playerRef.getUuid();
        if (playerId == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            if (!ref.isValid()) {
                return;
            }
            PlayerSkin baseSkin = readCurrentSkin(store, ref, playerId);
            if (baseSkin == null) {
                return;
            }
            PlayerCosmeticState state = playerState.computeIfAbsent(playerId, id -> new PlayerCosmeticState());
            if (state.baseSkin != null && !force) {
                return;
            }
            state.baseSkin = baseSkin;
            playerState.put(playerId, state);
            if (!state.overrides.isEmpty()) {
                rebuild(ref, store, state);
            }
        });
    }

    public void clearPlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }
        playerState.remove(playerId);
    }

    public void setOverrides(PlayerRef playerRef, Collection<String> cosmeticIds) {
        if (playerRef == null) {
            return;
        }
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = ref != null ? ref.getStore() : null;
        if (ref == null || store == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            if (!ref.isValid()) {
                return;
            }
            PlayerCosmeticState state = playerState.computeIfAbsent(playerRef.getUuid(), id -> new PlayerCosmeticState());
            if (state.baseSkin == null) {
                state.baseSkin = readCurrentSkin(store, ref, playerRef.getUuid());
            }
            state.overrides = cosmeticIds == null ? new ArrayList<>() : new ArrayList<>(cosmeticIds);
            rebuild(ref, store, state);
        });
    }

    public void clearOverrides(PlayerRef playerRef) {
        setOverrides(playerRef, Collections.emptyList());
    }

    public void refreshBase(PlayerRef playerRef) {
        if (playerRef == null) {
            return;
        }
        Ref<EntityStore> ref = playerRef.getReference();
        Store<EntityStore> store = ref != null ? ref.getStore() : null;
        if (ref == null || store == null) {
            return;
        }
        captureBase(playerRef, ref, store, true);
    }

    private void rebuild(Ref<EntityStore> ref, Store<EntityStore> store, PlayerCosmeticState state) {
        if (state.baseSkin == null) {
            return;
        }
        CosmeticsModule cosmetics = CosmeticsModule.get();
        if (cosmetics == null || cosmetics.getRegistry() == null) {
            LOG.warning("CosmeticsModule not ready; cannot rebuild cosmetics.");
            return;
        }
        ModelComponent modelComponent =
                (ModelComponent) store.getComponent(ref, ModelComponent.getComponentType());
        PlayerSkinComponent skinComponent =
                (PlayerSkinComponent) store.getComponent(ref, PlayerSkinComponent.getComponentType());
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (modelComponent == null || skinComponent == null || player == null) {
            return;
        }
        Model model = modelComponent.getModel();
        if (model == null) {
            return;
        }

        List<ModelAttachment> attachments = new ArrayList<>();
        Map<String, Boolean> overrides = new HashMap<>();

        for (String cosmeticId : state.overrides) {
            if (cosmeticId == null || cosmeticId.isBlank()) {
                continue;
            }
            CosmeticDefinition def = registry.get(cosmeticId);
            if (def == null) {
                if (missingCosmeticWarnings.add(cosmeticId)) {
                    LOG.warning("Talania cosmetic not found: " + cosmeticId);
                }
                continue;
            }
            String slot = normalizeSlot(def.slot());
            if (!slot.isEmpty()) {
                overrides.put(slot, true);
            }
            for (String extra : def.slotOverrides()) {
                String normalized = normalizeSlot(extra);
                if (!normalized.isEmpty()) {
                    overrides.put(normalized, true);
                }
            }

            String gradientSet = def.gradientSet();
            String gradientId = resolveGradientId(cosmetics, state.baseSkin, gradientSet);
            attachments.add(new ModelAttachment(
                    def.model(),
                    def.texture(),
                    gradientSet == null ? "" : gradientSet,
                    gradientId,
                    1
            ));
        }

        restoreBaseAttachments(cosmetics.getRegistry(), state.baseSkin, attachments, overrides);

        Model newModel = new Model(
                player.getDisplayName() + "_TalaniaCosmetics",
                model.getScale(),
                model.getRandomAttachmentIds(),
                attachments.toArray(new ModelAttachment[0]),
                model.getBoundingBox(),
                model.getModel(),
                model.getTexture(),
                model.getGradientSet(),
                model.getGradientId(),
                model.getEyeHeight(),
                model.getCrouchOffset(),
                model.getSittingOffset(),
                model.getSleepingOffset(),
                model.getAnimationSetMap(),
                model.getCamera(),
                model.getLight(),
                model.getParticles(),
                model.getTrails(),
                model.getPhysicsValues(),
                model.getDetailBoxes(),
                model.getPhobia(),
                model.getPhobiaModelAssetId()
        );

        store.replaceComponent(ref, ModelComponent.getComponentType(), new ModelComponent(newModel));
        PlayerSkinComponent refreshed = new PlayerSkinComponent(state.baseSkin);
        refreshed.setNetworkOutdated();
        store.replaceComponent(ref, PlayerSkinComponent.getComponentType(), refreshed);
    }

    private void restoreBaseAttachments(CosmeticRegistry registry, PlayerSkin baseSkin,
                                        List<ModelAttachment> attachments, Map<String, Boolean> overrides) {
        if (registry == null || baseSkin == null) {
            return;
        }
        String gradientId = skinGradientId(baseSkin);
        String[] bodyCharacteristicParts = splitParts(baseSkin.bodyCharacteristic);
        if (bodyCharacteristicParts.length > 0) {
            var bodyCharacteristic = registry.getBodyCharacteristics().get(bodyCharacteristicParts[0]);
            if (bodyCharacteristic != null) {
                attachments.add(ModelUtils.resolveAttachment(bodyCharacteristic, bodyCharacteristicParts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Beards") && baseSkin.facialHair != null) {
            String[] parts = splitParts(baseSkin.facialHair);
            var facialHairs = registry.getFacialHairs().get(parts[0]);
            if (facialHairs != null) {
                attachments.add(ModelUtils.resolveAttachment(facialHairs, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Ears") && baseSkin.ears != null) {
            String[] parts = splitParts(baseSkin.ears);
            var ears = registry.getEars().get(parts[0]);
            if (ears != null) {
                attachments.add(ModelUtils.resolveAttachment(ears, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Eyebrows") && baseSkin.eyebrows != null) {
            String[] parts = splitParts(baseSkin.eyebrows);
            var eyebrows = registry.getEyebrows().get(parts[0]);
            if (eyebrows != null) {
                attachments.add(ModelUtils.resolveAttachment(eyebrows, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Eyes") && baseSkin.eyes != null) {
            String[] parts = splitParts(baseSkin.eyes);
            var eyes = registry.getEyes().get(parts[0]);
            if (eyes != null) {
                attachments.add(ModelUtils.resolveAttachment(eyes, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Faces") && baseSkin.face != null) {
            String[] parts = splitParts(baseSkin.face);
            var faces = registry.getFaces().get(parts[0]);
            if (faces != null) {
                attachments.add(ModelUtils.resolveAttachment(faces, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Mouths") && baseSkin.mouth != null) {
            String[] parts = splitParts(baseSkin.mouth);
            var mouths = registry.getMouths().get(parts[0]);
            if (mouths != null) {
                attachments.add(ModelUtils.resolveAttachment(mouths, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Haircuts") && baseSkin.haircut != null) {
            String[] parts = splitParts(baseSkin.haircut);
            var haircuts = registry.getHaircuts().get(parts[0]);
            if (haircuts != null) {
                attachments.add(ModelUtils.resolveAttachment(haircuts, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Capes") && baseSkin.cape != null) {
            String[] parts = splitParts(baseSkin.cape);
            var capes = registry.getCapes().get(parts[0]);
            if (capes != null) {
                attachments.add(ModelUtils.resolveAttachment(capes, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Face_Accessories") && baseSkin.faceAccessory != null) {
            String[] parts = splitParts(baseSkin.faceAccessory);
            var faceAccessories = registry.getFaceAccessories().get(parts[0]);
            if (faceAccessories != null) {
                attachments.add(ModelUtils.resolveAttachment(faceAccessories, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Gloves") && baseSkin.gloves != null) {
            String[] parts = splitParts(baseSkin.gloves);
            var gloves = registry.getGloves().get(parts[0]);
            if (gloves != null) {
                attachments.add(ModelUtils.resolveAttachment(gloves, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Head") && baseSkin.headAccessory != null) {
            String[] parts = splitParts(baseSkin.headAccessory);
            var headAccessories = registry.getHeadAccessories().get(parts[0]);
            if (headAccessories != null) {
                attachments.add(ModelUtils.resolveAttachment(headAccessories, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Overpants") && baseSkin.overpants != null) {
            String[] parts = splitParts(baseSkin.overpants);
            var overpants = registry.getOverpants().get(parts[0]);
            if (overpants != null) {
                attachments.add(ModelUtils.resolveAttachment(overpants, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Overtops") && baseSkin.overtop != null) {
            String[] parts = splitParts(baseSkin.overtop);
            var overtops = registry.getOvertops().get(parts[0]);
            if (overtops != null) {
                attachments.add(ModelUtils.resolveAttachment(overtops, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Pants") && baseSkin.pants != null) {
            String[] parts = splitParts(baseSkin.pants);
            var pants = registry.getPants().get(parts[0]);
            if (pants != null) {
                attachments.add(ModelUtils.resolveAttachment(pants, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Shoes") && baseSkin.shoes != null) {
            String[] parts = splitParts(baseSkin.shoes);
            var shoes = registry.getShoes().get(parts[0]);
            if (shoes != null) {
                attachments.add(ModelUtils.resolveAttachment(shoes, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Undertops") && baseSkin.undertop != null) {
            String[] parts = splitParts(baseSkin.undertop);
            var undertops = registry.getUndertops().get(parts[0]);
            if (undertops != null) {
                attachments.add(ModelUtils.resolveAttachment(undertops, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Underwears") && baseSkin.underwear != null) {
            String[] parts = splitParts(baseSkin.underwear);
            var underwear = registry.getUnderwear().get(parts[0]);
            if (underwear != null) {
                attachments.add(ModelUtils.resolveAttachment(underwear, parts, gradientId));
            }
        }

        if (!isOverridden(overrides, "Ears_Accessories") && baseSkin.earAccessory != null) {
            String[] parts = splitParts(baseSkin.earAccessory);
            var earAccessories = registry.getEarAccessories().get(parts[0]);
            if (earAccessories != null) {
                attachments.add(ModelUtils.resolveAttachment(earAccessories, parts, gradientId));
            }
        }

        if (baseSkin.skinFeature != null) {
            String[] parts = splitParts(baseSkin.skinFeature);
            var skinFeatures = registry.getSkinFeatures().get(parts[0]);
            if (skinFeatures != null) {
                attachments.add(ModelUtils.resolveAttachment(skinFeatures, parts, gradientId));
            }
        }
    }

    private static String resolveGradientId(CosmeticsModule cosmetics, PlayerSkin baseSkin, String gradientSet) {
        if (gradientSet == null || gradientSet.isEmpty()) {
            return "";
        }
        if ("Skin".equalsIgnoreCase(gradientSet) && baseSkin != null) {
            String gradientId = skinGradientId(baseSkin);
            if (!gradientId.isEmpty()) {
                return gradientId;
            }
        }
        if (cosmetics == null || cosmetics.getRegistry() == null) {
            return "";
        }
        PlayerSkinGradientSet set = cosmetics.getRegistry().getGradientSets().get(gradientSet);
        if (set == null || set.getGradients() == null || set.getGradients().isEmpty()) {
            return "";
        }
        return set.getGradients().keySet().iterator().next();
    }

    private static boolean isOverridden(Map<String, Boolean> overrides, String slot) {
        if (overrides == null || slot == null) {
            return false;
        }
        String normalized = normalizeSlot(slot);
        return overrides.getOrDefault(normalized, false);
    }

    private static String skinGradientId(PlayerSkin baseSkin) {
        if (baseSkin == null || baseSkin.bodyCharacteristic == null) {
            return "";
        }
        String[] parts = baseSkin.bodyCharacteristic.split("\\.");
        return parts.length > 1 ? parts[1] : "";
    }

    private PlayerSkin readCurrentSkin(Store<EntityStore> store, Ref<EntityStore> ref, UUID playerId) {
        PlayerSkinComponent skinComponent =
                (PlayerSkinComponent) store.getComponent(ref, PlayerSkinComponent.getComponentType());
        if (skinComponent == null || skinComponent.getPlayerSkin() == null) {
            LOG.warning("Failed to capture base skin for " + playerId + ": missing PlayerSkinComponent.");
            return null;
        }
        return skinComponent.getPlayerSkin();
    }

    private static String[] splitParts(String value) {
        if (value == null || value.isBlank()) {
            return new String[] { "" };
        }
        String[] parts = value.split("\\.");
        return parts.length == 0 ? new String[] { value } : parts;
    }

    private static String normalizeSlot(String slot) {
        if (slot == null) {
            return "";
        }
        String cleaned = slot.trim().replace(' ', '_').replace('-', '_');
        String lower = cleaned.toLowerCase(Locale.ROOT);
        switch (lower) {
            case "faceaccessories":
            case "face_accessory":
            case "faceaccessory":
            case "face_accessories":
            case "face_details":
                return "Face_Accessories";
            case "earsaccessories":
            case "ears_accessory":
            case "earaccessories":
            case "ear_accessory":
            case "ear_accessories":
            case "ears_accessories":
                return "Ears_Accessories";
            case "mouth":
                return "Mouths";
            case "face":
                return "Faces";
            case "bodycharacteristics":
            case "body_characteristics":
                return "BodyCharacteristics";
            default:
                return cleaned.isBlank() ? "" : capitalize(cleaned);
        }
    }

    private static String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        if (value.contains("_")) {
            String[] parts = value.split("_");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(capitalizeSingle(parts[i]));
            }
            return builder.toString();
        }
        return capitalizeSingle(value);
    }

    private static String capitalizeSingle(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static final class PlayerCosmeticState {
        private PlayerSkin baseSkin;
        private List<String> overrides = new ArrayList<>();
    }
}
