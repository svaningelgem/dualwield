package com.ranull.dualwield.managers;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.data.BlockBreakData;
import com.ranull.dualwield.nms.NMS;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WieldManager {
    private final DualWield plugin;
    private final NMS nms;
    private final Map<Block, BlockBreakData> blockBreakDataList = new HashMap<>();
    private final List<String> itemNameList = new ArrayList<>();

    public WieldManager(DualWield plugin, NMS nms) {
        this.plugin = plugin;
        this.nms = nms;

        addNewItemMaterialsToList(itemNameList);
        addOldItemMaterialsToList(itemNameList);
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
            float timer = (blockBreakData.getHardness() / (toolStrength * 6)) * 20;
            int crackAmount = 10;

            if (toolStrength > 1) {
                // Correct tool
                if (blockBreakData.getItemInOffHand().hasItemMeta()) {
                    // Enchantment buff
                    if (blockBreakData.getItemInOffHand().getItemMeta().hasEnchant(Enchantment.DIG_SPEED)) {
                        crackAmount -= blockBreakData.getItemInOffHand().getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED);
                    }

                    // Haste buff
                    if (blockBreakData.getPlayer().hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
                        crackAmount -= blockBreakData.getPlayer().getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier();
                    }
                }
            } else {
                // Wrong tool debuff
                timer = timer * (blockBreakData.getHardness() * 2);
            }

            // Mining fatigue debuff
            if (blockBreakData.getPlayer().hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                timer *= blockBreakData.getPlayer().getPotionEffect(PotionEffectType.SLOW_DIGGING).getAmplifier() * 15;
            }

            // Swimming debuff
            if (blockBreakData.getPlayer().getLocation().add(0, 1, 0).getBlock().getType().equals(Material.WATER)) {
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

        swapHands(player, true);

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(blockBreakData.getBlock(), blockBreakData.getPlayer());
        plugin.getServer().getPluginManager().callEvent(blockBreakEvent);

        if (!blockBreakEvent.isCancelled()) {
            if (!nms.hasNBTKey(player.getInventory().getItemInMainHand(), "Unbreakable")) {
                nms.damageItem(player.getInventory().getItemInMainHand(), player);
            }

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

        if (!playerItemDamageEvent.isCancelled()
                && !nms.hasNBTKey(player.getInventory().getItemInMainHand(), "Unbreakable")
                && player.getGameMode() != GameMode.CREATIVE) {
            nms.damageItem(itemStack, player);
        }

        swapHands(player);
        player.updateInventory();
    }

    public boolean isValidItem(ItemStack itemStack) {
        return itemNameList.stream().anyMatch(nms.getItemName(itemStack)::equalsIgnoreCase);
    }

    public void swapHands(Player player) {
        swapHands(player, false);
    }

    public void swapHands(Player player, boolean apiData) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand().clone();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand().clone();

        if (apiData) {
            itemInOffHand = nms.addNBTKey(itemInOffHand, "dualWieldItem");
        } else {
            itemInMainHand = nms.removeNBTKey(itemInMainHand, "dualWieldItem");
        }

        nms.setItemInMainHand(player, itemInOffHand);
        nms.setItemInOffHand(player, itemInMainHand);
    }

    public void blockHitSound(BlockBreakData blockBreakData) {
        blockBreakData.getBlock().getWorld().playSound(blockBreakData.getBlock().getLocation(),
                nms.getBreakSound(blockBreakData.getBlock()), 0.50F, 0.75F);
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

    public void addNewItemMaterialsToList(List<String> itemNameList) {
        // Sword
        itemNameList.add("WOODEN_SWORD");
        itemNameList.add("STONE_SWORD");
        itemNameList.add("GOLDEN_SWORD");
        itemNameList.add("IRON_SWORD");
        itemNameList.add("DIAMOND_SWORD");
        itemNameList.add("NETHERITE_SWORD");
        itemNameList.add("TRIDENT");

        // Axe
        itemNameList.add("WOODEN_AXE");
        itemNameList.add("STONE_AXE");
        itemNameList.add("GOLDEN_AXE");
        itemNameList.add("IRON_AXE");
        itemNameList.add("DIAMOND_AXE");
        itemNameList.add("NETHERITE_AXE");

        // Pickaxe
        itemNameList.add("WOODEN_PICKAXE");
        itemNameList.add("STONE_PICKAXE");
        itemNameList.add("GOLDEN_PICKAXE");
        itemNameList.add("IRON_PICKAXE");
        itemNameList.add("DIAMOND_PICKAXE");
        itemNameList.add("NETHERITE_PICKAXE");

        // Shovel
        itemNameList.add("WOODEN_SHOVEL");
        itemNameList.add("STONE_SHOVEL");
        itemNameList.add("GOLDEN_SHOVEL");
        itemNameList.add("IRON_SHOVEL");
        itemNameList.add("DIAMOND_SHOVEL");
        itemNameList.add("NETHERITE_SHOVEL");

        // Hoe
        itemNameList.add("WOODEN_HOE");
        itemNameList.add("STONE_HOE");
        itemNameList.add("GOLDEN_HOE");
        itemNameList.add("IRON_HOE");
        itemNameList.add("DIAMOND_HOE");
        itemNameList.add("NETHERITE_HOE");
    }

    public void addOldItemMaterialsToList(List<String> itemNameList) {
        // Sword
        itemNameList.add("SWORDWOOD");
        itemNameList.add("SWORDSTONE");
        itemNameList.add("SWORDGOLD");
        itemNameList.add("SWORDIRON");
        itemNameList.add("SWORDDIAMOND");

        // Axe
        itemNameList.add("HATCHETWOOD");
        itemNameList.add("HATCHETSTONE");
        itemNameList.add("HATCHETGOLD");
        itemNameList.add("HATCHETIRON");
        itemNameList.add("HATCHETDIAMOND");

        // Pickaxe
        itemNameList.add("PICKAXEWOOD");
        itemNameList.add("PICKAXESTONE");
        itemNameList.add("PICKAXEGOLD");
        itemNameList.add("PICKAXEIRON");
        itemNameList.add("PICKAXEDIAMOND");

        // Shovel
        itemNameList.add("SHOVELWOOD");
        itemNameList.add("SHOVELSTONE");
        itemNameList.add("SHOVELGOLD");
        itemNameList.add("SHOVELIRON");
        itemNameList.add("SHOVELDIAMOND");

        // Hoe
        itemNameList.add("HOEWOOD");
        itemNameList.add("HOESTONE");
        itemNameList.add("HOEGOLD");
        itemNameList.add("HOEIRON");
        itemNameList.add("HOEDIAMOND");
    }
}
