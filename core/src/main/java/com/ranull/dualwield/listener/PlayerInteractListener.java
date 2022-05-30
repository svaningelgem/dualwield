package com.ranull.dualwield.listener;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.data.BlockBreakData;
import org.bukkit.GameMode;
import org.bukkit.Material;
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            ItemStack itemStack = player.getInventory().getItemInOffHand();

            if (event.getClickedBlock() != null && event.useInteractedBlock() != Event.Result.DENY
                    && plugin.getDualWieldManager().shouldMine(player)) {
                Block block = event.getClickedBlock();

                if (!plugin.getConfig().getBoolean("settings.mine.correct-item")
                        || plugin.getNMS().getToolStrength(block, itemStack) > 1) {
                    if (player.getGameMode() != GameMode.CREATIVE || (!itemStack.getType().name().contains("_SWORD")
                            && !itemStack.getType().name().equals("TRIDENT"))) {
                        BlockBreakData blockBreakData;

                        // Get blockBreakData
                        if (!plugin.getDualWieldManager().hasBlockBreakData(block)) {
                            blockBreakData = plugin.getDualWieldManager().createBlockBreakData(block, player, itemStack);

                            if (blockBreakData.getPlayer().getGameMode() == GameMode.CREATIVE
                                    || blockBreakData.getHardness() == 0) {
                                plugin.getDualWieldManager().breakBlock(blockBreakData);
                            } else if (blockBreakData.getHardness() > 0) {
                                plugin.getDualWieldManager().runBlockBreak(blockBreakData);
                            }
                        } else {
                            blockBreakData = plugin.getDualWieldManager().getBlockBreakData(block);
                        }

                        // Update mining data
                        if (blockBreakData.getItemInOffHand().equals(itemStack)) {
                            // Item matches
                            blockBreakData.updateLastMineTime();
                            plugin.getDualWieldManager().blockHitSound(blockBreakData);
                        } else {
                            // Item does not match
                            for (Player nearbyPlayer : plugin.getDualWieldManager().getNearbyPlayers(blockBreakData
                                    .getBlock().getLocation(), 10)) {
                                plugin.getDualWieldManager().blockCrackAnimation(blockBreakData, nearbyPlayer, -1);
                            }

                            plugin.getDualWieldManager().removeBlockBreakData(blockBreakData);
                        }

                        if (plugin.getConfig().getBoolean("settings.mine.cancel-event")) {
                            event.setCancelled(true);
                        }

                        plugin.getNMS().handAnimation(player, event.getHand());
                    }
                }
            } else if (itemStack.getType() != Material.AIR && plugin.getDualWieldManager().shouldSwing(player)) {
                plugin.getNMS().handAnimation(player, event.getHand());
            }
        }
    }
}
