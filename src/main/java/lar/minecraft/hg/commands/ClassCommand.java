package lar.minecraft.hg.commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lar.minecraft.hg.PlayerExt;
import lar.minecraft.hg.SpigotPlugin;

public class ClassCommand implements CommandExecutor {
	
	public static final ArrayList<String> AVAILABLE_CLASSES = new ArrayList<>(Arrays.asList(new String[] {
			"bowman", "armored", "doglover"
	}));
    
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmdName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (SpigotPlugin.isLobby()) {
				if (AVAILABLE_CLASSES.contains(cmdName)) {
					player.sendMessage("You will be a " + cmdName + "!");
					PlayerExt playerExt = new PlayerExt();
					playerExt.setChosenClass(cmdName);
					SpigotPlugin.playerExtension.put(player, playerExt);
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
