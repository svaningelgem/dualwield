package com.ranull.dualwield.listener;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.data.BlockBreakData;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
    private final DualWield plugin;

    public PlayerInteractListener(DualWield plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            if (player.hasPermission("dualwield.mine")
                    && event.useInteractedBlock() != Event.Result.DENY && event.getClickedBlock() != null
                    && ((itemInOffHand.getAmount() > 0 && !itemInOffHand.getType().isBlock())
                    || (plugin.getConfig().getBoolean("settings.mine.hand") && itemInOffHand.getAmount() <= 0))
                    && ((player.isSneaking() && plugin.getConfig().getBoolean("settings.mine.sneaking"))
                    || (!player.isSneaking() && plugin.getConfig().getBoolean("settings.mine.standing")))
                    && plugin.getConfig().getStringList("settings.mine.gamemode")
                    .contains(player.getGameMode().toString())) {
                if ((player.getGameMode() != GameMode.CREATIVE
                        || !player.getInventory().getItemInOffHand().getType().name().contains("_SWORD"))) {
                    Block block = event.getClickedBlock();
                    BlockBreakData blockBreakData;

                    // Get blockBreakData
                    if (!plugin.getWieldManager().hasBreakData(block)) {
                        blockBreakData = plugin.getWieldManager().createBlockBreakData(block, player, itemInOffHand);

                        plugin.getWieldManager().addBreakData(blockBreakData);
                        plugin.getWieldManager().runBlockBreakTask(blockBreakData);
                    } else {
                        blockBreakData = plugin.getWieldManager().getBreakData(block);
                    }

                    // Update mining data
                    if (blockBreakData.getItemInOffHand().equals(itemInOffHand)) {
                        // Item matches
                        blockBreakData.updateLastMineTime();
                        plugin.getWieldManager().blockHitSound(blockBreakData);
                    } else {
                        // Item does not match
                        for (Player nearbyPlayer : plugin.getWieldManager().getNearbyPlayers(blockBreakData
                                .getBlock().getLocation(), 20)) {
                            plugin.getWieldManager().blockCrackAnimation(blockBreakData, nearbyPlayer, -1);
                        }

                        plugin.getWieldManager().removeBreakData(blockBreakData);
                    }
                }

                plugin.getNMS().offHandAnimation(player);
            } else if (player.hasPermission("dualwield.attack")
                    && plugin.getConfig().getBoolean("settings.swing.air")
                    && ((itemInOffHand.getAmount() > 0 && !itemInOffHand.getType().isBlock())
                    || (plugin.getConfig().getBoolean("settings.attack.hand") && itemInOffHand.getAmount() <= 0))
                    && ((player.isSneaking() && plugin.getConfig().getBoolean("settings.attack.sneaking"))
                    || (!player.isSneaking() && plugin.getConfig().getBoolean("settings.attack.standing")))
                    && plugin.getConfig().getStringList("settings.attack.gamemode")
                    .contains(player.getGameMode().toString())) {
                plugin.getNMS().offHandAnimation(player);
            }
        }
    }
}
