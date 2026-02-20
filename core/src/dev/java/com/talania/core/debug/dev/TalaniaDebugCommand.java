package com.talania.core.debug.dev;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Dev-only /talania debug command.
 */
public final class TalaniaDebugCommand extends AbstractPlayerCommand {

    public TalaniaDebugCommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(@Nonnull CommandContext context,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        String[] args = parseArgs(context.getInputString());
        if (args.length < 2 || !"debug".equalsIgnoreCase(args[1])) {
            showHelp(context);
            return;
        }

        openMenu(context, playerRef, ref, store);
    }

    private void showHelp(CommandContext context) {
        context.sendMessage(Message.raw("Only /talania debug is available."));
        context.sendMessage(Message.raw("Usage: /talania debug"));
    }

    private void openMenu(CommandContext context, PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (playerRef == null) {
            context.sendMessage(Message.raw("Unable to resolve player ref."));
            return;
        }
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            context.sendMessage(Message.raw("Unable to open debug menu right now."));
            return;
        }
        player.getPageManager().openCustomPage(ref, store, new TalaniaDebugMenuPage(playerRef));
    }

    private String[] parseArgs(String input) {
        if (input == null || input.isBlank()) {
            return new String[0];
        }
        return input.trim().split("\\s+");
    }
}
