package com.ranull.dualwield.commands;

import com.ranull.dualwield.DualWield;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DualWieldCommand implements CommandExecutor {
    private final DualWield plugin;

    public DualWieldCommand(DualWield plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String version = "2.3";
        String author = "Ranull";

        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + "DualWield " + ChatColor.GRAY + "v" + version);
            sender.sendMessage(
                    ChatColor.GRAY + "/dualwield " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Plugin info");

            if (sender.hasPermission("dualwield.reload")) {
                sender.sendMessage(ChatColor.GRAY + "/dualwield reload " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET
                        + " Reload plugin");
            }

            sender.sendMessage(ChatColor.AQUA + "Author: " + ChatColor.GRAY + author);
        } else if (args[0].equals("reload")) {
            if (sender.hasPermission("dualwield.reload")) {
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET
                        + "Reloaded config file!");
            } else {
                sender.sendMessage(ChatColor.AQUA + "\uD83D\uDDE1" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET
                        + "No Permission!");
            }
        }

        return true;
    }
}
