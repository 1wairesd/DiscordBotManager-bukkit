package com.wairesd.discordbm.bukkit.api;

import com.wairesd.discordbm.bukkit.model.Command;
import com.wairesd.discordbm.bukkit.model.CommandOption;

import java.util.ArrayList;
import java.util.List;

// Helper class to build command definitions for registration.
public class CommandBuilder {
    private String name;
    private String description;
    private String pluginName;
    private String context = "both";
    private List<CommandOption> options = new ArrayList<>();

    public CommandBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder pluginName(String pluginName) {
        this.pluginName = pluginName;
        return this;
    }

    public CommandBuilder addChoice(String context) {
        if (context.equals("both") || context.equals("dm") || context.equals("server")) {
            this.context = context;
        } else {
            throw new IllegalArgumentException("Invalid context: " + context + ". Must be 'both', 'dm', or 'server'.");
        }
        return this;
    }

    public CommandBuilder addOption(String name, String type, String description, boolean required) {
        options.add(new CommandOption(name, type, description, required));
        return this;
    }

    public Command build() {
        return new Command(name, description, pluginName, context, options);
    }
}