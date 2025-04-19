package com.wairesd.discordbm.bukkit;

import com.google.gson.Gson;
import com.wairesd.discordbm.bukkit.api.DiscordBotManagerBukkitApi;
import com.wairesd.discordbm.bukkit.command.AdminCommand;
import com.wairesd.discordbm.bukkit.config.Messages;
import com.wairesd.discordbm.bukkit.config.Settings;
import com.wairesd.discordbm.bukkit.handle.DiscordCommandHandler;
import com.wairesd.discordbm.bukkit.model.Command;
import com.wairesd.discordbm.bukkit.model.RegisterMessage;
import com.wairesd.discordbm.bukkit.model.ResponseMessage;
import com.wairesd.discordbm.bukkit.network.NettyClient;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Main plugin class for Bukkit, sets up Netty client and handles command registration.
public class DiscordBMB extends JavaPlugin {
    private static DiscordBotManagerBukkitApi api;
    private NettyClient nettyClient;
    private final Map<String, DiscordCommandHandler> commandHandlers = new HashMap<>();
    private final List<Command> addonCommands = new ArrayList<>();
    private String serverName;
    private final Gson gson = new Gson();
    private boolean invalidSecret = false;

    @Override
    public void onEnable() {
        api = new DiscordBotManagerBukkitApi(this);
        Settings.load(this);
        Messages.load(this);

        String velocityHost = Settings.getVelocityHost();
        int velocityPort = Settings.getVelocityPort();
        serverName = Settings.getServerName();

        getCommand("discordBMB").setExecutor(new AdminCommand(this));
        getCommand("discordBMB").setTabCompleter(new AdminCommand(this));

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                nettyClient = new NettyClient(new InetSocketAddress(velocityHost, velocityPort), this);
                nettyClient.connect();
            } catch (Exception e) {
                if (Settings.isDebugErrors()) {
                    getLogger().warning("Failed to connect to Velocity Netty server: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onDisable() {
        if (nettyClient != null && nettyClient.isActive()) nettyClient.close();
    }

    public void registerCommandHandler(String command, DiscordCommandHandler handler, DiscordCommandRegistrationListener listener, Command addonCommand) {
        commandHandlers.put(command, handler);
        if (addonCommand != null) {
            synchronized (addonCommands) {
                addonCommands.add(addonCommand);
                if (Settings.isDebugCommandRegistrations()) {
                    getLogger().info("Registered addon command: " + addonCommand.name);
                }
            }
        }
        if (listener != null && nettyClient != null && nettyClient.isActive()) {
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
            if (Settings.isDebugErrors()) {
                getLogger().warning("Netty connection not active. Message not sent: " + message);
            }
        }
    }

    public void sendAllAddonCommands() {
        synchronized (addonCommands) {
            if (addonCommands.isEmpty()) return;
            String secretCode = Settings.getSecretCode();
            RegisterMessage registerMsg = new RegisterMessage(serverName, getName(), addonCommands, secretCode);
            String json = gson.toJson(registerMsg);
            sendNettyMessage(json);
            if (Settings.isDebugCommandRegistrations()) {
                getLogger().info("Sent registration message for " + addonCommands.size() + " addon commands.");
            }
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
            if (Settings.isDebugConnections()) {
                getLogger().info("Netty connection closed via closeNettyConnection().");
            }
        }
    }

    public void setNettyClient(NettyClient newClient) {
        if (nettyClient != null && nettyClient.isActive()) {
            nettyClient.close();
        }
        nettyClient = newClient;
        if (Settings.isDebugConnections()) {
            getLogger().info("Netty client set to new instance.");
        }
        if (newClient.isActive()) {
            sendAllAddonCommands();
        }
    }

    public NettyClient getNettyClient() {
        return nettyClient;
    }

    public void setInvalidSecret(boolean invalid) {
        this.invalidSecret = invalid;
    }

    public interface DiscordCommandRegistrationListener {
        void onNettyConnected();
    }
}