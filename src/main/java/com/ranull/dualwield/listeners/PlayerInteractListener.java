package com.ranull.dualwield.listeners;

import com.ranull.dualwield.data.BlockBreakData;
import com.ranull.dualwield.managers.WieldManager;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
    private WieldManager wieldManager;

    public PlayerInteractListener(WieldManager wieldManager) {
        this.wieldManager = wieldManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

        if (!event.isCancelled()
                && event.getClickedBlock() != null
                && itemInOffHand.getAmount() != 0
                && player.hasPermission("dualwield.mine")
                && player.getGameMode() == GameMode.SURVIVAL
                && event.getHand() == EquipmentSlot.OFF_HAND) {

            if (!event.isCancelled()
                    && event.getClickedBlock() != null
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
}
