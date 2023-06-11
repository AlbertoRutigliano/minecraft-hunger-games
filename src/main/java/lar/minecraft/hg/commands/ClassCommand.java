package lar.minecraft.hg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lar.minecraft.hg.PlayerExt;
import lar.minecraft.hg.SpigotPlugin;

public class ClassCommand implements CommandExecutor {
    
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmdName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			// TODO Check if cmdName is an available class
			 if (SpigotPlugin.isLobby()) {
					player.sendMessage("You will be a " + cmdName + "!");
					PlayerExt playerExt = new PlayerExt();
					playerExt.setChosenClass(cmdName);
					SpigotPlugin.playerExtension.put(player, playerExt);
				} else {
					player.sendMessage("Class selection is available only in LOBBY");
				}
		}
		return true;
	}
	
}
