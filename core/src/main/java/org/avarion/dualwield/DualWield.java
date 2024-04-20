package org.avarion.dualwield;

import org.avarion.dualwield.command.DualWieldCommand;
import org.avarion.dualwield.listener.BlockBreakListener;
import org.avarion.dualwield.listener.EntityDamageByEntityListener;
import org.avarion.dualwield.listener.PlayerInteractEntityListener;
import org.avarion.dualwield.listener.PlayerInteractListener;
import org.avarion.dualwield.manager.DualWieldManager;
import org.avarion.dualwield.manager.VersionManager;
import org.avarion.dualwield.nms.NMS;
import org.avarion.dualwield.util.UpdateUtil;
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
            registerBStats();
            registerCommands();
            registerListeners();

            runUpdateChecker();
        } else {
            getLogger().severe("Server version not supported, I am going to disable this plugin.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        unregisterListeners();

        instance = null;
        dualWieldManager = null;
        versionManager = null;
    }

    private void runUpdateChecker() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            if ( !getConfig().getBoolean("settings.update.check") ) return;

            String currentVersion = getDescription().getVersion();
            String latestVersion = UpdateUtil.getLatestVersion(getSpigotID());
            if (currentVersion.equals(latestVersion)) return;
            if (latestVersion == null) return;

            try {
                double pluginVersion = Double.parseDouble(currentVersion);
                double pluginVersionLatest = Double.parseDouble(latestVersion);

                if (pluginVersion >= pluginVersionLatest) return;
            } catch (NumberFormatException ignored) {
            }

            getLogger().info(String.format("Update: Outdated version detected %s, latest version is %s, https://www.spigotmc.org/resources/%d/", currentVersion, latestVersion, getSpigotID()));
        });
    }

    private int getSpigotID() { return 123; }
    private int getBStatsID() { return 21641; }

    private void registerBStats() { new Metrics(this, getBStatsID()); }

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
            Class<?> clazz = Class.forName("org.avarion.dualwield.nms.NMS_" + version);

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
