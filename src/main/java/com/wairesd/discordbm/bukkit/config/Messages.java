package com.wairesd.discordbm.bukkit.config;

import com.wairesd.discordbm.bukkit.util.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Manages messages from messages.yml for Bukkit with color translation.
 */
public class Messages {
    private static final Logger logger = LoggerFactory.getLogger(Messages.class);
    private static FileConfiguration messagesConfig;

    /** Loads messages.yml asynchronously with fallback creation. */
    public static void load(JavaPlugin plugin) {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try (InputStream in = plugin.getResource("messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                } else {
                    plugin.getLogger().warning("messages.yml not found in resources");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save messages.yml: " + e.getMessage());
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /**
     * Retrieves a translated message by key.
     * @param key the message key
     * @return the translated message or fallback
     */
    public static String getMessage(String key) {
        String message = messagesConfig != null ? messagesConfig.getString(key, "Message not found.") : "Message not found.";
        return Color.translate(message);
    }
}