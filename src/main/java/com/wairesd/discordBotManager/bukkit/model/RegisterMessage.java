package com.wairesd.discordBotManager.bukkit.model;

import java.util.List;

// Represents a registration message for commands.
public class RegisterMessage {
    public String type = "register";
    public List<Command> commands;
    public String secret; // New field for the secret code

    // Updated constructor that accepts the secret code
    public RegisterMessage(List<Command> commands, String secret) {
        this.commands = commands;
        this.secret = secret;
    }
}
