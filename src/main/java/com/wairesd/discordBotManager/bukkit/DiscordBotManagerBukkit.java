package com.wairesd.discordBotManager.bukkit;

import com.google.gson.Gson;
import com.wairesd.discordBotManager.bukkit.api.DiscordBotManagerBukkitApi;
import com.wairesd.discordBotManager.bukkit.command.AdminCommand;
import com.wairesd.discordBotManager.bukkit.config.Messages;
import com.wairesd.discordBotManager.bukkit.config.Settings;
import com.wairesd.discordBotManager.bukkit.handle.DiscordCommandHandler;
import com.wairesd.discordBotManager.bukkit.model.ResponseMessage;
import com.wairesd.discordBotManager.bukkit.network.NettyClient;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

// Main plugin class for Bukkit, sets up Netty client and handles command registration.
public class DiscordBotManagerBukkit extends JavaPlugin {
    private static DiscordBotManagerBukkitApi api;
    private NettyClient nettyClient;
    private final Map<String, DiscordCommandHandler> commandHandlers = new HashMap<>();
    private String serverName;
    private final Gson gson = new Gson();

    @Override
    public void onEnable() {
        api = new DiscordBotManagerBukkitApi(this);
        Settings.load(this);
        Messages.load(this);

        String velocityHost = Settings.getVelocityHost();
        int velocityPort = Settings.getVelocityPort();
        serverName = Settings.getServerName();

        getCommand("discordbotmanager-bukkit").setExecutor(new AdminCommand(this));
        getCommand("discordbotmanager-bukkit").setTabCompleter(new AdminCommand(this));

        try {
            nettyClient = new NettyClient(new InetSocketAddress(velocityHost, velocityPort), this);
            nettyClient.connect();
        } catch (Exception e) {
            getLogger().warning("Failed to connect to Velocity Netty server: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (nettyClient != null && nettyClient.isActive()) nettyClient.close();
    }

    public void registerCommandHandler(String command, DiscordCommandHandler handler, DiscordCommandRegistrationListener listener) {
        commandHandlers.put(command, handler);
        if (listener != null) {
            listener.onNettyConnected();
        }
    }

    public void sendResponse(String requestId, String response) {
        if (nettyClient != null && nettyClient.isActive()) {
            ResponseMessage respMsg = new ResponseMessage("response", requestId, response);
            String json = gson.toJson(respMsg);
            nettyClient.send(json);
        }
    }

    public void sendNettyMessage(String message) {
        if (nettyClient != null && nettyClient.isActive()) {
            nettyClient.send(message);
        } else {
            getLogger().warning("Netty connection not active. Message not sent: " + message);
        }
    }

    public Map<String, DiscordCommandHandler> getCommandHandlers() {
        return commandHandlers;
    }

    public String getServerName() {
        return serverName;
    }

    public static DiscordBotManagerBukkitApi getApi() {
        return api;
    }

    public void closeNettyConnection() {
        if (nettyClient != null && nettyClient.isActive()) {
            nettyClient.close();
            nettyClient = null;
            getLogger().info("Netty connection closed via closeNettyConnection().");
        }
    }

    public void setNettyClient(NettyClient newClient) {
        if (nettyClient != null && nettyClient.isActive()) {
            nettyClient.close();
        }
        nettyClient = newClient;
        getLogger().info("Netty client set to new instance.");
    }

    public interface DiscordCommandRegistrationListener {
        void onNettyConnected();
    }
}
