package com.ranull.dualwield.listeners;

import com.ranull.dualwield.managers.WieldManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractEntityListener implements Listener {
    private WieldManager wieldManager;

    public PlayerInteractEntityListener(WieldManager wieldManager) {
        this.wieldManager = wieldManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        Entity entity = event.getRightClicked();

        if (player.hasPermission("dualwield.attack")
                && itemInOffHand.getAmount() != 0
                && event.getHand() == EquipmentSlot.OFF_HAND
                && player.getGameMode() != GameMode.SPECTATOR
                && wieldManager.isValidItem(itemInOffHand)) {
            wieldManager.attackEntityOffHand(player, entity);
            wieldManager.getNMS().offHandAnimation(player);
        }
    }
}
