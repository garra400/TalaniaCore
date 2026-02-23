package com.talania.core.debug.dev;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.talania.core.debug.DebugModule;
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.module.TalaniaModuleRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class TalaniaDebugMenuPage extends InteractiveCustomUIPage {
    private final PlayerRef playerRef;
    private List<DebugModule> modules = List.of();

    public TalaniaDebugMenuPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, TalaniaDebugMenuEventData.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store store) {
        commandBuilder.append("Pages/TalaniaDebugMenuPage.ui");
        modules = buildModuleList();
        bindEvents(eventBuilder);
        applyState(commandBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref ref, @Nonnull Store store, @Nonnull Object data) {
        if (!(data instanceof TalaniaDebugMenuEventData eventData)) {
            return;
        }
        if (eventData.action == null) {
            return;
        }
        if ("OpenLogSettings".equals(eventData.action)) {
            openLogSettings(ref, store);
            return;
        }
        if ("OpenCombatLog".equals(eventData.action)) {
            openCombatLog(ref, store);
            return;
        }
        if ("OpenStatModifiers".equals(eventData.action)) {
            openStatModifiers(ref, store);
            return;
        }
        if ("OpenModule".equals(eventData.action) && eventData.value != null) {
            runOnWorldThread(ref, () ->
                    TalaniaModuleRegistry.get().openDebugSection(eventData.value, "main", playerRef, ref, store));
            return;
        }
    }

    private void bindEvents(UIEventBuilder eventBuilder) {
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OpenLogSettingsButton",
                new EventData().append("Action", "OpenLogSettings"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OpenCombatLogButton",
                new EventData().append("Action", "OpenCombatLog"), false);
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#OpenStatModifiersButton",
                new EventData().append("Action", "OpenStatModifiers"), false);
        for (int i = 1; i <= 4; i++) {
            int index = i - 1;
            if (index >= modules.size()) {
                continue;
            }
            DebugModule module = modules.get(index);
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Module" + i + "Button",
                    new EventData().append("Action", "OpenModule").append("Value", module.moduleId()), false);
        }
    }

    private void applyState(UICommandBuilder commandBuilder) {
        commandBuilder.set("#TitleLabel.Text", "Talania Debug");
        commandBuilder.set("#SubtitleLabel.Text", "Dev build only. Module tools and core debug.");
        for (int i = 1; i <= 4; i++) {
            int index = i - 1;
            if (index >= modules.size()) {
                commandBuilder.set("#Module" + i + "Container.Visible", false);
                continue;
            }
            DebugModule module = modules.get(index);
            commandBuilder.set("#Module" + i + "Container.Visible", true);
            commandBuilder.set("#Module" + i + "Label.Text", module.displayName());
            commandBuilder.set("#Module" + i + "Button.Text", "Open");
        }
    }

    private List<DebugModule> buildModuleList() {
        List<DebugModule> list = new ArrayList<>();
        for (DebugModule module : TalaniaDebug.registry().modules()) {
            if ("core".equalsIgnoreCase(module.moduleId())) {
                continue;
            }
            list.add(module);
        }
        return list;
    }

    private void openLogSettings(Ref ref, Store store) {
        runOnWorldThread(ref, () -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new TalaniaDebugLogSettingsPage(playerRef));
            }
        });
    }

    private void openCombatLog(Ref ref, Store store) {
        runOnWorldThread(ref, () -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new TalaniaCombatLogPage(playerRef));
            }
        });
    }

    private void openStatModifiers(Ref ref, Store store) {
        runOnWorldThread(ref, () -> {
            Player player = (Player) store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, new TalaniaDebugStatModifiersPage(playerRef));
            }
        });
    }

    private void runOnWorldThread(Ref ref, Runnable action) {
        if (ref == null || !ref.isValid() || action == null) {
            return;
        }
        com.hypixel.hytale.component.Store<EntityStore> typedStore = ref.getStore();
        typedStore.getExternalData().getWorld().execute(action);
    }

    public static final class TalaniaDebugMenuEventData {
        public static final BuilderCodec<TalaniaDebugMenuEventData> CODEC;
        private String action;
        private String value;

        static {
            BuilderCodec.Builder<TalaniaDebugMenuEventData> builder =
                    BuilderCodec.builder(TalaniaDebugMenuEventData.class, TalaniaDebugMenuEventData::new);
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
