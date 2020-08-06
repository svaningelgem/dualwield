package com.ranull.dualwield.nms;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NMS {
    void mainHandAnimation(Player player);

    void offHandAnimation(Player player);

    void blockBreakAnimation(Player player, Block block, int animationID, int stage);

    void blockCrackParticle(Block block);

    float getToolStrength(Block block, ItemStack itemStack);

    void setItemInMainHand(Player player, ItemStack itemStack);

    void attackEntityOffHand(Player player, Entity entity);

    void damageItem(ItemStack itemStack, Player player);

    double getAttackDamage(ItemStack itemStack);

    Sound getBreakSound(Block block);

    float getBlockHardness(Block block);
}