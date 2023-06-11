package lar.minecraft.hg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import lar.minecraft.hg.ServerSchedulers;
import lar.minecraft.hg.SpigotPlugin;

public class TestCommand implements CommandExecutor {
    SpigotPlugin plugin;

    public TestCommand(SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        if (cmdName.equals("test")) {
        	sender.sendMessage("Test command");
        }
                
        if (cmdName.equals("start-hg")) {
        	new ServerSchedulers(plugin).lobbyPhase();
        }

        return true;
    }
}
