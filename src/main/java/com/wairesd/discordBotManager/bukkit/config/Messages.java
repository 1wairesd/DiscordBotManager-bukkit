package com.wairesd.discordBotManager.bukkit.config;

import com.wairesd.discordBotManager.bukkit.util.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

// Manages loading and retrieving messages from messages.yml for Bukkit, with color translation.
public class Messages {
    private static FileConfiguration messagesConfig;

    public static void load(JavaPlugin plugin) {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try (InputStream in = plugin.getResource("messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                } else {
                    plugin.getLogger().warning("messages.yml not found in resources!");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save messages.yml: " + e.getMessage());
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public static String getMessage(String key) {
        String message = messagesConfig.getString(key, "Message not found.");
        return Color.translate(message);
    }
}