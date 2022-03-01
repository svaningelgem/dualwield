package com.ranull.dualwield.nms;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface NMS {
    void handAnimation(Player player, EquipmentSlot equipmentSlot);

    void blockBreakAnimation(Player player, Block block, int animationID, int stage);

    void blockCrackParticle(Block block);

    float getToolStrength(Block block, ItemStack itemStack);

    double getAttackDamage(ItemStack itemStack);

    double getAttackSpeed(ItemStack itemStack);

    Sound getHitSound(Block block);

    float getBlockHardness(Block block);

    boolean breakBlock(Player player, Block block);

    void setModifier(Player player, double damage, double speed, UUID uuid);

    void removeModifier(Player player, UUID uuid);

    void attack(Player player, Entity entity);
}