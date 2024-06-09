package lar.minecraft.hg.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import lar.minecraft.hg.ServerSchedulers;
import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.ItemStackProbability;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.enums.PlayerClass;
import lar.minecraft.hg.utils.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

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
		// Generate random location inside world border for the chest
        Location chestLocation = getSurfaceRandomLocation(ServerSchedulers.getWorldBorderSize(), SpigotPlugin.newSpawnLocation, 0, 1, 0);
        
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
            
            Collections.shuffle(items);
            items.forEach(i-> chestInventory.addItem(i));
        } while (chestInventory.isEmpty()); // To make sure that the chest is not completely empty
        
        ServerManager.sendSound(Sound.BLOCK_BELL_USE);
        chest.getWorld().strikeLightning(chestLocation);
        Bukkit.broadcastMessage(MessageUtils.getMessage(MessageKey.supply_drop, chestLocation.getX(), chestLocation.getY(), chestLocation.getZ()));
	}
	
	public static Location getSurfaceRandomLocation(int range, Location startingLocation, int xOffset, int yOffset, int zOffset) {
		// Generate random offsets for X and Z coordinates
		Random random = ThreadLocalRandom.current();
		int offsetX = random.nextInt(range/2) - random.nextInt(range/2); // Random value between -range/2 and range/2
		int offsetZ = random.nextInt(range/2) - random.nextInt(range/2); // Random value between -range/2 and range/2

		// Apply offsets to the spawn location
		Location randomLocation = startingLocation.clone().add(offsetX, 0, offsetZ);

		// Find the highest block at the random location
		Location resultLocation = randomLocation.getWorld().getHighestBlockAt(randomLocation).getLocation().add(xOffset, yOffset, zOffset);
		
		return(resultLocation);
	}
	
	/**
	 * Get a book with the list of possible classes
	 * Each class is in a single page
	 * @return An book with the instruction and materials for each class
	 */
	public static ItemStack getGameInstructionsBook() {
		// Prepare the book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

		// Get book meta to set attributes
	    BookMeta meta = (BookMeta) book.getItemMeta();
	    meta.setTitle("Instructions");
	    meta.setAuthor(SpigotPlugin.class.getName());
		
		StringBuilder commandInstructions = new StringBuilder();
		
		Arrays.asList(PlayerClass.values()).forEach(c -> {
			// Empty the text that will be added to the page for each page in order to have a clear one
			// Add the command description, if it is premium and command to select it
			commandInstructions.setLength(0);
			commandInstructions.append(c.getDescription());
			commandInstructions.append("\n\n");
			if (c.isPremium()) {
				commandInstructions.append(MessageUtils.getMessage(MessageKey.class_instructions_premium));
				commandInstructions.append("\n\n");	
			}
			commandInstructions.append(MessageUtils.getMessage(MessageKey.class_instructions, c.name()));
			commandInstructions.append("\n");
			
			// Get class materials and append to the command instructions
			Map<String, Integer> classMaterials = c.getMaterials();
			classMaterials.forEach((x, y) -> {
				commandInstructions.append(MessageUtils.getMessage(MessageKey.class_instructions_materials, x, y));
				commandInstructions.append("\n");
			});
			
			// Make the text clickable -> player can click on the text instead of writing class command
			BaseComponent[] page = new ComponentBuilder(commandInstructions.toString())
			        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/class %s", c.name())))
			        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(MessageUtils.getMessage(MessageKey.class_instructions_select))))
			        .create();
			
		    meta.spigot().addPage(page);
        });
		
	    book.setItemMeta(meta);
		
		return book;
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
