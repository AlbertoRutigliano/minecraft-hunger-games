package lar.minecraft.hg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entity.PlayerClass;
import lar.minecraft.hg.entity.PlayerExtra;
import lar.minecraft.hg.managers.PlayerClassManager;

public class ClassCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmdName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (SpigotPlugin.isLobby()) {
				PlayerClass playerClass = PlayerClassManager.AVAILABLE_PLAYER_CLASSES.getOrDefault(cmdName, null);
				if (playerClass != null) {
					
					PlayerExtra playerExtra = new PlayerExtra(player.getUniqueId());
					playerExtra.setPlayerClass(playerClass);
					SpigotPlugin.playerExtras.putIfAbsent(player.getUniqueId(), playerExtra);
					
					player.sendMessage("You will be a " + cmdName + "!");
					player.playSound(player, playerClass.getSound(), 10.0f, 10.0f);
					
				} else {
					player.sendMessage("There is no \"" + cmdName + "\" class");
				}
			} else {
				player.sendMessage("Class selection is available only in LOBBY");
			}
		}
		
		return true;
	}
	
}
