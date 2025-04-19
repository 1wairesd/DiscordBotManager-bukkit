package com.wairesd.discordbm.bukkit.handle;

// Interface for handling Discord commands received via Netty.
public interface DiscordCommandHandler {
    void handleCommand(String command, String[] args, String requestId);
}