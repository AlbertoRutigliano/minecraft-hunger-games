package lar.minecraft.hg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import lar.minecraft.hg.ServerSchedulers;
import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.managers.ServerManager;

public class TestCommand implements CommandExecutor {
    
	SpigotPlugin plugin;

    public TestCommand(SpigotPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        if (cmdName.equals("test")) {
        	ServerSchedulers.spawnSupplyDrop();
        }
        
        if (cmdName.equals("restart-hg-server")) {
        	ServerManager.restartServer();
        }
                
        if (cmdName.equals("start-hg")) {
        	new ServerSchedulers(plugin).lobbyPhase();
        }
        
        if (cmdName.equals("phase")) {
        	sender.sendMessage("Current phase is " + SpigotPlugin.getPhase());
        }

        return true;
    }
    

}
