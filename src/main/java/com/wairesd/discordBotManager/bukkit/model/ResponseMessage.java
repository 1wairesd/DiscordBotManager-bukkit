package com.wairesd.discordBotManager.bukkit.model;

// Represents a response message for a command request.
public record ResponseMessage(String type, String requestId, String response) {}