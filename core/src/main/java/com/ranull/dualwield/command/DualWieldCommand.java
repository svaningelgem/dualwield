package com.ranull.dualwield.command;

import com.ranull.dualwield.DualWield;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class DualWieldCommand implements CommandExecutor {
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
                    ChatColor.GRAY + "DualWield " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Plugin info");

            if (commandSender.hasPermission("dualwield.reload")) {
                commandSender.sendMessage(ChatColor.GRAY + "/dualwield reload " + ChatColor.DARK_GRAY + "-"
                        + ChatColor.RESET + " Reload plugin");
            }

            commandSender.sendMessage(ChatColor.DARK_GRAY + "Author: " + ChatColor.GRAY + "Ranull");
        } else if (args[0].equals("reload")) {
            if (commandSender.hasPermission("dualwield.reload")) {
                plugin.reloadConfig();
                commandSender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » "
                        + ChatColor.RESET + "Reloaded config file!");
            } else {
                commandSender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » "
                        + ChatColor.RESET + "No Permission!");
            }
        }

        return true;
    }
}
