package com.ranull.dualwield.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class OffHandBlockBreakEvent extends BlockBreakEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public OffHandBlockBreakEvent(Block block, Player player) {
        super(block, player);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
