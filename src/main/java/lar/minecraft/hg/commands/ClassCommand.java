package lar.minecraft.hg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.PlayerExtra;
import lar.minecraft.hg.enums.PlayerClass;
import lar.minecraft.hg.managers.PlayerManager;

public class ClassCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		PlayerClass cmdName = PlayerClass.valueOf(command.getName().toUpperCase());
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (SpigotPlugin.isLobby()) {
				if (cmdName != null) {
					PlayerExtra playerExtra = PlayerManager.playerExtras.get(player.getUniqueId());
					
					// Check command selection: only premium users or who won last match can use premium classes
					if (!cmdName.isPremium() || playerExtra.isPremium() || playerExtra.isLastWinner()) {
					    playerExtra.setPlayerClass(cmdName);
					    PlayerManager.playerExtras.put(player.getUniqueId(), playerExtra);
					    
					    player.sendMessage("You will be a " + cmdName.name().toLowerCase() + "!");
					    player.playSound(player, cmdName.getSound(), 10.0f, 10.0f);
					} else {
					    player.sendMessage("Choose another class.");
					    player.sendMessage("You must win a match or be premium to use this class!");
					}
				} else {
					player.sendMessage("There is no \"" + command.getName() + "\" class");
				}
			} else {
				player.sendMessage("Class selection is available only in LOBBY");
			}
		}
		
		return true;
	}
	
}
