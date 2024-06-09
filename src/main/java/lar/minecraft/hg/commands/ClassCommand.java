package lar.minecraft.hg.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.PlayerExtra;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.enums.PlayerClass;
import lar.minecraft.hg.managers.PlayerManager;
import lar.minecraft.hg.utils.MessageUtils;

public class ClassCommand implements CommandExecutor, TabExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (args.length == 1) {
				PlayerClass className = PlayerClass.valueOf(args[0].toLowerCase());

				if (className != null && !PlayerManager.playerExtras.isEmpty() && (SpigotPlugin.isWaitingForStart() || SpigotPlugin.isLobby())) {
					PlayerExtra playerExtra = PlayerManager.playerExtras.get(player.getUniqueId());

					if (playerExtra != null) {
						// Check command selection: only premium users or who won last match can use premium classes
						if (!className.isPremium() || playerExtra.isPremium() || playerExtra.isLastWinner()) {
							playerExtra.setPlayerClass(className);
							PlayerManager.playerExtras.put(player.getUniqueId(), playerExtra);
							player.sendMessage(MessageUtils.getMessage(MessageKey.class_selected, className.name()));
							player.playSound(player, className.getSound(), 10.0f, 10.0f);
						} else {
							player.sendMessage(MessageUtils.getMessage(MessageKey.class_premium));
						}
					} else {
						player.sendMessage(MessageUtils.getMessage(MessageKey.class_not_selected));
						return true;
					}
				} else {
					player.sendMessage(MessageUtils.getMessage(MessageKey.class_not_selected));
				}
			} else {
				player.sendMessage(MessageUtils.getMessage(MessageKey.class_not_selected));
				return true;
			}
		}
		
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		
		if (args.length == 1) {
			Arrays.asList(PlayerClass.values()).forEach(c -> {
				completions.add(c.name());
	        });
			
			return completions;
		}
		
		Collections.sort(completions);
		return Collections.emptyList();
	}
}
