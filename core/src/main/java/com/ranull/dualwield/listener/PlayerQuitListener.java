package com.ranull.dualwield.listener;

import com.ranull.dualwield.DualWield;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final DualWield plugin;

    public PlayerQuitListener(DualWield plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getDualWieldManager().removeDualWielding(event.getPlayer());
    }
}
