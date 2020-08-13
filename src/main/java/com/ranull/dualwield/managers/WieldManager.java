package com.ranull.dualwield.managers;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.containers.BlockBreakData;
import com.ranull.dualwield.nms.NMS;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WieldManager {
    private final DualWield plugin;
    private final NMS nms;
    private final Map<Block, BlockBreakData> blockBreakDataList = new HashMap<>();

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
            breakBlockOffHand(blockBreakData);
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
                                breakBlockOffHand(blockBreakData);
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

    public BlockBreakData createBlockBreakData(Block block, Player player, ItemStack itemStack) {
        return new BlockBreakData(block, nms.getBlockHardness(block), player, itemStack, new Random().nextInt(2000));
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

    public void breakBlockOffHand(BlockBreakData blockBreakData) {
        Player player = blockBreakData.getPlayer();

        swapHands(player, true);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(blockBreakData.getBlock(), blockBreakData.getPlayer());
        plugin.getServer().getPluginManager().callEvent(blockBreakEvent);

        if (!blockBreakEvent.isCancelled()) {
            nms.damageItem(player.getInventory().getItemInMainHand(), player);

            blockBreakData.getBlock().getWorld().playEffect(blockBreakData.getBlock().getLocation(),
                    Effect.STEP_SOUND, blockBreakData.getBlock().getType());

            blockBreakData.getBlock().breakNaturally(blockBreakData.getItemInOffHand());
        }

        swapHands(player);

        player.updateInventory();
    }

    public void attackEntityOffHand(Player player, Entity entity) {
        swapHands(player, true);

        nms.attackEntityOffHand(player, entity);

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        PlayerItemDamageEvent playerItemDamageEvent = new PlayerItemDamageEvent(player, itemStack, 1);

        plugin.getServer().getPluginManager().callEvent(playerItemDamageEvent);

        if (!playerItemDamageEvent.isCancelled()) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                nms.damageItem(itemStack, player);
            }
        }

        swapHands(player);

        player.updateInventory();
    }

    public void swapHands(Player player) {
        swapHands(player, false);
    }

    public void swapHands(Player player, boolean apiData) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand().clone();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand().clone();

        if (apiData) {
            itemInOffHand = nms.setAPIData(itemInOffHand);
        } else {
            itemInMainHand = nms.removeAPIData(itemInMainHand);
        }

        nms.setItemInMainHand(player, itemInOffHand);
        nms.setItemInOffHand(player, itemInMainHand);
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
