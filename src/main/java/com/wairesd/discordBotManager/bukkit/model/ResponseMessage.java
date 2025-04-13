package com.wairesd.discordBotManager.bukkit.model;

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