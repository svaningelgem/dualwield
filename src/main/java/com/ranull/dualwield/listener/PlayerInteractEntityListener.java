package com.ranull.dualwield.listener;

import com.ranull.dualwield.DualWield;
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        Entity entity = event.getRightClicked();

        if (player.hasPermission("dualwield.attack") && event.getHand() == EquipmentSlot.OFF_HAND
                && ((itemInOffHand.getAmount() > 0 && !itemInOffHand.getType().isBlock())
                || (plugin.getConfig().getBoolean("settings.attack.hand") && itemInOffHand.getAmount() <= 0))
                && ((player.isSneaking() && plugin.getConfig().getBoolean("settings.attack.sneaking"))
                || (!player.isSneaking() && plugin.getConfig().getBoolean("settings.attack.standing")))
                && plugin.getConfig().getStringList("settings.attack.gamemode")
                .contains(player.getGameMode().toString())) {
            plugin.getWieldManager().attackEntityOffHand(player, entity);

            if (!plugin.getConfig().getBoolean("settings.swing.air")) {
                plugin.getNMS().offHandAnimation(player);
            }
        }
    }
}
