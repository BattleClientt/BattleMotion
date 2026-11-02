package me.vexmc.mental.v5.command;

import java.util.List;
import me.vexmc.mental.v5.MentalPluginV5;
import me.vexmc.mental.v5.gui.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code /mental} executor (spec §13). {@code reload} re-reads the
 * configuration (permission {@code mental.command.reload}); {@code debug subscribe}
 * toggles streaming verbose debug lines to the caller's chat (permission {@code
 * battlemotion.command.debug}); a bare {@code /mental} from a permitted player ({@code
 * mental.command.use}) opens the in-game management menu, and from the console
 * prints the reload hint. plugin.yml already declares the command and its
 * permissions; this only supplies the executor.
 */
public final class MentalCommand implements CommandExecutor {

    private static final String RELOAD_PERMISSION = "battlemotion.command.reload";
    private static final String USE_PERMISSION = "battlemotion.command.use";
    private static final String DEBUG_PERMISSION = "battlemotion.command.debug";
    /** Legacy upstream (Mental) nodes — still honoured so existing permission grants keep working. */
    private static final String LEGACY_RELOAD_PERMISSION = "mental.command.reload";
    private static final String LEGACY_USE_PERMISSION = "mental.command.use";
    private static final String LEGACY_DEBUG_PERMISSION = "mental.command.debug";

    private final MentalPluginV5 plugin;
    private final MenuManager menus;

    public MentalCommand(@NotNull MentalPluginV5 plugin, @NotNull MenuManager menus) {
        this.plugin = plugin;
        this.menus = menus;
    }

    private static boolean allowed(
            @NotNull CommandSender sender, @NotNull String node, @NotNull String legacyNode) {
        return sender.hasPermission(node) || sender.hasPermission(legacyNode);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!allowed(sender, RELOAD_PERMISSION, LEGACY_RELOAD_PERMISSION)) {
                sender.sendMessage("§cYou lack the permission " + RELOAD_PERMISSION + ".");
                return true;
            }
            List<String> issues = plugin.reloadAll();
            if (issues.isEmpty()) {
                sender.sendMessage("§aBattle Motion configuration reloaded.");
            } else {
                sender.sendMessage("§eBattle Motion configuration reloaded with "
                        + issues.size() + " warning(s):");
                for (String issue : issues) {
                    sender.sendMessage("§7 - " + issue);
                }
            }
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
            return handleDebug(sender, args);
        }

        // A bare /mental: a player opens the management menu; the console reloads.
        if (sender instanceof Player player) {
            if (!allowed(player, USE_PERMISSION, LEGACY_USE_PERMISSION)) {
                player.sendMessage("§cYou lack the permission " + USE_PERMISSION + ".");
                return true;
            }
            menus.openDashboard(player);
            return true;
        }

        sender.sendMessage("§7Battle Motion v" + plugin.getDescription().getVersion()
                + " — the management menu is in-game only. Use §f/battlemotion reload§7 to reload the configuration.");
        return true;
    }

    /**
     * {@code /mental debug subscribe} — toggles routing verbose debug lines to the
     * caller's own chat (the player-facing sink). Player-only (the lines are chat
     * output) and permission-gated; the subscription also clears on quit.
     */
    private boolean handleDebug(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§7/battlemotion debug subscribe streams debug output to a player's chat — run it in-game.");
            return true;
        }
        if (!allowed(player, DEBUG_PERMISSION, LEGACY_DEBUG_PERMISSION)) {
            player.sendMessage("§cYou lack the permission " + DEBUG_PERMISSION + ".");
            return true;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("subscribe")) {
            player.sendMessage("§7Usage: §f/battlemotion debug subscribe§7 — toggle streaming debug lines to your chat.");
            return true;
        }
        boolean nowSubscribed = plugin.playerDebugSink().toggle(player);
        if (nowSubscribed) {
            player.sendMessage("§aNow streaming Battle Motion debug output to your chat. "
                    + "Run it again to stop; it clears automatically when you quit.");
            // A subscription is silent until the master switch and at least one
            // channel are live — say so, so an admin is not left wondering.
            if (!plugin.snapshot().debug().enabled() || plugin.snapshot().debug().categories().isEmpty()) {
                player.sendMessage("§7No debug channels are active yet — enable them in §f/battlemotion§7 › Debug.");
            }
        } else {
            player.sendMessage("§eStopped streaming Battle Motion debug output to your chat.");
        }
        return true;
    }
}
