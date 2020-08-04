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

    float getToolStrength(Block block, ItemStack itemStack);

    void setItemInMainHand(Player player, ItemStack itemStack);

    void attackEntityOffHand(Player player, Entity entity);

    double getAttackDamage(ItemStack itemStack);

    Sound getBreakSound(Block block);
}