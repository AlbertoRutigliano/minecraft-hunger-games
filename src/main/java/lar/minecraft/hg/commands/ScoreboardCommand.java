package lar.minecraft.hg.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import lar.minecraft.hg.entities.PlayerExtra;
import lar.minecraft.hg.enums.Cmp;
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
			if (args[0].equalsIgnoreCase(Cmp.global)) {
				player.sendMessage(MessageUtils.getMessage(MessageKey.scoreboard_list_header_global));
				
				// Get global scoreboard
				Map<String, Integer> globalScoreboard = DatabaseManager.getGlobalScoreboard();
				// Order scoreboard, limit to 10 records and print it
				AtomicInteger index = new AtomicInteger(1);
				globalScoreboard.entrySet()
				    .stream()
				    .sorted(new Comparator<Entry<String, Integer>>() {
				        @Override
				        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				            return o2.getValue() - o1.getValue();
				        }
				    })
				    .limit(10)
				    .forEach((entry) -> {
				        int currentIndex = index.getAndIncrement();
				        player.sendMessage(MessageUtils.getMessage(getListRowMessageKey(currentIndex), String.valueOf(currentIndex), entry.getKey(), entry.getValue()));
				    });
		    	
			} else {
				return false;
			}
		} else {
			player.sendMessage(MessageUtils.getMessage(MessageKey.scoreboard_list_header));
			// Order scoreboard, limit to 10 records and print it
			AtomicInteger index = new AtomicInteger(1);
			PlayerManager.playerExtras.values().stream()
			    .sorted(new Comparator<PlayerExtra>() {
			        @Override
			        public int compare(PlayerExtra o1, PlayerExtra o2) {
			            return o2.getWinCount() - o1.getWinCount();
			        }
			    })
			    .limit(10)
			    .forEach(playerExtra -> {
			        int currentIndex = index.getAndIncrement();
			        player.sendMessage(MessageUtils.getMessage(getListRowMessageKey(currentIndex), String.valueOf(currentIndex), playerExtra.getName(), playerExtra.getWinCount()));
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
	
	private MessageKey getListRowMessageKey(int index) {
		MessageKey messageKey;
		switch (index) {
		    case 1:
		        messageKey = MessageKey.scoreboard_list_first_row;
		        break;
		    case 2:
		        messageKey = MessageKey.scoreboard_list_second_row;
		        break;
		    case 3:
		        messageKey = MessageKey.scoreboard_list_third_row;
		        break;
		    default:
		        messageKey = MessageKey.scoreboard_list_row;
		        break;
		}
		return messageKey;

	}

}
