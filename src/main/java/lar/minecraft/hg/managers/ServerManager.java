package lar.minecraft.hg.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.ItemStackProbability;
import lar.minecraft.hg.enums.ConfigProperty;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.utils.ConfigUtils;
import lar.minecraft.hg.utils.MessageUtils;

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
	
	public static void spawnSupplyDrop() {
		// Get the spawn location
        Location spawnLocation = SpigotPlugin.world.getSpawnLocation();

        // Generate random offsets for X and Z coordinates
        Random random = new Random();
        int worldMaxSize = ConfigUtils.getInt(ConfigProperty.world_border_max_size);
        int offsetX = random.nextInt((worldMaxSize/2)+1) - worldMaxSize/2; // Random value between -worldMaxSize/2 and worldMaxSize/2
        int offsetZ = random.nextInt((worldMaxSize/2)+1) - worldMaxSize/2; // Random value between -worldMaxSize/2 and worldMaxSize/2

        // Apply offsets to the spawn location
        Location randomLocation = spawnLocation.clone().add(offsetX, 0, offsetZ);

        // Find the highest block at the random location
        Location chestLocation = randomLocation.getWorld().getHighestBlockAt(randomLocation).getLocation().add(0, 1, 0);;
        
        // Create a chest block at the spawn location
        Block block = chestLocation.getBlock();
        block.setType(Material.CHEST);
        
        // Get the chest's inventory
        Chest chest = (Chest) block.getState();
        Inventory chestInventory = chest.getBlockInventory();
        
        do {
        	// Items that can spawn in a chest
            ArrayList<ItemStackProbability> items = new ArrayList<>();
            items.add(new ItemStackProbability(Material.IRON_SWORD, 0.10));
            items.add(new ItemStackProbability(Material.IRON_PICKAXE, 0.20));
            items.add(new ItemStackProbability(Material.GRASS_BLOCK, 0.20, 8, 24));
            items.add(new ItemStackProbability(Material.BREAD, 0.20, 6, 10));
            items.add(new ItemStackProbability(Material.IRON_INGOT, 0.15, 5, 13));
            items.add(new ItemStackProbability(Material.LAVA_BUCKET, 0.15));
            items.add(new ItemStackProbability(Material.WATER_BUCKET, 0.15));
            items.add(new ItemStackProbability(Material.DIAMOND_SWORD, 0.05));
            items.add(new ItemStackProbability(Material.ENDER_PEARL, 0.25, 4, 12));

            /* This code should add empty spaces in the chest but seems not working
            int itemsToAdd = items.size();
            for (int i = itemsToAdd; i < 27; i++) { // 27 is the max inventory size
            	items.add(new ItemStackProbability(Material.AIR, 1.0)); // Add an empty slot
            }*/
            
            Collections.shuffle(items);
            items.forEach(i-> chestInventory.addItem(i));
        } while (chestInventory.isEmpty()); // To make sure that the chest is not completely empty
        
        ServerManager.sendSound(Sound.BLOCK_BELL_USE);
        chest.getWorld().strikeLightning(chestLocation);
        Bukkit.broadcastMessage(MessageUtils.getMessage(MessageKey.supply_drop, chestLocation.getX(), chestLocation.getY(), chestLocation.getZ()));
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
