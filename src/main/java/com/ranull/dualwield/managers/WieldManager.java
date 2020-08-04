package com.ranull.dualwield.managers;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.nms.NMS;
import com.ranull.dualwield.containers.BlockBreakData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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
        if (blockBreakData.getBlock().getType().getHardness() == 0.0) {
            // Instant break block
            blockCrackAnimation(blockBreakData);
            breakBlock(blockBreakData);
        } else if (blockBreakData.getBlock().getType().getHardness() > 0.0) {
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

            int finalCrackAmount = crackAmount;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (blockBreakData.getCrackAmount() < finalCrackAmount) {
                        if ((System.currentTimeMillis() - blockBreakData.getLastMineTime()) < 200) {
                            // Continue because player is mining
                            blockCrackAnimation(blockBreakData);
                            blockBreakData.addCrackAmount();
                        } else {
                            // Cancel because player stopped mining
                            blockCrackAnimation(blockBreakData, -1);
                            removeBreakData(blockBreakData);

                            // Cancel runnable
                            cancel();
                        }
                    } else {
                        // Break block mine finished
                        blockCrackAnimation(blockBreakData, -1);
                        blockParticleAnimation(blockBreakData);

                        // Break block and cleanup
                        breakBlock(blockBreakData);
                        removeBreakData(blockBreakData);

                        // Cancel runnable
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, (long) timer);
        }
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
            damageItem(blockBreakData.getItemInOffHand(), blockBreakData.getPlayer());

            blockBreakData.getBlock().getWorld().playEffect(blockBreakData.getBlock().getLocation(),
                    Effect.STEP_SOUND, blockBreakData.getBlock().getType());

            blockBreakData.getBlock().breakNaturally(blockBreakData.getItemInOffHand());
        }
    }

    public void blockHitSound(BlockBreakData blockBreakData) {
        Sound sound = nms.getBreakSound(blockBreakData.getBlock());

        blockBreakData.getBlock().getWorld().playSound(blockBreakData.getBlock().getLocation(), sound, 0.50F, 0.75F);
    }

    public void damageItem(ItemStack itemStack, Player player) {
        if (itemStack.getItemMeta() instanceof Damageable
                && itemStack.getType().getMaxDurability() > 0
                && calculateDamageChance(itemStack)) {
            Damageable damageable = (Damageable) itemStack.getItemMeta();

            damageable.setDamage(damageable.getDamage() + 1);

            itemStack.setItemMeta((ItemMeta) damageable);

            if (damageable.getDamage() >= itemStack.getType().getMaxDurability()) {
                itemStack.setAmount(0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }
        }
    }

    public boolean calculateDamageChance(ItemStack itemStack) {
        int level = itemStack.getEnchantmentLevel(Enchantment.DURABILITY);

        Random random = new Random();

        if (level == 1 ) {
            return random.nextFloat() <= 0.20f;
        } else if (level == 2 ) {
            return random.nextFloat() <= 0.27f;
        } else if (level == 3) {
            return random.nextFloat() <= 0.30f;
        }

        return true;
    }

    public void blockCrackAnimation(BlockBreakData blockBreakData) {
        blockCrackAnimation(blockBreakData, blockBreakData.getCrackAmount());
    }

    public void attackEntity(Player player, Entity entity) {
        nms.attackEntityOffHand(player, entity);
    }

    public void blockCrackAnimation(BlockBreakData blockBreakData, int stage) {
        int range = 20;

        for (Entity entity : blockBreakData.getBlock().getWorld()
                .getNearbyEntities(blockBreakData.getBlock().getLocation(), range, range, range)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;

                nms.blockBreakAnimation(player, blockBreakData.getBlock(), blockBreakData.getAnimationID(), stage);
            }
        }
    }

    public void blockParticleAnimation(BlockBreakData blockBreakData) {
        blockBreakData.getBlock().getWorld().spawnParticle(Particle.BLOCK_CRACK, blockBreakData.getBlock().getLocation()
                .add(0.5,0,0.5), 10, blockBreakData.getBlock().getBlockData());
    }
}
