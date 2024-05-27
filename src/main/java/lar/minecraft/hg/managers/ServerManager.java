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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entity.PlayerExtra;

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
