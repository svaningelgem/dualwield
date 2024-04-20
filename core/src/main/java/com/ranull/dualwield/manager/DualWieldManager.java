package org.avarion.dualwield.manager;

import org.avarion.dualwield.DualWield;
import org.avarion.dualwield.data.BlockBreakData;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.regex.PatternSyntaxException;

public final class DualWieldManager {
    private final DualWield plugin;
    private final Map<Block, BlockBreakData> blockBreakDataList = new HashMap<>();
    private final List<UUID> dualWieldingList;

    public DualWieldManager(DualWield plugin) {
        this.plugin = plugin;
        this.dualWieldingList = new ArrayList<>();
    }

    public void runBlockBreak(BlockBreakData blockBreakData) {
        blockBreakDataList.put(blockBreakData.getBlock(), blockBreakData);

        if (blockBreakData.getPlayer().getGameMode() == GameMode.CREATIVE || blockBreakData.getHardness() == 0) {
            breakBlock(blockBreakData);
        } else if (blockBreakData.getHardness() > 0) {
            final List<Player> nearbyPlayers = getNearbyPlayers(blockBreakData.getBlock().getLocation(), 20);
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

                            removeBlockBreakData(blockBreakData);

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
                                removeBlockBreakData(blockBreakData);
                            }
                        }.runTask(plugin);

                        // Cancel runnable
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(plugin, 0L, (long) timer);
        }
    }

    public void setDualWielding(Entity entity) {
        dualWieldingList.add(entity.getUniqueId());
    }

    public void removeDualWielding(Entity entity) {
        dualWieldingList.remove(entity.getUniqueId());
    }

    public boolean isDualWielding(Entity entity) {
        return dualWieldingList.contains(entity.getUniqueId());
    }

    public void attack(Player player, Entity entity, EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HAND) {
            plugin.getNMS().attack(player, entity);
            plugin.getNMS().handAnimation(player, EquipmentSlot.HAND);
        } else if (equipmentSlot == EquipmentSlot.OFF_HAND) {
            UUID uuid = UUID.randomUUID();

            swapHands(player);
            setDualWielding(player);
            correctPlayerAttackAndSpeed(player, uuid);
            plugin.getNMS().attack(player, entity);
            returnPlayerAttackAndSpeed(player, uuid);
            swapHands(player);
            removeDualWielding(player);
            plugin.getNMS().handAnimation(player, EquipmentSlot.OFF_HAND);
        }
    }

    private void correctPlayerAttackAndSpeed(Player player, UUID uuid) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        double damageMainHand = plugin.getNMS().getAttackDamage(mainHand) - plugin.getNMS().getAttackDamage(offHand);
        double speedMainHand = plugin.getNMS().getAttackSpeed(mainHand) - plugin.getNMS().getAttackSpeed(offHand);

        plugin.getNMS().setModifier(player, damageMainHand, speedMainHand, uuid);
    }

    private void returnPlayerAttackAndSpeed(Player player, UUID uuid) {
        plugin.getNMS().removeModifier(player, uuid);
    }

    public void breakBlock(BlockBreakData blockBreakData) {
        Player player = blockBreakData.getPlayer();
        Block block = blockBreakData.getBlock();
        Material material = block.getType();

        swapHands(player);
        setDualWielding(player);

        if (plugin.getNMS().breakBlock(player, block)) {
            blockBreakData.getBlock().getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, material);
        } else if (player.getPlayer() != null) {
            player.getPlayer().playEffect(block.getLocation(), Effect.STEP_SOUND, material);
        }

        swapHands(player);
        removeDualWielding(player);
    }

    public BlockBreakData createBlockBreakData(Block block, Player player, ItemStack itemStack) {
        return new BlockBreakData(block, plugin.getNMS().getBlockHardness(block), player, itemStack,
                new Random().nextInt(2000));
    }

    public BlockBreakData getBlockBreakData(Block block) {
        return blockBreakDataList.get(block);
    }

    public boolean hasBlockBreakData(Block block) {
        return blockBreakDataList.containsKey(block);
    }

    public void removeBlockBreakData(BlockBreakData blockBreakData) {
        blockBreakDataList.remove(blockBreakData.getBlock());
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

    public void swapHands(Player player) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand().clone();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand().clone();

        player.getInventory().setItemInMainHand(itemInOffHand);
        player.getInventory().setItemInOffHand(itemInMainHand);
        player.updateInventory();
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

    public boolean shouldAttack(Player player) {
        ItemStack itemStack = player.getInventory().getItemInOffHand();

        return player.hasPermission("dualwield.attack")
                && shouldMaterialAttack(itemStack.getType().name())
                && ((itemStack.getType() != Material.AIR && !itemStack.getType().isBlock())
                || (plugin.getConfig().getBoolean("attack.hand") && itemStack.getType() == Material.AIR))
                && ((player.isSneaking() && plugin.getConfig().getBoolean("attack.sneaking"))
                || (!player.isSneaking() && plugin.getConfig().getBoolean("attack.standing")))
                && plugin.getConfig().getStringList("attack.gamemodes")
                .contains(player.getGameMode().toString());
    }

    public boolean shouldMine(Player player) {
        ItemStack itemStack = player.getInventory().getItemInOffHand();

        return player.hasPermission("dualwield.mine")
                && shouldMaterialMine(itemStack.getType().name())
                && ((itemStack.getType() != Material.AIR && !itemStack.getType().isBlock())
                || (plugin.getConfig().getBoolean("mine.hand") && itemStack.getType() == Material.AIR))
                && ((player.isSneaking() && plugin.getConfig().getBoolean("mine.sneaking"))
                || (!player.isSneaking() && plugin.getConfig().getBoolean("mine.standing")))
                && plugin.getConfig().getStringList("mine.gamemodes")
                .contains(player.getGameMode().toString());
    }

    public boolean shouldSwing(Player player) {
        ItemStack itemStack = player.getInventory().getItemInOffHand();

        return player.hasPermission("dualwield.attack")
                && shouldMaterialAttack(itemStack.getType().name())
                && plugin.getConfig().getBoolean("swing.air")
                && ((itemStack.getType() != Material.AIR && !itemStack.getType().isBlock())
                || (plugin.getConfig().getBoolean("attack.hand") && itemStack.getType() == Material.AIR))
                && ((player.isSneaking() && plugin.getConfig().getBoolean("attack.sneaking"))
                || (!player.isSneaking() && plugin.getConfig().getBoolean("attack.standing")))
                && plugin.getConfig().getStringList("attack.gamemodes")
                .contains(player.getGameMode().toString());
    }

    private boolean shouldMaterialMine(String string) {
        String mode = plugin.getConfig().getString("mine.items.mode", "BLACKLIST");
        List<String> stringList = plugin.getConfig().getStringList("mine.items.list");

        return (mode.equalsIgnoreCase("whitelist") && stringListMatches(stringList, string))
                || (mode.equalsIgnoreCase("blacklist") && !stringListMatches(stringList, string));
    }

    private boolean shouldMaterialAttack(String string) {
        String mode = plugin.getConfig().getString("attack.items.mode", "BLACKLIST");
        List<String> stringList = plugin.getConfig().getStringList("attack.items.list");

        return (mode.equalsIgnoreCase("whitelist") && stringListMatches(stringList, string))
                || (mode.equalsIgnoreCase("blacklist") && !stringListMatches(stringList, string));
    }

    private boolean stringListMatches(List<String> stringList, String string) {
        try {
            return stringList.stream().anyMatch(string::matches);
        } catch (PatternSyntaxException exception) {
            plugin.getLogger().warning("Misconfiguration: " + exception.getMessage());
        }

        return false;
    }
}
