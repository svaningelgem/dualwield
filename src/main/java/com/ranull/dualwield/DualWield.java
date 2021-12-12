package com.ranull.dualwield;

import com.ranull.dualwield.api.DualWieldAPI;
import com.ranull.dualwield.commands.DualWieldCommand;
import com.ranull.dualwield.listener.PlayerInteractEntityListener;
import com.ranull.dualwield.listener.PlayerInteractListener;
import com.ranull.dualwield.manager.WieldManager;
import com.ranull.dualwield.nms.*;
import com.ranull.dualwield.update.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DualWield extends JavaPlugin {
    private NMS nms;
    private WieldManager wieldManager;
    private DualWieldAPI dualWieldAPI;

    @Override
    public void onEnable() {
        if (setupNMS()) {
            saveDefaultConfig();

            wieldManager = new WieldManager(this);
            dualWieldAPI = new DualWieldAPI(this);

            PluginCommand pluginCommand = getCommand("dualwield");

            if (pluginCommand != null) {
                pluginCommand.setExecutor(new DualWieldCommand(this));
            }

            getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(this), this);

            new Metrics(this, 12853);
            String response = new UpdateChecker(this, 82349).getVersion();

            if (response != null) {
                try {
                    double pluginVersion = Double.parseDouble(getDescription().getVersion());
                    double pluginVersionLatest = Double.parseDouble(response);

                    if (pluginVersion < pluginVersionLatest) {
                        updateMessage("Outdated version detected " + pluginVersion + ", latest version is "
                                + pluginVersionLatest + ", https://www.spigotmc.org/resources/dualwield.82349/");
                    }
                } catch (NumberFormatException exception) {
                    if (!getDescription().getVersion().equalsIgnoreCase(response)) {
                        updateMessage("Outdated version detected " + getDescription().getVersion()
                                + ", latest version is " + response + ", https://www.spigotmc.org/resources/dualwield.82349/");
                    }
                }
            }
        } else {
            getLogger().severe("Version not supported, disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private boolean setupNMS() {
        try {
            String version = getServer().getClass().getPackage().getName().split("\\.")[3];

            if (version.equals("v1_9_R1") && !getServer().getBukkitVersion().contains("1.9-SNAPSHOT")) {
                nms = new NMS_v1_9_R1();
            } else if (version.equals("v1_9_R2")) {
                nms = new NMS_v1_9_R2();
            } else if (version.equals("v1_10_R1")) {
                nms = new NMS_v1_10_R1();
            } else if (version.equals("v1_11_R1")) {
                nms = new NMS_v1_11_R1();
            } else if (version.equals("v1_12_R1")) {
                nms = new NMS_v1_12_R1();
            } else if (version.equals("v1_13_R1")) {
                nms = new NMS_v1_13_R1();
            } else if (version.equals("v1_13_R2")) {
                nms = new NMS_v1_13_R2();
            } else if (version.equals("v1_14_R1")) {
                nms = new NMS_v1_14_R1();
            } else if (version.equals("v1_15_R1")) {
                nms = new NMS_v1_15_R1();
            } else if (version.equals("v1_16_R1")) {
                nms = new NMS_v1_16_R1();
            } else if (version.equals("v1_16_R2")) {
                nms = new NMS_v1_16_R2();
            } else if (version.equals("v1_16_R3")) {
                nms = new NMS_v1_16_R3();
            } else if (version.equals("v1_17_R1")) {
                nms = new NMS_v1_17_R1();
            } else if (version.equals("v1_18_R1")) {
                nms = new NMS_v1_18_R1();
            }
            

            return nms != null;
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return false;
        }
    }

    public NMS getNMS() {
        return nms;
    }

    public WieldManager getWieldManager() {
        return wieldManager;
    }

    public void updateMessage(String string) {
        getLogger().info("Update: " + string);
    }

    public DualWieldAPI getDualWieldAPI() {
        return dualWieldAPI;
    }
}
