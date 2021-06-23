package com.ranull.dualwield;

import com.ranull.dualwield.api.DualWieldAPI;
import com.ranull.dualwield.listeners.PlayerInteractEntityListener;
import com.ranull.dualwield.listeners.PlayerInteractListener;
import com.ranull.dualwield.managers.WieldManager;
import com.ranull.dualwield.nms.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class DualWield extends JavaPlugin {
    private NMS nms;
    private DualWieldAPI dualWieldAPI;

    @Override
    public void onEnable() {
        if (setupNMS()) {
            WieldManager wieldManager = new WieldManager(this, nms);
            dualWieldAPI = new DualWieldAPI(wieldManager, nms);

            getServer().getPluginManager().registerEvents(new PlayerInteractListener(wieldManager), this);
            getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(wieldManager), this);
        } else {
            getLogger().severe("Version not supported, disabling plugin!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private boolean setupNMS() {
        try {
            String version = getServer().getClass().getPackage().getName().split("\\.")[3];

            if (version.equals("v1_9_R2")) {
                nms = new NMS_v1_9_R2();
            } else if (version.equals("v1_10_R1")) {
                nms = new NMS_v1_10_R1();
            } else if (version.equals("v1_11_R1")) {
                nms = new NMS_v1_11_R1();
            } else if (version.equals("v1_12_R1")) {
                nms = new NMS_v1_12_R1();
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
            }

            return nms != null;
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return false;
        }
    }

    public DualWieldAPI getDualWieldAPI() {
        return dualWieldAPI;
    }
}
