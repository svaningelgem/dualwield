package com.ranull.dualwield.listener;

import com.ranull.dualwield.DualWield;
import com.ranull.dualwield.event.OffHandBlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    private final DualWield plugin;

    public BlockBreakListener(DualWield plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (plugin.getDualWieldManager().isDualWielding(player)) {
            OffHandBlockBreakEvent offHandBlockBreakEvent = new OffHandBlockBreakEvent(block, player);

            plugin.getServer().getPluginManager().callEvent(offHandBlockBreakEvent);
            event.setCancelled(offHandBlockBreakEvent.isCancelled());
            event.setExpToDrop(offHandBlockBreakEvent.getExpToDrop());

            if (!plugin.getVersionManager().is_v1_9() && !plugin.getVersionManager().is_v1_10()
                    && !plugin.getVersionManager().is_v1_11()) {
                event.setDropItems(offHandBlockBreakEvent.isDropItems());
            }
        }
    }
}
