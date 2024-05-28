package lar.minecraft.hg;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.FireworkMeta;

import lar.minecraft.hg.enums.HGPhase;
import lar.minecraft.hg.managers.DatabaseManager;
import lar.minecraft.hg.managers.PlayerClassManager;
import lar.minecraft.hg.managers.PlayerManager;
import lar.minecraft.hg.managers.ServerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ServerSchedulers {

	private FileConfiguration config;
	private World world;
	private Server server;
	private int worldBorderSize = 256;
	private int worldBorderMinumumSize = 32;
	private int idleTimeout = 2;
	
	public ServerSchedulers(SpigotPlugin plugin) {
		this.config = plugin.getConfig();
		this.server = plugin.getServer();
		this.world = server.getWorld("world");
		this.worldBorderSize = config.getInt("world-border.max-size", worldBorderSize);
		this.worldBorderMinumumSize = config.getInt("world-border.min-size", worldBorderMinumumSize);
		this.idleTimeout = config.getInt("durations.idle-timeout");
	}
	
	private static int currentHGGameId = 0;
	
	private static long gameStartTime = 0;
	private static long safeAreaTime = 0;
	private static long winnerCelebrationsTime = 0;
	private static long fireworksEffectsTime = 0;
	private static long worldBorderCollapseTime = 0;
	private static long supplyDropTime = 0;
	
	private static int waitingPhaseTaskId = -1;
	private static int lobbyPhaseTaskId = -1;
	private static int safeAreaPhaseTaskId = -1;
	private static int playingPhaseTaskId = -1;
	private static int fireworksEffectsTaskId = -1;
	private static int supplyDropTaskId = -1;
	
	private final static int WORLD_BORDER_COLLAPSE_COUNTER_SECONDS = 60; // TODO May be added on config.yml
	private final static int WORLD_BORDER_COLLAPSE_RADIUS = 20; // TODO May be added on config.yml
	
	public void waitingPhase() {
		SpigotPlugin.setPhase(HGPhase.WAITING_FOR_HG);
		ServerManager.getLivingPlayers().forEach(p -> {
			p.setGameMode(GameMode.ADVENTURE);
			p.getInventory().clear();
		});
		waitingPhaseTaskId = server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			int minimumPlayers = config.getInt("min-players", 3);
			@Override
			public void run() {
				if (SpigotPlugin.server.getOnlinePlayers().size() < minimumPlayers) {
					for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
						p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								TextComponent.fromLegacyText("The game starts when the minimum number of players is reached: " + SpigotPlugin.server.getOnlinePlayers().size() + "/" + minimumPlayers));
					}
				} else {
					lobbyPhase();
					server.getScheduler().cancelTask(waitingPhaseTaskId);
				}
				
			}
			
		}, 20, 20); // 1 second = 20 ticks
	}
	
	public void lobbyPhase() {
		SpigotPlugin.setPhase(HGPhase.LOBBY);
		ServerManager.getLivingPlayers().forEach(p -> {
			p.setGameMode(GameMode.ADVENTURE);
			p.getInventory().clear();
		});
		gameStartTime = 0;
		lobbyPhaseTaskId = server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				long execTime = world.getTime();
				
				int minimumPlayers = config.getInt("min-players", 3);
				if (SpigotPlugin.server.getOnlinePlayers().size() < minimumPlayers) {
					waitingPhase();
					server.getScheduler().cancelTask(lobbyPhaseTaskId);
				}
				
				// First runnable run
				if (gameStartTime == 0) {
					gameStartTime = execTime + (20 * config.getInt("durations.lobby", 30)); // Game will start in X seconds
				}
				long passedSeconds = (execTime - gameStartTime) / 20;
				for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
					p.spigot().sendMessage(
							ChatMessageType.ACTION_BAR, 
							TextComponent.fromLegacyText("Game will start in " + Math.abs(passedSeconds) + " seconds"));
				}
				if (passedSeconds == 0) {
					safeAreaPhase();
					server.getScheduler().cancelTask(lobbyPhaseTaskId);
				}
			}
		}, 20, 20); // 1 second = 20 ticks
	}
	
	public void safeAreaPhase() {
		SpigotPlugin.setPhase(HGPhase.SAFE_AREA);
		safeAreaTime = 0;

		// Register new HungerGames game on Database
		currentHGGameId = DatabaseManager.createHGGame(SpigotPlugin.serverId);
		
		// Notify all players that Hunger Games is starting
		ServerManager.getLivingPlayers().forEach(p -> {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("It's starting the Hunger Games!"));
			p.setGameMode(GameMode.SURVIVAL);
			p.teleport(world.getSpawnLocation());
			
			// Write player join on Database
			DatabaseManager.addPlayerJoin(config.getInt("server.id", 1), currentHGGameId, p);
		});
		PlayerClassManager.giveClasses();
		ServerManager.sendSound(Sound.EVENT_RAID_HORN);
		
		safeAreaPhaseTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				long execTime = world.getTime();
				// First runnable run
				if (safeAreaTime == 0) {
					safeAreaTime = execTime + (20 * config.getInt("durations.safe-area", 40));
				}
				long passedSeconds = (execTime - safeAreaTime) / 20;

				ServerManager.getLivingPlayers().forEach(p -> p.spigot().sendMessage(
						ChatMessageType.ACTION_BAR, 
						TextComponent.fromLegacyText("Safe area expires in " + Math.abs(passedSeconds) + " seconds")));
				if (passedSeconds == 0) {
					ServerManager.getLivingPlayers().forEach(p -> p.spigot().sendMessage(
							ChatMessageType.ACTION_BAR, 
							TextComponent.fromLegacyText("It's Hunger Games tiiiiiiiiime!")));
					playingPhase();
					server.getScheduler().cancelTask(safeAreaPhaseTaskId);
				}
				
			}
		}, 20, 20); // 1 second = 20 ticks
		
	}
	
	public void playingPhase() {
		SpigotPlugin.setPhase(HGPhase.PLAYING);
		worldBorderCollapseTime = 0;
		winnerCelebrationsTime = 0;
		server.setIdleTimeout(idleTimeout);
		playingPhaseTaskId = server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				long execTime = world.getTime();
				
				// WORLD BORDER COLLAPSE
				// First runnable run
				if(worldBorderCollapseTime == 0) {
					worldBorderCollapseTime = execTime + (20 * WORLD_BORDER_COLLAPSE_COUNTER_SECONDS);
				}
				long passedSecondsForWorldBorderCollapse = (execTime - worldBorderCollapseTime) / 20;
				if(passedSecondsForWorldBorderCollapse == 0) {
					if (worldBorderSize > config.getInt("world-border.min-size", 32)) {
						worldBorderSize = worldBorderSize - WORLD_BORDER_COLLAPSE_RADIUS;
						world.getWorldBorder().setSize(worldBorderSize, WORLD_BORDER_COLLAPSE_COUNTER_SECONDS);
						world.getWorldBorder().setDamageBuffer(0);
						world.getWorldBorder().setDamageAmount(0.2);
						worldBorderCollapseTime = 0; 
					}
				}
				
				// COMPASS TRACKING
				for (Player player : ServerManager.getLivingPlayers()) {
					PlayerInventory inventory = player.getInventory();
	                for (int i = 0; i < 36; i++) {
	                	ItemStack item = inventory.getItem(i);
	                	if (item != null && item.getType() != null && !item.getType().isAir() && item.getType().equals(Material.COMPASS)) {
	                		final CompassMeta compassMeta = (CompassMeta) item.getItemMeta();	  
	                		if (PlayerManager.getNearestPlayer(player, 200) != null) {
	                			compassMeta.setDisplayName("Following " + PlayerManager.getNearestPlayer(player, 200).getName());
	                		}
                            player.getInventory().getItem(i).setItemMeta(compassMeta);
	                	}
	                }
	                if (PlayerManager.getNearestPlayer(player, 200) != null) {
                		player.setCompassTarget(PlayerManager.getNearestPlayer(player, 200).getLocation());     
                	}
				}
				
				// LAST PLAYER WINNER
				if (ServerManager.getLivingPlayers().size() == 1) {
					// First runnable run
					if(winnerCelebrationsTime == 0) {
						SpigotPlugin.setPhase(HGPhase.WINNING);
						Player winner = ServerManager.getLivingPlayers().iterator().next();
						SpigotPlugin.server.broadcastMessage(winner.getName() + " wins the Hunger Games!");
						winner.sendTitle("You win the Hunger Games!", null, 10, 70, 20);
						winnerCelebrationsTime = execTime + (20 * config.getInt("durations.winner-celebrations", 20));

						// Save the winning player on Database
						DatabaseManager.savePlayerWin(config.getInt("server.id", 1), currentHGGameId, winner);
						fireworkEffect(winner);
					}
					
					long passedSeconds = (execTime - winnerCelebrationsTime) / 20;
					
					for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
						p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								TextComponent.fromLegacyText("A new Hunger Games will start in " + Math.abs(passedSeconds) + " seconds. Server will be restarted"));
					}
					if (passedSeconds == 0) {
						SpigotPlugin.setPhase(HGPhase.WAITING_FOR_HG);
						if (config.getBoolean("server.auto-restart", false)) {
							ServerManager.restartServer();
						}
						SpigotPlugin.server.getScheduler().cancelTask(playingPhaseTaskId);
					}
				}	
			}
			
			
			
		}, 20, 20); // 1 second = 20 ticks
		
		supplyDrop();
		
	}
	
	public static void currentPhaseLogger() {
		SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				SpigotPlugin.server.getLogger().info("Phase: " + SpigotPlugin.getPhase().toString());
			}
		}, 20, 20); // 1 second = 20 ticks
	}
	
	private void fireworkEffect(Player winner) {
		fireworksEffectsTime = 0;
		fireworksEffectsTaskId = server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			@Override
			public void run() {
				long execTime = world.getTime();
				if (fireworksEffectsTime == 0) {
					fireworksEffectsTime = execTime + (20 * config.getInt("durations.fireworks", 20));
				}
				long passedSeconds = (execTime - fireworksEffectsTime) / 20;
				
				if (passedSeconds % 4 == 0) {
					Firework firework = world.spawn(winner.getLocation(), Firework.class);
					FireworkMeta data = firework.getFireworkMeta();
					data.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().build());
					data.setPower(1);
					firework.setFireworkMeta(data);
				}
				
				if (passedSeconds == 0) {
					server.getScheduler().cancelTask(fireworksEffectsTaskId);
				}
			}
			
		}, 20, 20); // 1 second = 20 ticks	
	}
	
	private void supplyDrop() {
		supplyDropTime = 0;
		supplyDropTaskId = server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			@Override
			public void run() {
				long execTime = world.getTime();
				
				// Spawn a supply drop chest after durations.supply-drop seconds
				if (supplyDropTime == 0) {
					supplyDropTime = execTime + (20 * config.getInt("durations.supply-drop", 10));
				}
				
				long passedSeconds = (execTime - supplyDropTime) / 20;
				
				if (Math.abs(passedSeconds) <= 10) {
					for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
						p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								TextComponent.fromLegacyText("A supply drop chest will be spawned in " + Math.abs(passedSeconds) + " seconds."));
					}
				}
				
				if (passedSeconds == 0) {
					spawnSupplyDrop();
					server.getScheduler().cancelTask(supplyDropTaskId);
				}
			}
			
		}, 20, 20); // 1 second = 20 ticks	
	}
	
	private static void spawnSupplyDrop() {
		ServerManager.sendSound(Sound.BLOCK_BELL_USE);
		
		// Get the spawn location
        Location spawnLocation = Bukkit.getWorld("world").getSpawnLocation();

        // Generate random offsets for X and Z coordinates
        Random random = new Random();
        int offsetX = random.nextInt(21) - 10; // Random value between -10 and 10
        int offsetZ = random.nextInt(21) - 10; // Random value between -10 and 10

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
        
        // Add items to the chest's inventory
        chestInventory.addItem(new ItemStack(Material.DIAMOND, 5));
        chestInventory.addItem(new ItemStack(Material.GOLD_INGOT, 10));
        chestInventory.addItem(new ItemStack(Material.IRON_INGOT, 20));
        
        Bukkit.broadcastMessage("Supply chest dropped at (x = " + chestLocation.getX() + ", y = " + chestLocation.getY() + ", z = " + chestLocation.getZ() + ")");
	}
	
}
