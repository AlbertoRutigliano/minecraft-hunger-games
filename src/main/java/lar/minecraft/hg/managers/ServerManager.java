package lar.minecraft.hg.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
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
			p.playSound(p, sound, 10.0f, 1.0f);
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
			PlayerInventory playerInventory = player.getInventory();
			playerInventory.clear();
			playerInventory.addItem(new ItemStack(Material.COMPASS));
			if (SpigotPlugin.playerExtension.get(player) != null) {
				String chosenClass = SpigotPlugin.playerExtension.get(player).getChosenClass();
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
					case "doglover":
						playerInventory.addItem(new ItemStack(Material.BONE, 8));
						Wolf dog = SpigotPlugin.server.getWorld("world").spawn(player.getLocation(), Wolf.class);
						dog.setOwner(player);
						dog.setAdult();
						dog.setSitting(false);
						break;
					default:
						break;
				}
			}
			player.updateInventory();
		}
	}
	
	public static void restartServer() {
		// Save all worlds before restarting
		Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(SpigotPlugin.class), () -> {
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "save-all");
			Bukkit.getScheduler().runTaskLater(SpigotPlugin.getPlugin(SpigotPlugin.class), () -> {
				saveRestartFlag();
				Bukkit.spigot().restart();
			}, 100L); // 5-second delay to ensure save completion
		});
	}
	
	private static void saveRestartFlag() {
    	File restartFlagFile = new File(SpigotPlugin.getPlugin(SpigotPlugin.class).getDataFolder(), "restart.flag");
        FileConfiguration restartFlagConfig = YamlConfiguration.loadConfiguration(restartFlagFile);
        try {
        	restartFlagConfig.save(restartFlagFile);
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
	
}
