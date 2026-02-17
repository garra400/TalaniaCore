package com.talania.core.debug.dev;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.talania.core.debug.DebugCategory;
import com.talania.core.debug.TalaniaDebug;
import com.talania.core.debug.combat.CombatLogEntry;
import com.talania.core.debug.combat.CombatLogFormatter;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Dev-only /talania debug command.
 */
public final class TalaniaDebugCommand extends CommandBase {

    public TalaniaDebugCommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        String[] args = parseArgs(context.getInputString());
        if (args.length < 2 || !"debug".equalsIgnoreCase(args[1])) {
            showHelp(context);
            return;
        }

        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("This command can only be used by a player."));
            return;
        }

        Player player = (Player) context.senderAs(Player.class);
        PlayerRef playerRef = Universe.get().getPlayer(player.getUuid());
        if (playerRef == null) {
            context.sendMessage(Message.raw("Unable to resolve player ref."));
            return;
        }

        if (args.length < 3 || "help".equalsIgnoreCase(args[2])) {
            showHelp(context);
            return;
        }

        switch (args[2].toLowerCase()) {
            case "log" -> handleLog(context, playerRef, args);
            case "combatlog" -> handleCombatLog(context, playerRef, args);
            default -> showHelp(context);
        }
    }

    private void handleLog(CommandContext context, PlayerRef playerRef, String[] args) {
        if (args.length == 3 || "open".equalsIgnoreCase(args[3])) {
            openLogSettings(context, playerRef);
            return;
        }
        if ("toggle".equalsIgnoreCase(args[3]) && args.length >= 5) {
            DebugCategory category = DebugCategory.fromId(args[4]);
            if (category == null) {
                context.sendMessage(Message.raw("Unknown category: " + args[4]));
                return;
            }
            boolean enabled = TalaniaDebug.logs().toggle(playerRef.getUuid(), category);
            context.sendMessage(Message.raw(category.id() + " -> " + (enabled ? "Enabled" : "Disabled")));
            return;
        }
        showHelp(context);
    }

    private void handleCombatLog(CommandContext context, PlayerRef playerRef, String[] args) {
        if (args.length == 3 || "open".equalsIgnoreCase(args[3])) {
            openCombatLog(context, playerRef);
            return;
        }
        if ("last".equalsIgnoreCase(args[3])) {
            int limit = 10;
            if (args.length >= 5) {
                try {
                    limit = Integer.parseInt(args[4]);
                } catch (NumberFormatException ignored) {
                    context.sendMessage(Message.raw("Usage: /talania debug combatlog last [n]"));
                    return;
                }
            }
            List<CombatLogEntry> entries = TalaniaDebug.combatLog().recent(playerRef.getUuid(), limit);
            if (entries.isEmpty()) {
                context.sendMessage(Message.raw("No combat log entries yet."));
                return;
            }
            context.sendMessage(Message.raw("Combat log (last " + entries.size() + "):"));
            for (CombatLogEntry entry : entries) {
                context.sendMessage(Message.raw("- " +
                        CombatLogFormatter.summaryFor(playerRef.getUuid(), entry, null, null)));
            }
            return;
        }
        showHelp(context);
    }

    private void openLogSettings(CommandContext context, PlayerRef playerRef) {
        if (playerRef == null || playerRef.getReference() == null) {
            context.sendMessage(Message.raw("Unable to open log settings right now."));
            return;
        }
        playerRef.getReference().getStore().getExternalData().getWorld().execute(() -> {
            Player player = (Player) playerRef.getReference().getStore()
                    .getComponent(playerRef.getReference(), Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(
                        playerRef.getReference(),
                        playerRef.getReference().getStore(),
                        new TalaniaDebugLogSettingsPage(playerRef));
            }
        });
    }

    private void openCombatLog(CommandContext context, PlayerRef playerRef) {
        if (playerRef == null || playerRef.getReference() == null) {
            context.sendMessage(Message.raw("Unable to open combat log right now."));
            return;
        }
        playerRef.getReference().getStore().getExternalData().getWorld().execute(() -> {
            Player player = (Player) playerRef.getReference().getStore()
                    .getComponent(playerRef.getReference(), Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(
                        playerRef.getReference(),
                        playerRef.getReference().getStore(),
                        new TalaniaCombatLogPage(playerRef));
            }
        });
    }

    private void showHelp(CommandContext context) {
        context.sendMessage(Message.raw("Usage: /talania debug"));
        context.sendMessage(Message.raw("  /talania debug log open"));
        context.sendMessage(Message.raw("  /talania debug log toggle <category>"));
        context.sendMessage(Message.raw("  /talania debug combatlog open"));
        context.sendMessage(Message.raw("  /talania debug combatlog last [n]"));
    }

    private String[] parseArgs(String input) {
        if (input == null || input.isBlank()) {
            return new String[0];
        }
        return input.trim().split("\\s+");
    }
}
