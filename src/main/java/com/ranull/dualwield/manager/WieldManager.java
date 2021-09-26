package com.ranull.dualwield.manager;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.data.BlockBreakData;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WieldManager {
    private final DualWield plugin;
    private final Map<Block, BlockBreakData> blockBreakDataList = new HashMap<>();

    public WieldManager(DualWield plugin) {
        this.plugin = plugin;
    }

    public void runBlockBreakTask(BlockBreakData blockBreakData) {
        final List<Player> nearbyPlayers = getNearbyPlayers(blockBreakData.getBlock().getLocation(), 20);
        float blockHardness = plugin.getNMS().getBlockHardness(blockBreakData.getBlock());

        if (blockBreakData.getPlayer().getGameMode() == GameMode.CREATIVE || blockHardness == 0) {
            breakBlockOffHand(blockBreakData);
        } else if (blockHardness > 0) {
            // Timed break tool
            float toolStrength = plugin.getNMS().getToolStrength(blockBreakData.getBlock(),
                    blockBreakData.getItemInOffHand());
            float timer = (blockBreakData.getHardness() / (toolStrength * 6)) * 20;
            int crackAmount = 10;

            if (toolStrength > 1) {
                // Correct tool
                if (blockBreakData.getItemInOffHand().getItemMeta() != null) {
                    // Enchantment buff
                    if (blockBreakData.getItemInOffHand().getItemMeta().hasEnchant(Enchantment.DIG_SPEED)) {
                        crackAmount -= blockBreakData.getItemInOffHand().getItemMeta()
                                .getEnchantLevel(Enchantment.DIG_SPEED);
                    }

                    // Haste buff
                    if (blockBreakData.getPlayer().hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
                        PotionEffect potionEffect = blockBreakData.getPlayer()
                                .getPotionEffect(PotionEffectType.FAST_DIGGING);

                        if (potionEffect != null) {
                            crackAmount -= potionEffect.getAmplifier();
                        }
                    }
                }
            } else {
                // Wrong tool debuff
                timer = timer * (blockBreakData.getHardness() * 2);
            }

            // Mining fatigue debuff
            if (blockBreakData.getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                PotionEffect potionEffect = blockBreakData.getPlayer().getPotionEffect(PotionEffectType.SLOW_DIGGING);

                if (potionEffect != null) {
                    timer *= potionEffect.getAmplifier() * 15;
                }
            }

            // Swimming debuff
            if (blockBreakData.getPlayer().getLocation().add(0, 1, 0).getBlock().getType()
                    .equals(Material.WATER)) {
                timer *= 5;
            }

            // Vehicle debuff
            if (blockBreakData.getPlayer().getVehicle() != null) {
                timer *= 5;
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
        return new BlockBreakData(block, plugin.getNMS().getBlockHardness(block), player, itemStack,
                new Random().nextInt(2000));
    }

    public List<Player> getNearbyPlayers(Location location, int range) {
        final List<Player> nearbyPlayers = new ArrayList<>();

        if (location.getWorld() != null) {
            for (Entity entity : location.getWorld().getNearbyEntities(location, range, range, range)) {
                if (entity instanceof Player) {
                    nearbyPlayers.add((Player) entity);
                }
            }
        }

        return nearbyPlayers;
    }

    public boolean hasBreakData(Block block) {
        return blockBreakDataList.containsKey(block);
    }

    public BlockBreakData getBreakData(Block block) {
        return blockBreakDataList.get(block);
    }

    public void addBreakData(BlockBreakData blockBreakData) {
        blockBreakDataList.put(blockBreakData.getBlock(), blockBreakData);
    }

    public void removeBreakData(BlockBreakData blockBreakData) {
        blockBreakDataList.remove(blockBreakData.getBlock());
    }

    public void breakBlockOffHand(BlockBreakData blockBreakData) {
        Player player = blockBreakData.getPlayer();
        Material material = blockBreakData.getBlock().getType();

        swapHands(player, true);

        if (plugin.getNMS().breakBlock(player, blockBreakData.getBlock())) {
            if (player.getGameMode() != GameMode.CREATIVE
                    && !plugin.getNMS().hasNBTKey(player.getInventory().getItemInMainHand(), "Unbreakable")) {
                plugin.getNMS().damageItem(player.getInventory().getItemInMainHand(), player);
            }

            blockBreakData.getBlock().getWorld().playEffect(blockBreakData.getBlock().getLocation(),
                    Effect.STEP_SOUND, material);
        }

        swapHands(player);
        player.updateInventory();
    }

    public void attackEntityOffHand(Player player, Entity entity) {
        swapHands(player, true);
        plugin.getNMS().attackEntityOffHand(player, entity);

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        PlayerItemDamageEvent playerItemDamageEvent = new PlayerItemDamageEvent(player, itemStack, 1);

        plugin.getServer().getPluginManager().callEvent(playerItemDamageEvent);

        if (!playerItemDamageEvent.isCancelled() && player.getGameMode() != GameMode.CREATIVE
                && !plugin.getNMS().hasNBTKey(player.getInventory().getItemInMainHand(), "Unbreakable")) {
            plugin.getNMS().damageItem(itemStack, player);
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
            itemInOffHand = plugin.getNMS().addNBTKey(itemInOffHand, "dualWieldItem");
        } else {
            itemInMainHand = plugin.getNMS().removeNBTKey(itemInMainHand, "dualWieldItem");
        }

        plugin.getNMS().setItemInMainHand(player, itemInOffHand);
        plugin.getNMS().setItemInOffHand(player, itemInMainHand);
    }

    public void blockHitSound(BlockBreakData blockBreakData) {
        blockBreakData.getBlock().getWorld().playSound(blockBreakData.getBlock().getLocation(),
                plugin.getNMS().getHitSound(blockBreakData.getBlock()), 0.50F, 0.50F);
    }

    public void blockCrackAnimation(BlockBreakData blockBreakData, Player player) {
        blockCrackAnimation(blockBreakData, player, blockBreakData.getCrackAmount());
    }

    public void blockCrackAnimation(BlockBreakData blockBreakData, Player player, int stage) {
        plugin.getNMS().blockBreakAnimation(player, blockBreakData.getBlock(), blockBreakData.getAnimationID(), stage);
    }

    public void blockCrackParticle(BlockBreakData blockBreakData) {
        plugin.getNMS().blockCrackParticle(blockBreakData.getBlock());
    }
}
