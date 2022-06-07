package com.ranull.dualwield;

import com.ranull.dualwield.command.DualWieldCommand;
import com.ranull.dualwield.listener.BlockBreakListener;
import com.ranull.dualwield.listener.EntityDamageByEntityListener;
import com.ranull.dualwield.listener.PlayerInteractEntityListener;
import com.ranull.dualwield.listener.PlayerInteractListener;
import com.ranull.dualwield.manager.DualWieldManager;
import com.ranull.dualwield.manager.VersionManager;
import com.ranull.dualwield.nms.NMS;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

public final class DualWield extends JavaPlugin {
    private static DualWield instance;
    private DualWieldManager dualWieldManager;
    private VersionManager versionManager;
    private NMS nms;

    public static boolean shouldAttack(Player player) {
        return instance != null && instance.getDualWieldManager().shouldAttack(player);
    }

    public static boolean shouldSwing(Player player) {
        return instance != null && instance.getDualWieldManager().shouldSwing(player);
    }

    public static boolean shouldBreak(Player player) {
        return instance != null && instance.getDualWieldManager().shouldMine(player);
    }

    public static boolean isDualWielding(Player player) {
        return instance != null && instance.getDualWieldManager().isDualWielding(player);
    }

    public static void attack(Player player, Entity entity) {
        if (instance != null) {
            instance.getDualWieldManager().attack(player, entity, EquipmentSlot.HAND);
        }
    }

    public static void attack(Player player, Entity entity, EquipmentSlot equipmentSlot) {
        if (instance != null) {
            instance.getDualWieldManager().attack(player, entity, equipmentSlot);
        }
    }

    @Override
    public void onEnable() {
        if (setupNMS()) {
            instance = this;
            dualWieldManager = new DualWieldManager(this);
            versionManager = new VersionManager(this);

            saveDefaultConfig();
            registerMetrics();
            registerCommands();
            registerListeners();
        } else {
            getLogger().severe("Version not supported, disabling plugin.");
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
            DualWieldCommand dualWieldCommand = new DualWieldCommand(this);

            pluginCommand.setExecutor(dualWieldCommand);
            pluginCommand.setTabCompleter(dualWieldCommand);
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
                nms = (NMS) clazz.getDeclaredConstructor().newInstance();
            }

            return nms != null;
        } catch (ArrayIndexOutOfBoundsException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            return false;
        }
    }
}
