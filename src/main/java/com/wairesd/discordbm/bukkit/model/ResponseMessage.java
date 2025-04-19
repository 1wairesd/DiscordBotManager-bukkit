package com.wairesd.discordbm.bukkit.model;

/**
 * Represents a response message sent from the Bukkit server back to the Discord bot.
 * Contains the original request ID and the response content.
 */
public class ResponseMessage {
    public String type = "response";
    public String requestId;
    public String response;

    public ResponseMessage(String type, String requestId, String response) {
        this.type = type;
        this.requestId = requestId;
        this.response = response;
    }
}