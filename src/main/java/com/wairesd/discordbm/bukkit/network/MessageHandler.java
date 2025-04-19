package com.wairesd.discordbm.bukkit.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wairesd.discordbm.bukkit.DiscordBMB;
import com.wairesd.discordbm.bukkit.config.Settings;
import com.wairesd.discordbm.bukkit.handle.DiscordCommandHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles incoming string messages over the Netty channel,
 * parses them as JSON requests or error notices,
 * and dispatches commands to the appropriate handler.
 */
public class MessageHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final DiscordBMB plugin;
    private final Gson gson = new Gson();

    /**
     * @param plugin Main plugin instance, used to access command handlers and send responses.
     */
    public MessageHandler(DiscordBMB plugin) {
        this.plugin = plugin;
    }

    /**
     * Called whenever a full String message is received.
     * Distinguishes between error messages and JSON-formatted requests.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) {
        // Debug log of raw message if enabled
        if (Settings.isDebugClientResponses()) {
            logger.debug("Received message: {}", message);
        }

        // Handle plain-text error lines first
        if (message.startsWith("Error:")) {
            handleErrorMessage(message, ctx);
            return;
        }

        try {
            // Parse JSON and look for a "request" type
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();
            if ("request".equals(type)) {
                handleRequest(json);
            } else {
                logger.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            // Log parsing or handler errors if debug is enabled
            if (Settings.isDebugErrors()) {
                logger.error("Error processing message: {}", message, e);
            }
        }
    }

    /**
     * Processes server-sent error messages, flags invalid authentication,
     * and closes the connection if necessary.
     */
    private void handleErrorMessage(String message, ChannelHandlerContext ctx) {
        if (Settings.isDebugErrors()) {
            logger.warn("Received error from server: {}", message);
        }
        switch (message) {
            case "Error: Invalid secret code":
            case "Error: No secret code provided":
                plugin.setInvalidSecret(true);
                ctx.close();
                break;
            case "Error: Authentication timeout":
                if (Settings.isDebugAuthentication()) {
                    logger.warn("Authentication timeout occurred");
                }
                ctx.close();
                break;
            default:
                break;
        }
    }

    /**
     * Parses a JSON request object, extracts command name and options,
     * then invokes the corresponding DiscordCommandHandler.
     */
    private void handleRequest(JsonObject json) {
        String command = json.get("command").getAsString();
        String requestId = json.get("requestId").getAsString();

        // Build options map if provided
        Map<String, String> options = new HashMap<>();
        if (json.has("options")) {
            JsonObject optionsJson = json.get("options").getAsJsonObject();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : optionsJson.entrySet()) {
                options.put(entry.getKey(), entry.getValue().getAsString());
            }
        }

        // Dispatch to the handler or send a "not found" response
        DiscordCommandHandler handler = plugin.getCommandHandlers().get(command);
        if (handler != null) {
            String[] args = options.values().toArray(new String[0]);
            handler.handleCommand(command, args, requestId);
        } else {
            plugin.sendResponse(requestId, "Command not found.");
        }
    }

    /**
     * Handles unexpected exceptions on the channel by logging and closing the connection.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (Settings.isDebugErrors()) {
            logger.error("Connection error: {}", cause.getMessage(), cause);
        }
        ctx.close();
    }
}
