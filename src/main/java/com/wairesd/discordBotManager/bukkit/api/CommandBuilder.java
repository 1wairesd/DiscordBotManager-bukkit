package com.wairesd.discordBotManager.bukkit.api;

import com.wairesd.discordBotManager.bukkit.model.Command;
import com.wairesd.discordBotManager.bukkit.model.CommandOption;

import java.util.ArrayList;
import java.util.List;

// Helper class to build command definitions for registration.
public class CommandBuilder {
    private String name;
    private String description;
    private List<CommandOption> options = new ArrayList<>();

    public CommandBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder addOption(String name, String type, String description, boolean required) {
        options.add(new CommandOption(name, type, description, required));
        return this;
    }

    public Command build() {
        return new Command(name, description, options);
    }
}