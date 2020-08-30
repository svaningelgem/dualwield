package com.ranull.dualwield.api;

import com.ranull.dualwield.managers.WieldManager;
import com.ranull.dualwield.nms.NMS;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class DualWieldAPI {
    private WieldManager wieldManager;
    private NMS nms;

    public DualWieldAPI(WieldManager wieldManager, NMS nms) {
        this.wieldManager = wieldManager;
        this.nms = nms;
    }

    public boolean isBlockBreakEventOffHand(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (nms.hasNBTKey(itemStack, "dualWieldItem")) {
            return true;
        }

        return false;
    }

    public boolean isEntityDamageByEntityEventOffHand(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (entityDamageByEntityEvent.getDamager() instanceof Player) {
            Player player = (Player) entityDamageByEntityEvent.getDamager();
            ItemStack itemStack = player.getInventory().getItemInMainHand();

            if (nms.hasNBTKey(itemStack, "dualWieldItem")) {
                return true;
            }
        }

        return false;
    }

    public ItemStack getItemInMainHand(Player player) {
        return nms.removeNBTKey(player.getInventory().getItemInOffHand().clone(), "dualWieldItem");
    }

    public void setItemInMainHand(Player player, ItemStack itemStack) {
        player.getInventory().setItemInOffHand(nms.addNBTKey(itemStack, "dualWieldItem"));
    }

    public ItemStack getItemInOffHand(Player player) {
        return player.getInventory().getItemInMainHand().clone();
    }

    public void setItemInOffHand(Player player, ItemStack itemStack) {
        player.getInventory().setItemInMainHand(itemStack);
    }

    public WieldManager getWieldManager() {
        return wieldManager;
    }

    public NMS getNms() {
        return nms;
    }
}
