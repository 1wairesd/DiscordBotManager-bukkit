package com.wairesd.discordbm.bukkit.command;

import com.wairesd.discordbm.bukkit.DiscordBMB;
import com.wairesd.discordbm.bukkit.network.NettyClient;
import com.wairesd.discordbm.bukkit.config.Messages;
import com.wairesd.discordbm.bukkit.config.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.net.InetSocketAddress;
import java.util.List;

// Handles the /discordbotmanager-bukkit command for reloading configurations.
public class AdminCommand implements CommandExecutor, TabCompleter {
    private final DiscordBMB plugin;

    public AdminCommand(DiscordBMB plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Messages.getMessage("usage-admin-command"));
            return true;
        }
        if (!sender.hasPermission("discordbotmanager.reload")) {
            sender.sendMessage(Messages.getMessage("no-permission"));
            return true;
        }

        Settings.load(plugin);
        Messages.load(plugin);

        plugin.closeNettyConnection();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String velocityHost = Settings.getVelocityHost();
                int velocityPort = Settings.getVelocityPort();
                NettyClient newClient = new NettyClient(new InetSocketAddress(velocityHost, velocityPort), plugin);
                plugin.setNettyClient(newClient);
                newClient.connect();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to recover to Netty: " + e.getMessage());
            }
        });

        sender.sendMessage(Messages.getMessage("reload-success"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }
}