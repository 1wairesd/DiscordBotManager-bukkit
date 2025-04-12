package com.wairesd.discordBotManager.bukkit.model;

import java.util.List;

// Represents a registration message for commands.
public class RegisterMessage {
    public String type = "register";
    public List<Command> commands;

    public RegisterMessage(List<Command> commands) {
        this.commands = commands;
    }
}