package lar.minecraft.hg.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lar.minecraft.hg.entities.PlayerExtra;
import lar.minecraft.hg.enums.PlayerClass;

public class PlayerClassManager {
			
	public static void giveClasses() {
		for(Player player : ServerManager.getLivingPlayers()) {
			PlayerInventory playerInventory = player.getInventory();
			playerInventory.clear();
			PlayerExtra playerExtra = PlayerManager.playerExtras.getOrDefault(player.getUniqueId(), null);
			if (playerExtra != null) {
				PlayerClass playerClass = playerExtra.getPlayerClass();
				if (playerClass != null) {
					playerClass.getAction().perform(player); // Give class items to players who chosen a class
				}
				if(playerClass == null || playerClass != PlayerClass.hardcore) {
					playerInventory.addItem(new ItemStack(Material.COMPASS)); // Give a compass to all players
				}
			}
			player.updateInventory();
		}
	}
}
