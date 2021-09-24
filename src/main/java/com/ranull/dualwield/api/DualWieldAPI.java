package com.ranull.dualwield.api;

import com.ranull.dualwield.DualWield;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class DualWieldAPI {
    private final DualWield plugin;

    public DualWieldAPI(DualWield plugin) {
        this.plugin = plugin;
    }

    public boolean isBlockBreakEventOffHand(BlockBreakEvent blockBreakEvent) {
        Player player = blockBreakEvent.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        return plugin.getNMS().hasNBTKey(itemStack, "dualWieldItem");
    }

    public boolean isEntityDamageByEntityEventOffHand(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        if (entityDamageByEntityEvent.getDamager() instanceof Player) {
            Player player = (Player) entityDamageByEntityEvent.getDamager();
            ItemStack itemStack = player.getInventory().getItemInMainHand();

            return plugin.getNMS().hasNBTKey(itemStack, "dualWieldItem");
        }

        return false;
    }

    public ItemStack getItemInMainHand(Player player) {
        return plugin.getNMS().removeNBTKey(player.getInventory().getItemInOffHand().clone(), "dualWieldItem");
    }

    public void setItemInMainHand(Player player, ItemStack itemStack) {
        player.getInventory().setItemInOffHand(plugin.getNMS().addNBTKey(itemStack, "dualWieldItem"));
    }

    public ItemStack getItemInOffHand(Player player) {
        return player.getInventory().getItemInMainHand().clone();
    }

    public void setItemInOffHand(Player player, ItemStack itemStack) {
        player.getInventory().setItemInMainHand(itemStack);
    }
}
