package com.ranull.dualwield.nms;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NMS {
    void offHandAnimation(Player player);

    void blockBreakAnimation(Player player, Block block, int animationID, int stage);

    void blockCrackParticle(Block block);

    float getToolStrength(Block block, ItemStack itemStack);

    void setItemInMainHand(Player player, ItemStack itemStack);

    void setItemInOffHand(Player player, ItemStack itemStack);

    void damageItem(ItemStack itemStack, Player player);

    ItemStack addNBTKey(ItemStack itemStack, String key);

    ItemStack removeNBTKey(ItemStack itemStack, String key);

    boolean hasNBTKey(ItemStack itemStack, String key);

    String getItemName(ItemStack itemStack);

    double getAttackDamage(ItemStack itemStack);

    Sound getHitSound(Block block);

    float getBlockHardness(Block block);

    boolean breakBlock(Player player, Block block);

    void attackEntityOffHand(Player player, Entity entity);
}