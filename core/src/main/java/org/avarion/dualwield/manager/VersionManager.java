package org.avarion.dualwield.manager;

import org.bukkit.plugin.java.JavaPlugin;

public final class VersionManager {
    private final String version;

    public VersionManager(JavaPlugin plugin) {
        this.version = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_9() {
        return version.matches("(?i)v1_9_R1|v1_9_R2");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_10() {
        return version.matches("(?i)v1_10_R1");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_11() {
        return version.matches("(?i)v1_11_R1");
    }
}
