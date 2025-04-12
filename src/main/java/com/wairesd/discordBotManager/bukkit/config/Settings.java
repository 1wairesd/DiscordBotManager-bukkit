package com.wairesd.discordBotManager.bukkit.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

// Loads and provides access to settings from settings.yml for the Bukkit plugin.
public class Settings {
    private static FileConfiguration settingsConfig;

    public static void load(JavaPlugin plugin) {
        File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            plugin.saveResource("settings.yml", false);
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
    }

    public static String getVelocityHost() {
        return settingsConfig.getString("velocity.host", "127.0.0.1");
    }

    public static int getVelocityPort() {
        return settingsConfig.getInt("velocity.port", 8080);
    }

    public static boolean isDebug() {
        return settingsConfig.getBoolean("debug", false);
    }

    public static String getServerName() {
        return settingsConfig.getString("server", "ServerName");
    }

    public static String getVelocitySecret() {
        return settingsConfig.getString("velocity.secret", "");
    }
}