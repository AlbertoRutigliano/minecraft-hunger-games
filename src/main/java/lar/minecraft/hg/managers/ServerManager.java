package lar.minecraft.hg.managers;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lar.minecraft.hg.SpigotPlugin;

public class ServerManager {

	/**
	 * Send sound to all players
	 * 
	 * @param  sound  the sound to reproduce
	 */
	public static void sendSound(Sound sound) {
		for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
			p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
		}
	}

	
	public static ArrayList<Player> getLivingPlayers() {
		ArrayList<Player> livingPlayers = new ArrayList<>();
		for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
			if(!p.isDead() && !p.getGameMode().equals(GameMode.SPECTATOR)) {
				livingPlayers.add(p);
			}
		}
		return livingPlayers;
	}
	
	public static void giveClasses() {
		for(Player player : getLivingPlayers()) {
			if (SpigotPlugin.playerExtension.get(player) != null) {
				String chosenClass = SpigotPlugin.playerExtension.get(player).getChosenClass();
				PlayerInventory playerInventory = player.getInventory();
				playerInventory.clear();
				player.sendMessage("Giving " + chosenClass + " items");
				switch (chosenClass) {
					case "bowman":
						playerInventory.addItem(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16));
						break;
					case "armored":
						playerInventory.setHelmet(new ItemStack(Material.IRON_HELMET));
						playerInventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
						playerInventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
						playerInventory.setBoots(new ItemStack(Material.IRON_BOOTS));
						break;
					default:
						break;
				}
			}
		}
	}
}
