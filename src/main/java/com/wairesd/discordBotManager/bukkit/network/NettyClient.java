package com.wairesd.discordBotManager.bukkit.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wairesd.discordBotManager.bukkit.DiscordBotManagerBukkit;
import com.wairesd.discordBotManager.bukkit.config.Settings;
import com.wairesd.discordBotManager.bukkit.handle.DiscordCommandHandler;
import com.wairesd.discordBotManager.bukkit.model.Command;
import com.wairesd.discordBotManager.bukkit.model.RegisterMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the Netty client connection to Velocity from Bukkit.
 */
public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private boolean closing = false;
    private boolean invalidSecret = false;
    private final InetSocketAddress address;
    private final JavaPlugin plugin;
    private EventLoopGroup group;
    private Channel channel;
    private final Gson gson = new Gson();

    public NettyClient(InetSocketAddress address, JavaPlugin plugin) {
        this.address = address;
        this.plugin = plugin;
    }

    /** Closes the Netty client connection. */
    public void close() {
        closing = true;
        if (channel != null) channel.close();
        if (group != null) group.shutdownGracefully();
        logger.info("Netty client connection closed");
    }

    /** Establishes a connection to the Velocity server. */
    public void connect() throws InterruptedException {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                        ch.pipeline().addLast("stringDecoder", new StringDecoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
                        ch.pipeline().addLast("stringEncoder", new StringEncoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast("handler", new NettyClientHandler());
                    }
                });

        ChannelFuture future = bootstrap.connect(address).sync();
        if (future.isSuccess()) {
            channel = future.channel();
            logger.info("Connected to Velocity Netty server at {}:{}", address.getHostString(), address.getPort());

            DiscordBotManagerBukkit discordPlugin = (DiscordBotManagerBukkit) plugin;
            List<Command> commands = discordPlugin.getCommandHandlers().keySet().stream()
                    .map(name -> new Command(name, "Description", List.of()))
                    .toList();
            String secretCode = Settings.getSecretCode();
            RegisterMessage registerMsg = new RegisterMessage(commands, secretCode);
            String json = gson.toJson(registerMsg);
            send(json);
        } else {
            logger.warn("Failed to connect to Velocity at {}:{}: {}", address.getHostString(), address.getPort(), future.cause().getMessage());
        }
    }

    public boolean isActive() { return channel != null && channel.isActive(); }

    /** Sends a message to the server if the channel is active. */
    public void send(String message) {
        if (isActive()) {
            channel.writeAndFlush(message);
            if (Settings.isDebug()) logger.debug("Sent message: {}", message);
        } else {
            logger.warn("Cannot send message, channel inactive");
        }
    }

    private class NettyClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String message) {
            if ("Error: Invalid secret code".equals(message) || "Error: No secret code provided".equals(message)) {
                invalidSecret = true;
                logger.warn("Connection rejected: {}", message);
                ctx.close();
                return;
            }
            try {
                JsonObject json = gson.fromJson(message, JsonObject.class);
                String type = json.get("type").getAsString();
                if ("request".equals(type)) {
                    String command = json.get("command").getAsString();
                    String requestId = json.get("requestId").getAsString();
                    Map<String, String> options = new HashMap<>();
                    if (json.has("options")) {
                        JsonObject optionsJson = json.get("options").getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : optionsJson.entrySet()) {
                            options.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                    DiscordBotManagerBukkit discordPlugin = (DiscordBotManagerBukkit) plugin;
                    DiscordCommandHandler handler = discordPlugin.getCommandHandlers().get(command);
                    if (handler != null) {
                        String[] args = options.values().toArray(new String[0]);
                        handler.handleCommand(command, args, requestId);
                    } else {
                        discordPlugin.sendResponse(requestId, "Command not found.");
                    }
                } else {
                    logger.warn("Unknown message type: {}", type);
                }
            } catch (Exception e) {
                logger.error("Error processing message: {}", message, e);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (closing) {
                logger.info("Netty client closed intentionally");
            } else if (!invalidSecret) {
                logger.warn("Connection closed unexpectedly");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Connection error: {}", cause.getMessage(), cause);
            ctx.close();
        }
    }
}