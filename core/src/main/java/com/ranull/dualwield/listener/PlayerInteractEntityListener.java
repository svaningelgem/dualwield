package org.avarion.dualwield.listener;

import org.avarion.dualwield.DualWield;
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

            if (!plugin.getConfig().getBoolean("attack.items.correct")
                    || plugin.getNMS().getAttackDamage(itemStack) > 0) {
                plugin.getDualWieldManager().attack(player, entity, event.getHand());

                if (plugin.getConfig().getBoolean("attack.events.cancel.original")) {
                    event.setCancelled(true);
                }

                if (itemStack.getType() == Material.AIR && plugin.getDualWieldManager().shouldSwing(player)) {
                    plugin.getNMS().handAnimation(player, event.getHand());
                }
            }
        }
    }
}
