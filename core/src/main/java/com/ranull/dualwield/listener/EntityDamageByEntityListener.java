package com.ranull.dualwield.listener;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.event.OffHandAttackEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {
    private final DualWield plugin;

    public EntityDamageByEntityListener(DualWield plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player
                && plugin.getDualWieldManager().isDualWielding(event.getDamager())) {
            Player player = (Player) event.getDamager();

            plugin.getDualWieldManager().swapHands(player);

            OffHandAttackEvent offHandAttackEvent = new OffHandAttackEvent(player, event.getEntity(),
                    event.getCause(), event.getDamage());

            plugin.getServer().getPluginManager().callEvent(offHandAttackEvent);
            plugin.getDualWieldManager().swapHands(player);
            event.setDamage(offHandAttackEvent.getDamage());
            event.setCancelled(offHandAttackEvent.isCancelled());
        }
    }
}
