package com.wairesd.discordBotManager.bukkit.api;

import com.google.gson.Gson;
import com.wairesd.discordBotManager.bukkit.DiscordBotManagerBukkit;
import com.wairesd.discordBotManager.bukkit.handle.DiscordCommandHandler;
import com.wairesd.discordBotManager.bukkit.model.Command;
import com.wairesd.discordBotManager.bukkit.model.RegisterMessage;

import java.util.List;

// API for Bukkit plugins to interact with DiscordBotManager (register commands, send responses).
public class DiscordBotManagerBukkitApi {
    private final DiscordBotManagerBukkit plugin;
    private final Gson gson = new Gson();

    public DiscordBotManagerBukkitApi(DiscordBotManagerBukkit plugin) {
        this.plugin = plugin;
    }

    public void registerCommand(Command command, DiscordCommandHandler handler, DiscordBotManagerBukkit.DiscordCommandRegistrationListener listener) {
        plugin.registerCommandHandler(command.name, handler, listener);
        sendRegistrationMessage(command);
    }

    private void sendRegistrationMessage(Command command) {
        RegisterMessage registerMsg = new RegisterMessage(List.of(command));
        String json = gson.toJson(registerMsg);
        plugin.sendWebSocketMessage(json);
    }

    public void sendResponse(String requestId, String response) {
        plugin.sendResponse(requestId, response);
    }

    public void sendWebSocketMessage(String message) {
        plugin.sendWebSocketMessage(message);
    }
}