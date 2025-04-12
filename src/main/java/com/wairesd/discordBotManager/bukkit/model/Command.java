package com.wairesd.discordBotManager.bukkit.model;

import java.util.List;

// Represents a command with name, description, and options.
public class Command {
    public String name;
    public String description;
    public List<CommandOption> options;

    public Command(String name, String description, List<CommandOption> options) {
        this.name = name;
        this.description = description;
        this.options = options;
    }
}