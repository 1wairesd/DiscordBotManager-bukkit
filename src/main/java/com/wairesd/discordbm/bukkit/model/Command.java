package com.wairesd.discordbm.bukkit.model;

import java.util.List;

// Represents a command with name, description, and options.
public class Command {
    public String name;
    public String description;
    public String pluginName;
    public String context;
    public List<CommandOption> options;

    public Command(String name, String description, String pluginName, String context, List<CommandOption> options) {
        this.name = name;
        this.description = description;
        this.pluginName = pluginName;
        this.context = context;
        this.options = options;
    }
}