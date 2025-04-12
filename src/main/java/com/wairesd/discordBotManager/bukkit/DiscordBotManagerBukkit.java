package com.wairesd.discordBotManager.bukkit;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.wairesd.discordBotManager.bukkit.api.DiscordBotManagerBukkitApi;
import com.wairesd.discordBotManager.bukkit.command.AdminCommand;
import com.wairesd.discordBotManager.bukkit.config.Messages;
import com.wairesd.discordBotManager.bukkit.config.Settings;
import com.wairesd.discordBotManager.bukkit.handle.DiscordCommandHandler;
import com.wairesd.discordBotManager.bukkit.model.ResponseMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

// Main plugin class for DiscordBotManager on Bukkit. Manages WebSocket communication and command handling.
public class DiscordBotManagerBukkit extends JavaPlugin {
    private static DiscordBotManagerBukkitApi api;
    private MyWebSocketClient wsClient;
    private final Map<String, DiscordCommandHandler> commandHandlers = new HashMap<>();
    private final Map<String, DiscordCommandRegistrationListener> registrationListeners = new HashMap<>();
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
            URI uri = new URI("ws://" + velocityHost + ":" + velocityPort);
            wsClient = new MyWebSocketClient(uri, this);
            wsClient.connect();
            getLogger().info("Plugin started on server: " + serverName);
        } catch (Exception e) {
            getLogger().severe("Failed to connect to Velocity WebSocket server: " + e.getMessage());
        }

        scheduleReconnection();
    }

    @Override
    public void onDisable() {
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.close();
        }
    }

    public void registerCommandHandler(String command, DiscordCommandHandler handler, DiscordCommandRegistrationListener listener) {
        commandHandlers.put(command, handler);
        if (listener != null) {
            registrationListeners.put(command, listener);
        }
    }

    public void sendResponse(String requestId, String response) {
        if (wsClient != null && wsClient.isOpen()) {
            ResponseMessage respMsg = new ResponseMessage("response", requestId, response);
            String json = gson.toJson(respMsg);
            wsClient.send(json);
        }
    }

    public void sendWebSocketMessage(String message) {
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.send(message);
        } else {
            getLogger().warning("WebSocket not connected. Message not sent: " + message);
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

    private void scheduleReconnection() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (wsClient == null || wsClient.isClosed()) {
                try {
                    URI uri = new URI("ws://" + Settings.getVelocityHost() + ":" + Settings.getVelocityPort());
                    wsClient = new MyWebSocketClient(uri, this);
                    wsClient.connect();
                    getLogger().info("Attempting to reconnect to WebSocket...");
                } catch (Exception e) {
                    getLogger().warning("Failed to reconnect to WebSocket: " + e.getMessage());
                }
            }
        }, 1200L, 1200L);
    }

    private void notifyPluginsOnConnect() {
        for (var entry : registrationListeners.entrySet()) {
            try {
                String command = entry.getKey();
                DiscordCommandRegistrationListener listener = entry.getValue();
                listener.onWebSocketConnected();
                getLogger().info("Notified plugin for command: " + command);
            } catch (Exception e) {
                getLogger().severe("Error notifying plugin for command " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    public static class MyWebSocketClient extends WebSocketClient {
        private final DiscordBotManagerBukkit plugin;

        public MyWebSocketClient(URI serverUri, DiscordBotManagerBukkit plugin) {
            super(serverUri);
            this.plugin = plugin;
            // Add the secret code to the headers for authentication
            addHeader("Secret", Settings.getVelocitySecret());
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            plugin.getLogger().info("Connected to Velocity WebSocket server.");
            plugin.notifyPluginsOnConnect();
        }

        @Override
        public void onMessage(String message) {
            try {
                var json = JsonParser.parseString(message).getAsJsonObject();
                if ("request".equals(json.get("type").getAsString())) {
                    String command = json.get("command").getAsString();
                    String requestId = json.get("requestId").getAsString();
                    var options = json.get("options").getAsJsonObject();
                    DiscordCommandHandler handler = plugin.getCommandHandlers().get(command);
                    if (handler != null) {
                        String[] args = options.entrySet().stream()
                                .map(e -> e.getValue().getAsString())
                                .toArray(String[]::new);
                        handler.handleCommand(command, args, requestId);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error processing message: " + e.getMessage());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            plugin.getLogger().info("Connection to Velocity WebSocket server closed: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            plugin.getLogger().severe("WebSocket client error: " + ex.getMessage());
        }
    }

    public void closeWebSocket() {
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.close();
        }
    }

    public void setWsClient(MyWebSocketClient client) {
        this.wsClient = client;
    }

    public interface DiscordCommandRegistrationListener {
        void onWebSocketConnected();
    }
}