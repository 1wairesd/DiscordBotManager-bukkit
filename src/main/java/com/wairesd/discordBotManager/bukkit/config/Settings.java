package com.wairesd.discordBotManager.bukkit.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Manages settings from settings.yml for Bukkit.
 */
public class Settings {
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);
    private static FileConfiguration settingsConfig;

    /** Loads settings.yml asynchronously with default saving. */
    public static void load(JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
            if (!settingsFile.exists()) {
                plugin.saveResource("settings.yml", false);
            }
            settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
            logger.info("settings.yml loaded successfully");
        });
    }

    public static String getVelocityHost() { return settingsConfig != null ? settingsConfig.getString("velocity.host", "127.0.0.1") : "127.0.0.1"; }
    public static int getVelocityPort() { return settingsConfig != null ? settingsConfig.getInt("velocity.port", 8080) : 8080; }
    public static boolean isDebug() { return settingsConfig != null && settingsConfig.getBoolean("debug", false); }
    public static String getServerName() { return settingsConfig != null ? settingsConfig.getString("server", "ServerName") : "ServerName"; }
    public static String getSecretCode() { return settingsConfig != null ? settingsConfig.getString("velocity.secret", "") : ""; }
}