package lar.minecraft.hg;

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
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.FireworkMeta;

import lar.minecraft.hg.enums.ConfigProperty;
import lar.minecraft.hg.enums.HGPhase;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.managers.DatabaseManager;
import lar.minecraft.hg.managers.PlayerClassManager;
import lar.minecraft.hg.managers.PlayerManager;
import lar.minecraft.hg.managers.ServerManager;
import lar.minecraft.hg.utils.ConfigUtils;
import lar.minecraft.hg.utils.MessageUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ServerSchedulers {

	private static World world;
	private static Server server;
	private static SpigotPlugin plugin;
	private static int worldBorderSize = ConfigUtils.getInt(ConfigProperty.world_border_max_size);
	private static int minPlayers = ConfigUtils.getInt(ConfigProperty.min_players);
	private static int currentHGGameId = 0;
	
	public static void init(SpigotPlugin plugin) {
		ServerSchedulers.plugin = plugin;
		ServerSchedulers.server = plugin.getServer();
		ServerSchedulers.world = SpigotPlugin.world;
	}
	
	public static int getWorldBorderSize() {
		return worldBorderSize;
	}
	
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
	
	public static void waitingPhase() {
		SpigotPlugin.setPhase(HGPhase.WAITING);
		plugin.getLogger().info(SpigotPlugin.getPhase() + " phase");
		ServerManager.getLivingPlayers().forEach(p -> {
			p.setGameMode(GameMode.ADVENTURE);
			p.getInventory().clear();
		});
		waitingPhaseTaskId = server.getScheduler().scheduleSyncRepeatingTask(plugin,  new Runnable() {
			@Override
			public void run() {
				if (SpigotPlugin.server.getOnlinePlayers().size() < minPlayers) {
					for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
						p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								new TextComponent(MessageUtils.getMessage(MessageKey.waiting_phase_min_players, SpigotPlugin.server.getOnlinePlayers().size(), minPlayers)));					
					}
				} else {
					lobbyPhase();
					server.getScheduler().cancelTask(waitingPhaseTaskId);
				}
				
			}
			
		}, 20, 20); // 1 second = 20 ticks
	}
	
	public static void lobbyPhase() {
		SpigotPlugin.setPhase(HGPhase.LOBBY);
		plugin.getLogger().info(SpigotPlugin.getPhase() + " phase");
		ServerManager.getLivingPlayers().forEach(p -> {
			p.setGameMode(GameMode.ADVENTURE);
			p.getInventory().clear();
		});
		gameStartTime = 0;
		lobbyPhaseTaskId = server.getScheduler().scheduleSyncRepeatingTask(plugin,  new Runnable() {
			public void run() {
				long execTime = world.getTime();
				
				if (SpigotPlugin.server.getOnlinePlayers().size() < minPlayers) {
					waitingPhase();
					server.getScheduler().cancelTask(lobbyPhaseTaskId);
				}
				
				// First runnable run
				if (gameStartTime == 0) {
					gameStartTime = execTime + (20 * ConfigUtils.getInt(ConfigProperty.duration_lobby)); // Game will start in X seconds
				}
				long passedSeconds = (execTime - gameStartTime) / 20;
				for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
					p.spigot().sendMessage(
							ChatMessageType.ACTION_BAR, 
							new TextComponent(MessageUtils.getMessage(MessageKey.lobby_phase_expires_alert, Math.abs(passedSeconds))));
				}
				if (passedSeconds == 0) {
					safeAreaPhase();
					server.getScheduler().cancelTask(lobbyPhaseTaskId);
				}
			}
		}, 20, 20); // 1 second = 20 ticks
	}
	
	public static void safeAreaPhase() {
		SpigotPlugin.setPhase(HGPhase.SAFE_AREA);
		plugin.getLogger().info(SpigotPlugin.getPhase() + " phase");
		
		// Register new HungerGames game on Database
		currentHGGameId = DatabaseManager.createHGGame(SpigotPlugin.serverId);
		
		safeAreaTime = 0;
		// Notify all players that Hunger Games is starting
		ServerManager.getLivingPlayers().forEach(p -> {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageUtils.getMessage(MessageKey.safe_area_phase_alert)));
			p.setGameMode(GameMode.SURVIVAL);
			
			// Teleport each player to a random location 
			Location spawnLocation = ServerManager.getSurfaceRandomLocation(30
											, SpigotPlugin.newSpawnLocation
											, 0
											, 2
											, 0);
			
			p.teleport(spawnLocation);
			
			// Write player join on Database
			DatabaseManager.addPlayerJoin(SpigotPlugin.serverId, currentHGGameId, p);
		});
		PlayerClassManager.giveClasses();
		ServerManager.sendSound(Sound.EVENT_RAID_HORN);
		
		safeAreaPhaseTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(plugin,  new Runnable() {
			public void run() {
				long execTime = world.getTime();
				// First runnable run
				if (safeAreaTime == 0) {
					safeAreaTime = execTime + (20 * ConfigUtils.getInt(ConfigProperty.duration_safe_area));
				}
				long passedSeconds = (execTime - safeAreaTime) / 20;

				ServerManager.getLivingPlayers().forEach(
						p -> p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								new TextComponent(MessageUtils.getMessage(MessageKey.safe_area_expires_alert, Math.abs(passedSeconds))))
						);
				if (passedSeconds == 0) {
					playingPhase();
					server.getScheduler().cancelTask(safeAreaPhaseTaskId);
				}
				
			}
		}, 20, 20); // 1 second = 20 ticks
		
	}
	
	public static void playingPhase() {
		SpigotPlugin.setPhase(HGPhase.PLAYING);
		plugin.getLogger().info(SpigotPlugin.getPhase() + " phase");
		
		worldBorderCollapseTime = 0;
		winnerCelebrationsTime = 0;
		server.setIdleTimeout(ConfigUtils.getInt(ConfigProperty.duration_idle_timeout));
		ServerManager.getLivingPlayers()
			.forEach(p -> p.sendTitle(MessageUtils.getMessage(MessageKey.playing_phase_alert), null, 10, 100, 10));
		
		DatabaseManager.saveStartingDateTime(SpigotPlugin.serverId, currentHGGameId);
		playingPhaseTaskId = server.getScheduler().scheduleSyncRepeatingTask(plugin,  new Runnable() {
			public void run() {
				long execTime = world.getTime();
				
				// WORLD BORDER COLLAPSE
				if (ConfigUtils.getBoolean(ConfigProperty.world_border_collapse) && SpigotPlugin.isPlaying()) {
					// First runnable run
					if(worldBorderCollapseTime == 0) {
						worldBorderCollapseTime = execTime + (20 * ConfigUtils.getInt(ConfigProperty.world_border_collapse_counter_seconds));
					}
					long passedSecondsForWorldBorderCollapse = (execTime - worldBorderCollapseTime) / 20;
					if(passedSecondsForWorldBorderCollapse == 0) {
						if (worldBorderSize > ConfigUtils.getInt(ConfigProperty.world_border_min_size)) {
							Bukkit.broadcastMessage(MessageUtils.getMessage(MessageKey.world_border_collapse_message));
							worldBorderSize = worldBorderSize - ConfigUtils.getInt(ConfigProperty.world_border_collapse_radius);
							world.getWorldBorder().setSize(worldBorderSize, ConfigUtils.getInt(ConfigProperty.world_border_collapse_counter_seconds));
							world.getWorldBorder().setDamageBuffer(0);
							world.getWorldBorder().setDamageAmount(0.2);
							worldBorderCollapseTime = 0; 
						}
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
						Player winner = ServerManager.getLivingPlayers().iterator().next();
						SpigotPlugin.server.broadcastMessage(MessageUtils.getMessage(MessageKey.wins_the_hunger_games, winner.getName()));
						winner.sendTitle("You win the Hunger Games!", null, 10, 70, 20);
						winnerCelebrationsTime = execTime + (20 * ConfigUtils.getInt(ConfigProperty.duration_winner_celebrations));

						// Save the winning player on Database
						DatabaseManager.savePlayerWin(SpigotPlugin.serverId, currentHGGameId, winner);
						fireworkEffect(winner);
					}
					
					long passedSeconds = (execTime - winnerCelebrationsTime) / 20;
					
					for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
						p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								new TextComponent(MessageUtils.getMessage(MessageKey.server_to_restart_alert, Math.abs(passedSeconds))));
					}
					if (passedSeconds == 0) {
						SpigotPlugin.setPhase(HGPhase.WAITING);
						if (ConfigUtils.getBoolean(ConfigProperty.server_auto_restart)) {
							ServerManager.restartServer();
						}
						SpigotPlugin.server.getScheduler().cancelTask(playingPhaseTaskId);
					}
				}
				
				// If there are no more players restart the game
				// Used to prevent simultaneous disconnection or death of the remaining players
				if (ServerManager.getLivingPlayers().size() == 0) {
					SpigotPlugin.setPhase(HGPhase.WAITING);
					if (ConfigUtils.getBoolean(ConfigProperty.server_auto_restart)) {
						ServerManager.restartServer();
					}
					SpigotPlugin.server.getScheduler().cancelTask(playingPhaseTaskId);
				}
        
			}
			
			
			
		}, 20, 20); // 1 second = 20 ticks
		
		if (SpigotPlugin.isPlaying()) {
			supplyDrop(ConfigUtils.getInt(ConfigProperty.chest_spawn_num));
		}
		
	}
	
	private static void fireworkEffect(Player winner) {
		SpigotPlugin.setPhase(HGPhase.WINNING);
		plugin.getLogger().info(SpigotPlugin.getPhase() + " phase");
		
		fireworksEffectsTime = 0;
		fireworksEffectsTaskId = server.getScheduler().scheduleSyncRepeatingTask(plugin,  new Runnable() {
			@Override
			public void run() {
				long execTime = world.getTime();
				if (fireworksEffectsTime == 0) {
					fireworksEffectsTime = execTime + (20 *ConfigUtils.getInt(ConfigProperty.duration_fireworks));
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
	
	private static void supplyDrop(int index) {
		if (index > 0) {
			supplyDropTime = 0;
			supplyDropTaskId = server.getScheduler().scheduleSyncRepeatingTask(plugin,  new Runnable() {
				@Override
				public void run() {
					if (SpigotPlugin.isPlaying()) {
						long execTime = world.getTime();
						
						// Spawn a supply drop chest after durations.supply-drop seconds
						if (supplyDropTime == 0) {
							// If is the first supply drop, get the first-supply-drop duration property 
							if (index == ConfigUtils.getInt(ConfigProperty.chest_spawn_num)) {
								supplyDropTime = execTime + (20 * ConfigUtils.getInt(ConfigProperty.duration_first_supply_drop));
							} else {
								supplyDropTime = execTime + (20 * ConfigUtils.getInt(ConfigProperty.duration_supply_drop));
							}
						}
						
						long passedSeconds = (execTime - supplyDropTime) / 20;
					
						if (Math.abs(passedSeconds) <= 10) {
							for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
								p.spigot().sendMessage(
										ChatMessageType.ACTION_BAR, 
										new TextComponent(MessageUtils.getMessage(MessageKey.supply_drop_alert, Math.abs(passedSeconds))));
							}
						}
						
						if (passedSeconds == 0) {
							ServerManager.spawnSupplyDrop();
							server.getScheduler().cancelTask(supplyDropTaskId);
							supplyDrop(index-1);
						}
					} else {
						server.getScheduler().cancelTask(supplyDropTaskId);
					}
					
				}
				
			}, 20, 20); // 1 second = 20 ticks
		}
			
	}
	
}
