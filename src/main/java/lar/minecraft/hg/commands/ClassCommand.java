package lar.minecraft.hg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lar.minecraft.hg.MessageUtils;
import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.PlayerExtra;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.enums.PlayerClass;
import lar.minecraft.hg.managers.PlayerManager;

public class ClassCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		PlayerClass cmdName = PlayerClass.valueOf(command.getName().toLowerCase());

		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (cmdName != null && !PlayerManager.playerExtras.isEmpty() && (SpigotPlugin.isWaitingForStart() || SpigotPlugin.isLobby())) {
				PlayerExtra playerExtra = PlayerManager.playerExtras.get(player.getUniqueId());

				if (playerExtra != null) {
					// Check command selection: only premium users or who won last match can use premium classes
					if (!cmdName.isPremium() || playerExtra.isPremium() || playerExtra.isLastWinner()) {
						playerExtra.setPlayerClass(cmdName);
						PlayerManager.playerExtras.put(player.getUniqueId(), playerExtra);
						player.sendMessage(MessageUtils.getMessage(MessageKey.class_selected, cmdName.name()));
						player.playSound(player, cmdName.getSound(), 10.0f, 10.0f);
					} else {
						player.sendMessage(MessageUtils.getMessage(MessageKey.class_premium));
					}
				} else {
					return false;
				}
			} else {
				player.sendMessage(MessageUtils.getMessage(MessageKey.class_not_selected));
			}
		}
		return true;
	}

}
