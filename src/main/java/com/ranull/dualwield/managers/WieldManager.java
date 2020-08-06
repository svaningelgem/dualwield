package com.ranull.dualwield.managers;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.containers.BlockBreakData;
import com.ranull.dualwield.nms.NMS;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WieldManager {
    private DualWield plugin;
    private NMS nms;
    private Map<Block, BlockBreakData> blockBreakDataList = new HashMap<>();

    public WieldManager(DualWield plugin, NMS nms) {
        this.plugin = plugin;
        this.nms = nms;
    }

    public NMS getNMS() {
        return nms;
    }

    public void runBlockBreakTask(BlockBreakData blockBreakData) {
        final List<Player> nearbyPlayers = getNearbyPlayers(blockBreakData.getBlock().getLocation(), 20);
        float blockHardness = nms.getBlockHardness(blockBreakData.getBlock());

        if (blockHardness == 0.0) {
            // Instant break block
            for (Player nearbyPlayer : nearbyPlayers) {
                blockCrackAnimation(blockBreakData, nearbyPlayer);
            }
            breakBlock(blockBreakData);
        } else if (blockHardness > 0.0) {
            // Timed break tool
            float toolStrength = nms.getToolStrength(blockBreakData.getBlock(), blockBreakData.getItemInOffHand());

            // Base timer
            float timer = (blockBreakData.getHardness() / (toolStrength * 6)) * 20;

            int crackAmount = 10;

            if (toolStrength > 1) {
                // Correct tool enchantment check
                if (blockBreakData.getItemInOffHand().hasItemMeta()
                        && blockBreakData.getItemInOffHand().getItemMeta().hasEnchant(Enchantment.DIG_SPEED)) {
                    int enchantmentLevel = blockBreakData.getItemInOffHand().getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED);
                    int enchantmentLevelCounter = 0;

                    // For each enchantment level subtract 1 from the required cracks to break a block
                    while (enchantmentLevelCounter <= enchantmentLevel) {
                        crackAmount--;
                        enchantmentLevelCounter++;
                    }
                }
            } else {
                // Wrong tool timer debuff
                timer = timer * (blockBreakData.getHardness() * 2);
            }

            // Swimming debuff
            if (blockBreakData.getPlayer().getLocation().add(0, 1, 0).getBlock().getType().equals(Material.WATER)) {
                timer = timer * 5;
            }

            // Vehicle debuff
            if (blockBreakData.getPlayer().getVehicle() != null) {
                timer = timer * 5;
            }

            int finalCrackAmount = crackAmount;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (blockBreakData.getCrackAmount() < finalCrackAmount) {
                        if ((System.currentTimeMillis() - blockBreakData.getLastMineTime()) < 230) {
                            // Continue because player is mining
                            for (Player nearbyPlayer : nearbyPlayers) {
                                blockCrackAnimation(blockBreakData, nearbyPlayer);
                            }
                            blockBreakData.addCrackAmount();
                        } else {
                            // Cancel because player stopped mining
                            for (Player nearbyPlayer : nearbyPlayers) {
                                blockCrackAnimation(blockBreakData, nearbyPlayer, -1);
                            }
                            removeBreakData(blockBreakData);

                            // Cancel runnable
                            cancel();
                        }
                    } else {
                        // Break block mine finished
                        for (Player nearbyPlayer : nearbyPlayers) {
                            blockCrackAnimation(blockBreakData, nearbyPlayer, -1);
                        }
                        blockCrackParticle(blockBreakData);

                        // Break block on the main thread
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                breakBlock(blockBreakData);
                                removeBreakData(blockBreakData);
                            }
                        }.runTask(plugin);

                        // Cancel runnable
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0L, (long) timer);
        }
    }

    public List<Player> getNearbyPlayers(Location location, int range) {
        final List<Player> nearbyPlayers = new ArrayList<>();

        for (Entity entity : location.getWorld().getNearbyEntities(location, range, range, range)) {
            if (entity instanceof Player) {
                nearbyPlayers.add((Player) entity);
            }
        }

        return nearbyPlayers;
    }

    public boolean hasBreakData(Block block) {
        return blockBreakDataList.containsKey(block);
    }

    public BlockBreakData getBreakData(Block block) {
        if (blockBreakDataList.containsKey(block)) {
            return blockBreakDataList.get(block);
        }

        return null;
    }

    public void addBreakData(BlockBreakData blockBreakData) {
        blockBreakDataList.put(blockBreakData.getBlock(), blockBreakData);
    }

    public void removeBreakData(BlockBreakData blockBreakData) {
        blockBreakDataList.remove(blockBreakData.getBlock());
    }

    public void breakBlock(BlockBreakData blockBreakData) {
        Player player = blockBreakData.getPlayer();

        ItemStack mainHandItem = player.getInventory().getItemInMainHand().clone();
        nms.setItemInMainHand(player, blockBreakData.getItemInOffHand());

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(blockBreakData.getBlock(), blockBreakData.getPlayer());
        plugin.getServer().getPluginManager().callEvent(blockBreakEvent);

        nms.setItemInMainHand(player, mainHandItem);
        player.updateInventory();

        if (!blockBreakEvent.isCancelled()) {
            nms.damageItem(blockBreakData.getItemInOffHand(), player);

            blockBreakData.getBlock().getWorld().playEffect(blockBreakData.getBlock().getLocation(),
                    Effect.STEP_SOUND, blockBreakData.getBlock().getType());

            blockBreakData.getBlock().breakNaturally(blockBreakData.getItemInOffHand());
        }
    }

    public void blockHitSound(BlockBreakData blockBreakData) {
        Sound sound = nms.getBreakSound(blockBreakData.getBlock());

        blockBreakData.getBlock().getWorld().playSound(blockBreakData.getBlock().getLocation(), sound, 0.50F, 0.75F);
    }

    public void blockCrackAnimation(BlockBreakData blockBreakData, Player player) {
        blockCrackAnimation(blockBreakData, player, blockBreakData.getCrackAmount());
    }

    public void blockCrackAnimation(BlockBreakData blockBreakData, Player player, int stage) {
        nms.blockBreakAnimation(player, blockBreakData.getBlock(), blockBreakData.getAnimationID(), stage);
    }

    public void blockCrackParticle(BlockBreakData blockBreakData) {
        nms.blockCrackParticle(blockBreakData.getBlock());
    }
}
