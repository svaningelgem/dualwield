package com.ranull.dualwield;

import com.ranull.dualwield.command.DualWieldCommand;
import com.ranull.dualwield.listener.*;
import com.ranull.dualwield.manager.DualWieldManager;
import com.ranull.dualwield.manager.VersionManager;
import com.ranull.dualwield.nms.NMS;
import com.ranull.dualwield.update.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

public final class DualWield extends JavaPlugin {
    private static DualWield instance;
    private DualWieldManager dualWieldManager;
    private VersionManager versionManager;
    private NMS nms;

    public static boolean shouldAttack(Player player) {
        return instance.getDualWieldManager().shouldAttack(player);
    }

    public static boolean shouldSwing(Player player) {
        return instance.getDualWieldManager().shouldSwing(player);
    }

    public static boolean shouldBreak(Player player) {
        return instance.getDualWieldManager().shouldMine(player);
    }

    public static boolean isDualWielding(Player player) {
        return instance.getDualWieldManager().isDualWielding(player);
    }

    public static void attack(Player player, Entity entity) {
        instance.getDualWieldManager().attack(player, entity, EquipmentSlot.HAND);
    }

    public static void attack(Player player, Entity entity, EquipmentSlot equipmentSlot) {
        instance.getDualWieldManager().attack(player, entity, equipmentSlot);
    }

    @Override
    public void onEnable() {
        if (setupNMS()) {
            saveDefaultConfig();

            instance = this;
            dualWieldManager = new DualWieldManager(this);
            versionManager = new VersionManager(this);

            registerMetrics();
            registerCommands();
            registerListeners();
            getServer().getScheduler().runTask(this, this::updateChecker);
        } else {
            getLogger().severe("Version not supported, disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        unregisterListeners();
    }

    private void registerMetrics() {
        new Metrics(this, 12853);
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    private void registerCommands() {
        PluginCommand pluginCommand = getCommand("dualwield");

        if (pluginCommand != null) {
            pluginCommand.setExecutor(new DualWieldCommand(this));
        }
    }

    private void updateChecker() {
        String response = new UpdateChecker(this, 82349).getVersion();

        if (response != null) {
            try {
                double pluginVersion = Double.parseDouble(getDescription().getVersion());
                double pluginVersionLatest = Double.parseDouble(response);

                if (pluginVersion < pluginVersionLatest) {
                    getLogger().info("Update: Outdated version detected " + pluginVersion + ", latest version is "
                            + pluginVersionLatest + ", https://www.spigotmc.org/resources/dualwield.82349/");
                }
            } catch (NumberFormatException exception) {
                if (!getDescription().getVersion().equalsIgnoreCase(response)) {
                    getLogger().info("Update: Outdated version detected " + getDescription().getVersion()
                            + ", latest version is " + response + ", https://www.spigotmc.org/resources/dualwield.82349/");
                }
            }
        }
    }

    public NMS getNMS() {
        return nms;
    }

    public DualWieldManager getDualWieldManager() {
        return dualWieldManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    private boolean setupNMS() {
        try {
            String version = getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> clazz = Class.forName("com.ranull.dualwield.nms.NMS_" + version);

            if (NMS.class.isAssignableFrom(clazz)) {
                nms = (NMS) clazz.newInstance();
            }

            return nms != null;
        } catch (ArrayIndexOutOfBoundsException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ignored) {
            return false;
        }
    }
}
