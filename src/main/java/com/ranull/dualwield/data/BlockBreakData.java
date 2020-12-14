package com.ranull.dualwield.data;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockBreakData {
    private final Block block;
    private final Player player;
    private final ItemStack itemInOffHand;
    private final int animationID;
    private final float hardness;
    private int crackAmount;
    private long lastMineTime;

    public BlockBreakData(Block block, float hardness, Player player, ItemStack itemInOffHand, int animationID) {
        this.block = block;
        this.player = player;
        this.itemInOffHand = itemInOffHand;
        this.animationID = animationID;
        this.hardness = hardness;
        this.crackAmount = 0;
        this.lastMineTime = System.currentTimeMillis();
    }

    public Block getBlock() {
        return block;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getItemInOffHand() {
        return itemInOffHand;
    }

    public int getAnimationID() {
        return animationID;
    }

    public float getHardness() {
        return hardness;
    }

    public long getLastMineTime() {
        return lastMineTime;
    }

    public int getCrackAmount() {
        return crackAmount;
    }

    public void addCrackAmount() {
        crackAmount++;
    }

    public void updateLastMineTime() {
        lastMineTime = System.currentTimeMillis();
    }
}
