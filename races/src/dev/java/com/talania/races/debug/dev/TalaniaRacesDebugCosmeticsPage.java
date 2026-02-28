package com.talania.races.debug.dev;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticRegistry;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.races.TalaniaRacesPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class TalaniaRacesDebugCosmeticsPage extends InteractiveCustomUIPage {
    private static final int MAX_LABEL_LENGTH = 60;
    private static final int MAX_TRIES = 10;
    private final PlayerRef playerRef;
    private final TalaniaRacesPlugin plugin;
    private final Random random = new Random();

    public TalaniaRacesDebugCosmeticsPage(PlayerRef playerRef, TalaniaRacesPlugin plugin) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaRacesDebugCosmeticsEventData.CODEC);
        this.playerRef = playerRef;
        this.plugin = plugin;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaRacesDebugCosmeticsPage.ui");
        bindEvents(eventBuilder);
        applyState(ref, store, commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaRacesDebugCosmeticsEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        if ("Return".equals(eventData.action)) {
            TalaniaRacesDebugMenuPage.open(playerRef, ref, store, plugin);
            return;
        }
        if ("Randomize".equals(eventData.action) && eventData.value != null) {
            SkinSlot slot = SkinSlot.fromId(eventData.value);
            if (slot != null) {
                applyRandomCosmetic(ref, store, slot);
            }
            refresh(ref, store);
        }
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ReturnButton",
                new EventData().append("Action", "Return"), false);
        for (SkinSlot slot : SkinSlot.values()) {
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Slot" + slot.uiId + "Button",
                    new EventData().append("Action", "Randomize").append("Value", slot.id), false);
        }
    }

    private void applyState(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Races: Cosmetics Debug");
        commandBuilder.set("#SubtitleLabel.Text", "Shows current cosmetics and randomizes per slot.");

        PlayerSkin skin = getOrCreateSkin(ref, store);
        for (SkinSlot slot : SkinSlot.values()) {
            String current = getSlotValue(skin, slot);
            commandBuilder.set("#Slot" + slot.uiId + "Name.Text", slot.label);
            commandBuilder.set("#Slot" + slot.uiId + "Value.Text", "Current: " + formatValue(current));
            commandBuilder.set("#Slot" + slot.uiId + "Button.Text", "Randomize");
        }
    }

    private void applyRandomCosmetic(Ref<EntityStore> ref, Store<EntityStore> store, SkinSlot slot) {
        CosmeticsModule cosmetics = CosmeticsModule.get();
        if (cosmetics == null) {
            return;
        }
        CosmeticRegistry registry = cosmetics.getRegistry();
        if (registry == null) {
            return;
        }
        PlayerSkin currentSkin = getOrCreateSkin(ref, store);
        String currentValue = getSlotValue(currentSkin, slot);
        List<String> candidates = new ArrayList<>(catalogFor(registry, slot).keySet());
        candidates.removeIf(id -> id == null || id.isBlank());
        if (candidates.isEmpty()) {
            return;
        }
        java.util.Collections.shuffle(candidates, random);
        int tries = 0;
        for (String candidate : candidates) {
            if (candidate.equals(currentValue)) {
                continue;
            }
            PlayerSkin updated = new PlayerSkin(currentSkin);
            setSlotValue(updated, slot, candidate);
            if (isValidSkin(cosmetics, updated)) {
                setPlayerSkin(ref, store, updated);
                return;
            }
            tries++;
            if (tries >= MAX_TRIES) {
                break;
            }
        }
    }

    private boolean isValidSkin(CosmeticsModule cosmetics, PlayerSkin skin) {
        try {
            cosmetics.validateSkin(skin);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void setPlayerSkin(Ref<EntityStore> ref, Store<EntityStore> store, PlayerSkin skin) {
        if (ref == null || store == null || skin == null) {
            return;
        }
        PlayerSkinComponent component = new PlayerSkinComponent(skin);
        store.addComponent(ref, PlayerSkinComponent.getComponentType(), component);
        component.setNetworkOutdated();
    }

    private PlayerSkin getOrCreateSkin(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerSkinComponent component =
                (PlayerSkinComponent) store.getComponent(ref, PlayerSkinComponent.getComponentType());
        if (component != null && component.getPlayerSkin() != null) {
            return component.getPlayerSkin();
        }
        CosmeticsModule cosmetics = CosmeticsModule.get();
        PlayerSkin skin = cosmetics != null ? cosmetics.generateRandomSkin(random) : new PlayerSkin();
        setPlayerSkin(ref, store, skin);
        return skin;
    }

    private Map<String, ?> catalogFor(CosmeticRegistry registry, SkinSlot slot) {
        return switch (slot) {
            case BODY_CHARACTERISTIC -> registry.getBodyCharacteristics();
            case UNDERWEAR -> registry.getUnderwear();
            case FACE -> registry.getFaces();
            case EYES -> registry.getEyes();
            case EARS -> registry.getEars();
            case MOUTH -> registry.getMouths();
            case EYEBROWS -> registry.getEyebrows();
            case HAIRCUT -> registry.getHaircuts();
            case FACIAL_HAIR -> registry.getFacialHairs();
            case PANTS -> registry.getPants();
            case OVERPANTS -> registry.getOverpants();
            case UNDERTOP -> registry.getUndertops();
            case OVERTOP -> registry.getOvertops();
            case SHOES -> registry.getShoes();
            case HEAD_ACCESSORY -> registry.getHeadAccessories();
            case FACE_ACCESSORY -> registry.getFaceAccessories();
            case EAR_ACCESSORY -> registry.getEarAccessories();
            case SKIN_FEATURE -> registry.getSkinFeatures();
            case GLOVES -> registry.getGloves();
            case CAPE -> registry.getCapes();
        };
    }

    private String getSlotValue(PlayerSkin skin, SkinSlot slot) {
        return switch (slot) {
            case BODY_CHARACTERISTIC -> skin.bodyCharacteristic;
            case UNDERWEAR -> skin.underwear;
            case FACE -> skin.face;
            case EYES -> skin.eyes;
            case EARS -> skin.ears;
            case MOUTH -> skin.mouth;
            case EYEBROWS -> skin.eyebrows;
            case HAIRCUT -> skin.haircut;
            case FACIAL_HAIR -> skin.facialHair;
            case PANTS -> skin.pants;
            case OVERPANTS -> skin.overpants;
            case UNDERTOP -> skin.undertop;
            case OVERTOP -> skin.overtop;
            case SHOES -> skin.shoes;
            case HEAD_ACCESSORY -> skin.headAccessory;
            case FACE_ACCESSORY -> skin.faceAccessory;
            case EAR_ACCESSORY -> skin.earAccessory;
            case SKIN_FEATURE -> skin.skinFeature;
            case GLOVES -> skin.gloves;
            case CAPE -> skin.cape;
        };
    }

    private void setSlotValue(PlayerSkin skin, SkinSlot slot, String value) {
        switch (slot) {
            case BODY_CHARACTERISTIC -> skin.bodyCharacteristic = value;
            case UNDERWEAR -> skin.underwear = value;
            case FACE -> skin.face = value;
            case EYES -> skin.eyes = value;
            case EARS -> skin.ears = value;
            case MOUTH -> skin.mouth = value;
            case EYEBROWS -> skin.eyebrows = value;
            case HAIRCUT -> skin.haircut = value;
            case FACIAL_HAIR -> skin.facialHair = value;
            case PANTS -> skin.pants = value;
            case OVERPANTS -> skin.overpants = value;
            case UNDERTOP -> skin.undertop = value;
            case OVERTOP -> skin.overtop = value;
            case SHOES -> skin.shoes = value;
            case HEAD_ACCESSORY -> skin.headAccessory = value;
            case FACE_ACCESSORY -> skin.faceAccessory = value;
            case EAR_ACCESSORY -> skin.earAccessory = value;
            case SKIN_FEATURE -> skin.skinFeature = value;
            case GLOVES -> skin.gloves = value;
            case CAPE -> skin.cape = value;
        }
    }

    private void refresh(Ref<EntityStore> ref, Store<EntityStore> store) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        bindEvents(eventBuilder);
        applyState(ref, store, commandBuilder);
        sendUpdate(commandBuilder, eventBuilder, false);
    }

    private String formatValue(String value) {
        if (value == null || value.isBlank()) {
            return "None";
        }
        if (value.length() <= MAX_LABEL_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LABEL_LENGTH - 3) + "...";
    }

    public static void open(PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store,
                            TalaniaRacesPlugin plugin) {
        if (playerRef == null || ref == null || store == null || plugin == null) {
            return;
        }
        store.getExternalData().getWorld().execute(() -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store,
                        new TalaniaRacesDebugCosmeticsPage(playerRef, plugin));
            }
        });
    }

    private enum SkinSlot {
        BODY_CHARACTERISTIC("BodyCharacteristic", "Body", "body"),
        UNDERWEAR("Underwear", "Underwear", "underwear"),
        FACE("Face", "Face", "face"),
        EYES("Eyes", "Eyes", "eyes"),
        EARS("Ears", "Ears", "ears"),
        MOUTH("Mouth", "Mouth", "mouth"),
        EYEBROWS("Eyebrows", "Eyebrows", "eyebrows"),
        HAIRCUT("Haircut", "Haircut", "haircut"),
        FACIAL_HAIR("FacialHair", "Facial Hair", "facial_hair"),
        PANTS("Pants", "Pants", "pants"),
        OVERPANTS("Overpants", "Overpants", "overpants"),
        UNDERTOP("Undertop", "Undertop", "undertop"),
        OVERTOP("Overtop", "Overtop", "overtop"),
        SHOES("Shoes", "Shoes", "shoes"),
        HEAD_ACCESSORY("HeadAccessory", "Head Accessory", "head_accessory"),
        FACE_ACCESSORY("FaceAccessory", "Face Accessory", "face_accessory"),
        EAR_ACCESSORY("EarAccessory", "Ear Accessory", "ear_accessory"),
        SKIN_FEATURE("SkinFeature", "Skin Feature", "skin_feature"),
        GLOVES("Gloves", "Gloves", "gloves"),
        CAPE("Cape", "Cape", "cape");

        private final String uiId;
        private final String label;
        private final String id;

        SkinSlot(String uiId, String label, String id) {
            this.uiId = uiId;
            this.label = label;
            this.id = id;
        }

        static SkinSlot fromId(String id) {
            if (id == null) {
                return null;
            }
            for (SkinSlot slot : values()) {
                if (slot.id.equalsIgnoreCase(id)) {
                    return slot;
                }
            }
            return null;
        }
    }

    public static final class TalaniaRacesDebugCosmeticsEventData {
        public static final BuilderCodec<TalaniaRacesDebugCosmeticsEventData> CODEC;
        private String action;
        private String value;

        static {
            BuilderCodec.Builder<TalaniaRacesDebugCosmeticsEventData> builder =
                    BuilderCodec.builder(TalaniaRacesDebugCosmeticsEventData.class,
                            TalaniaRacesDebugCosmeticsEventData::new);
            builder.addField(new KeyedCodec("Action", Codec.STRING),
                    (entry, s) -> entry.action = s,
                    (entry) -> entry.action);
            builder.addField(new KeyedCodec("Value", Codec.STRING),
                    (entry, s) -> entry.value = s,
                    (entry) -> entry.value);
            CODEC = builder.build();
        }
    }
}
