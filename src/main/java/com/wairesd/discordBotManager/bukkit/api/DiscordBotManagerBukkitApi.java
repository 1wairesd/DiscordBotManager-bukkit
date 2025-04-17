package com.wairesd.discordBotManager.bukkit.api;

import com.google.gson.Gson;
import com.wairesd.discordBotManager.bukkit.DiscordBotManagerBukkit;
import com.wairesd.discordBotManager.bukkit.config.Settings;
import com.wairesd.discordBotManager.bukkit.handle.DiscordCommandHandler;
import com.wairesd.discordBotManager.bukkit.model.Command;
import com.wairesd.discordBotManager.bukkit.model.RegisterMessage;

import java.util.List;

// Provides an API for other Bukkit plugins to register commands and send responses via Netty.
public class DiscordBotManagerBukkitApi {
    private final DiscordBotManagerBukkit plugin;
    private final Gson gson = new Gson();

    public DiscordBotManagerBukkitApi(DiscordBotManagerBukkit plugin) {
        this.plugin = plugin;
    }

    public void registerCommand(Command command, DiscordCommandHandler handler, DiscordBotManagerBukkit.DiscordCommandRegistrationListener listener) {
        plugin.registerCommandHandler(command.name, handler, listener, command);
        if (plugin.getNettyClient() != null && plugin.getNettyClient().isActive()) {
            sendRegistrationMessage(command);
        }
    }

    private void sendRegistrationMessage(Command command) {
        String secretCode = Settings.getSecretCode();
        if (secretCode == null || secretCode.isEmpty()) {
            return;
        }

        RegisterMessage registerMsg = new RegisterMessage(
                plugin.getServerName(),
                command.pluginName,
                List.of(command),
                secretCode
        );
        String json = gson.toJson(registerMsg);
        plugin.sendNettyMessage(json);
        if (Settings.isDebugCommandRegistrations()) {
            plugin.getLogger().info("Sent registration message for command: " + command.name);
        }
    }

    public void sendResponse(String requestId, String response) {
        plugin.sendResponse(requestId, response);
    }

    public void sendNettyMessage(String message) {
        plugin.sendNettyMessage(message);
    }
}