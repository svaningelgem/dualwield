package com.ranull.dualwield.update;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public final class UpdateChecker {
    private final Plugin plugin;
    private final int resourceId;

    public UpdateChecker(Plugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public String getVersion() {
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource="
                + resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {

            if (scanner.hasNext()) {
                return scanner.next();
            }
        } catch (IOException exception) {
            plugin.getLogger().info("Update: Cannot look for updates: " + exception.getMessage());
        }

        return null;
    }
}