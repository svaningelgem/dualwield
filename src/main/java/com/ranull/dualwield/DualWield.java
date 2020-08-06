package com.ranull.dualwield;

import com.ranull.dualwield.events.Events;
import com.ranull.dualwield.managers.WieldManager;
import com.ranull.dualwield.nms.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class DualWield extends JavaPlugin {
    private NMS nms;

    @Override
    public void onEnable() {
        if (!setupNMS()) {
            getLogger().severe("Version not supported, plugin disabling!");
            getServer().getPluginManager().disablePlugin(this);
        }

        WieldManager wieldManager = new WieldManager(this, nms);

        getServer().getPluginManager().registerEvents(new Events(wieldManager), this);
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
            }

            return nms != null;
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return false;
        }
    }
}
