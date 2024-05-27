package lar.minecraft.hg.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import lar.minecraft.hg.entity.PlayerClass;
import lar.minecraft.hg.entity.PlayerExtra;

public class ServerManager {
	
	public static final Map<String, PlayerClass> AVAILABLE_PLAYER_CLASSES = new HashMap<>();
	
	public static void initPlayerClasses() {
		AVAILABLE_PLAYER_CLASSES.put("bowman", new PlayerClass("bowman", Sound.ITEM_CROSSBOW_HIT));
		AVAILABLE_PLAYER_CLASSES.put("armored", new PlayerClass("armored", Sound.ITEM_ARMOR_EQUIP_IRON));
		AVAILABLE_PLAYER_CLASSES.put("doglover", new PlayerClass("doglover", Sound.ENTITY_WOLF_PANT));
		AVAILABLE_PLAYER_CLASSES.put("lavaman", new PlayerClass("lavaman", Sound.ITEM_BUCKET_FILL_LAVA));
	}
	
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
			PlayerExtra playerExtra = SpigotPlugin.playerExtras.getOrDefault(player.getUniqueId(), null);
			if (playerExtra != null && AVAILABLE_PLAYER_CLASSES.containsKey(playerExtra.getPlayerClass().getName())) {
				String chosenClass = playerExtra.getPlayerClass().getName();
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
					case "lavaman":
						playerInventory.addItem(new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET));
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
