package com.ranull.dualwield.events;

import com.ranull.dualwield.containers.BlockBreakData;
import com.ranull.dualwield.managers.WieldManager;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {
    private WieldManager wieldManager;

    public Events(WieldManager wieldManager) {
        this.wieldManager = wieldManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if (player.hasPermission("dualwield.mine") && event.getHand() == EquipmentSlot.OFF_HAND) {

            if (event.getClickedBlock() != null
                    && itemInOffHand.getAmount() != 0
                    && player.getGameMode() == GameMode.SURVIVAL) {
                Block block = event.getClickedBlock();
                BlockBreakData blockBreakData;

                // Get blockBreakData
                if (!wieldManager.hasBreakData(block)) {
                    blockBreakData = wieldManager.createBlockBreakData(block, player, itemInOffHand);

                    wieldManager.addBreakData(blockBreakData);
                    wieldManager.runBlockBreakTask(blockBreakData);
                } else {
                    blockBreakData = wieldManager.getBreakData(block);
                }

                // Update mining data
                if (blockBreakData.getItemInOffHand().equals(itemInOffHand)) {
                    // Item matches
                    blockBreakData.updateLastMineTime();
                    wieldManager.blockHitSound(blockBreakData);
                } else {
                    // Item does not match
                    for (Player nearbyPlayer : wieldManager.getNearbyPlayers(blockBreakData
                            .getBlock().getLocation(), 20)) {
                        wieldManager.blockCrackAnimation(blockBreakData, nearbyPlayer, -1);
                    }
                    wieldManager.removeBreakData(blockBreakData);
                }
            }

            wieldManager.getNMS().offHandAnimation(player);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        Entity entity = event.getRightClicked();

        if (player.hasPermission("dualwield.attack")
                && itemInOffHand.getAmount() != 0
                && event.getHand() == EquipmentSlot.OFF_HAND
                && player.getGameMode() != GameMode.SPECTATOR) {

            wieldManager.attackEntityOffHand(player, entity);
            wieldManager.getNMS().offHandAnimation(player);
        }
    }
}
