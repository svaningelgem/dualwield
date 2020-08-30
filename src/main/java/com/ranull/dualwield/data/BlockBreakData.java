package com.ranull.dualwield.data;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockBreakData {
    private Block block;
    private Player player;
    private ItemStack itemInOffHand;
    private int animationID;
    private int crackAmount;
    private long lastMineTime;
    private float hardness;

    public BlockBreakData(Block block, float hardness, Player player, ItemStack itemInOffHand, int animationID) {
        this.block = block;
        this.player = player;
        this.itemInOffHand = itemInOffHand;
        this.animationID = animationID;
        this.crackAmount = 0;
        this.lastMineTime = System.currentTimeMillis();
        this.hardness = hardness;
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

    public long getLastMineTime() {
        return lastMineTime;
    }

    public int getCrackAmount() {
        return crackAmount;
    }

    public void addCrackAmount() {
        crackAmount++;
    }

    public float getHardness() {
        return hardness;
    }

    public void updateLastMineTime() {
        lastMineTime = System.currentTimeMillis();
    }
}
