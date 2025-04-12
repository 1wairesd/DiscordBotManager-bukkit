package com.wairesd.discordBotManager.bukkit.util;

import org.bukkit.ChatColor;

// Utility class for translating color codes in messages for Bukkit.
public class Color {
    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}