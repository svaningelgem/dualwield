package com.ranull.dualwield.events;

import com.ranull.dualwield.containers.BlockBreakData;
import com.ranull.dualwield.managers.WieldManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Events implements Listener {
    private WieldManager wieldManager;

    public Events(WieldManager wieldManager) {
        this.wieldManager = wieldManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

            if (player.getGameMode() == GameMode.SURVIVAL && itemInOffHand != null && event.getClickedBlock() != null) {
                Block block = event.getClickedBlock();

                BlockBreakData blockBreakData;

                // Get blockBreakData
                if (!wieldManager.hasBreakData(block)) {
                    blockBreakData = new BlockBreakData(block, player, itemInOffHand, new Random().nextInt(2000));

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
                    wieldManager.blockCrackAnimation(blockBreakData, -1);
                    wieldManager.removeBreakData(blockBreakData);
                }
            }

            // Hand animation
            if (itemInMainHand.getType() == Material.AIR && itemInOffHand.getType() == Material.AIR) {
                wieldManager.getNMS().mainHandAnimation(player);
            } else {
                wieldManager.getNMS().offHandAnimation(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            Player player = event.getPlayer();

            wieldManager.attackEntity(event.getPlayer(), event.getRightClicked());

            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            ItemStack itemInOffHand = player.getInventory().getItemInOffHand();

            // Hand animation
            if (itemInMainHand.getType() == Material.AIR && itemInOffHand.getType() == Material.AIR) {
                wieldManager.getNMS().mainHandAnimation(player);
            } else {
                wieldManager.getNMS().offHandAnimation(player);
            }
        }
    }
}
