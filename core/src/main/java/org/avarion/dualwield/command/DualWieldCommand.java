package org.avarion.dualwield.command;

import org.avarion.dualwield.DualWield;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DualWieldCommand implements CommandExecutor, TabExecutor {
    private final DualWield plugin;

    public DualWieldCommand(DualWield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String string, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA
                    + "DualWield " + ChatColor.DARK_GRAY + "v" + plugin.getDescription().getVersion());
            commandSender.sendMessage(
                    ChatColor.AQUA + "/dualwield " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Plugin info");

            if (commandSender.hasPermission("dualwield.reload")) {
                commandSender.sendMessage(ChatColor.AQUA + "/dualwield reload " + ChatColor.DARK_GRAY + "-"
                        + ChatColor.RESET + " Reload plugin");
            }

            commandSender.sendMessage(ChatColor.DARK_GRAY + "Author: " + ChatColor.AQUA + "Ranull");
        } else if (args[0].equals("reload")) {
            if (commandSender.hasPermission("dualwield.reload")) {
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                commandSender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » "
                        + ChatColor.RESET + "Reloaded config file.");
            } else {
                commandSender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » "
                        + ChatColor.RESET + "No permission.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
                                      @NotNull String string, @NotNull String[] args) {
        if (commandSender.hasPermission("dualwield.reload")) {
            return Collections.singletonList("reload");
        }

        return new ArrayList<>();
    }
}
