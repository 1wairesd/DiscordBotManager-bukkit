package com.wairesd.discordbm.bukkit.model;

import java.util.List;

// Represents a registration message for commands.
public class RegisterMessage {
    public String type = "register";
    public String serverName;
    public String pluginName;
    public List<Command> commands;
    public String secret;

    public RegisterMessage(String serverName, String pluginName, List<Command> commands, String secret) {
        this.serverName = serverName;
        this.pluginName = pluginName;
        this.commands = commands;
        this.secret = secret;
    }
}