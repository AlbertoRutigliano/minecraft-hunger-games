package lar.minecraft.hg.managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.PlayerExtra;

public class PlayerClassManager {
			
	public static void giveClasses() {
		for(Player player : ServerManager.getLivingPlayers()) {
			PlayerInventory playerInventory = player.getInventory();
			playerInventory.clear();
			playerInventory.addItem(new ItemStack(Material.COMPASS)); // Give a compass to all players
			PlayerExtra playerExtra = PlayerManager.playerExtras.getOrDefault(player.getUniqueId(), null);
			if (playerExtra != null && playerExtra.getPlayerClass() != null) {
				playerExtra.getPlayerClass().getAction().perform(player); // Give class items to players who chosen a class
			}
			player.updateInventory();
		}
	}
}
