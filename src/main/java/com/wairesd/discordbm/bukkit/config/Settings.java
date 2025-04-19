package com.wairesd.discordbm.bukkit.config;

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
        File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
        if (!settingsFile.exists()) {
            plugin.saveResource("settings.yml", false);
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
    }

    public static String getVelocityHost() { return settingsConfig.getString("velocity.host", "127.0.0.1"); }
    public static int getVelocityPort() { return settingsConfig.getInt("velocity.port", 8080); }
    public static String getServerName() { return settingsConfig.getString("server", "ServerName"); }
    public static String getSecretCode() { return settingsConfig.getString("velocity.secret", ""); }

    // Debug settings from the query
    public static boolean isDebugConnections() { return settingsConfig.getBoolean("debug.debug-connections", true); }
    public static boolean isDebugClientResponses() { return settingsConfig.getBoolean("debug.debug-client-responses", false); }
    public static boolean isDebugCommandRegistrations() { return settingsConfig.getBoolean("debug.debug-command-registrations", false); }
    public static boolean isDebugAuthentication() { return settingsConfig.getBoolean("debug.debug-authentication", true); }
    public static boolean isDebugErrors() { return settingsConfig.getBoolean("debug.debug-errors", true); }
}