package lar.minecraft.hg.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import lar.minecraft.hg.entities.PlayerExtra;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.managers.DatabaseManager;
import lar.minecraft.hg.managers.PlayerManager;
import lar.minecraft.hg.utils.MessageUtils;

public class ScoreboardCommand implements CommandExecutor, TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		
		if (args.length == 1) {
			// Global scoreboard
			if (args[0].equalsIgnoreCase("global")) {
				player.sendMessage(MessageUtils.getMessage(MessageKey.scoreboard_list_header));
				
				// Get global scoreboard
				Map<String, Integer> globalScoreboard = DatabaseManager.getGlobalScoreboard();
				// Order scoreboard, limit to 10 records and print it
		    	globalScoreboard.entrySet()
		    		.stream()
			    	.sorted(new Comparator<Entry<String, Integer>>() {
						@Override
						public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
							return o2.getValue() - o1.getValue();
						}
					})
			    	.limit(10)
			    	.forEach((x) -> {
			    		player.sendMessage(String.format("%s --> %d", x.getKey(), x.getValue()));
			    	});;
		    	
			} else {
				return false;
			}
		} else {
			player.sendMessage(MessageUtils.getMessage(MessageKey.scoreboard_list_header));
			
			// Order scoreboard, limit to 10 records and print it
			PlayerManager.playerExtras.values().stream()
				.sorted(new Comparator<PlayerExtra>() {
					@Override
					public int compare(PlayerExtra o1, PlayerExtra o2) {
		    			return o2.getWinCount() - o1.getWinCount();
					}
				})
				.limit(10)
				.forEach(x -> {
		    		player.sendMessage(String.format("%s --> %d", x.getName(), x.getWinCount()));
				});
		}
    	
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		
		if (args.length == 1) {
			completions.add("global");
			return completions;
		}
		
		Collections.sort(completions);
		return Collections.emptyList();
	}

}
