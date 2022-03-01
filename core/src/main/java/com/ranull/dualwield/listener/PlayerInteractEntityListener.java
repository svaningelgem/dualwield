package com.ranull.dualwield.listener;

import com.ranull.dualwield.DualWield;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractEntityListener implements Listener {
    private final DualWield plugin;

    public PlayerInteractEntityListener(DualWield plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (event.getHand() == EquipmentSlot.OFF_HAND && plugin.getDualWieldManager().shouldAttack(player)) {
            ItemStack itemStack = player.getInventory().getItemInOffHand();

            plugin.getDualWieldManager().attack(player, entity, event.getHand());

            if (plugin.getConfig().getBoolean("settings.attack.cancel-event")) {
                event.setCancelled(true);
            }

            if (itemStack.getType() == Material.AIR && plugin.getDualWieldManager().shouldSwing(player)) {
                plugin.getNMS().handAnimation(player, event.getHand());
            }
        }
    }
}
