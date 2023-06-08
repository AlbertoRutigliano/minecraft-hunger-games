package lar.minecraft.hg;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.FireworkMeta;

import lar.minecraft.hg.managers.PlayerManager;
import lar.minecraft.hg.managers.QueryManager;
import lar.minecraft.hg.managers.DatabaseManager;
import lar.minecraft.hg.managers.ServerManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ServerSchedulers {

	private static int currentHGGameId = 0;
	
	private static long gameStartTime = 0;
	private static long safeAreaTime = 0;
	private static long winnerCelebrationsTime = 0;
	private static long fireworksEffectsTime = 0;
	
	private static int gameStartTaskId = -1;
	private static int safeAreaTaskId = -1;
	private static int winnerCelebrationsTaskId = -1;
	private static int fireworksEffectsTaskId = -1;
	
	private final static int GAME_START_COUNTER_SECONDS = 20;
	private final static int SAFE_AREA_COUNTER_SECONDS = 20;
	private final static int WINNER_CELEBRATIONS_COUNTER_SECONDS = 20;
	private final static int FIREWORKS_EFFECTS_COUNTER_SECONDS = 20;
	
	public static void initGameStartCounter() {
		SpigotPlugin.setPhase(HGPhase.LOBBY);
		gameStartTime = 0;
		gameStartTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				long execTime = SpigotPlugin.server.getWorld("world").getTime();
				// First runnable run
				if (gameStartTime == 0) {
					gameStartTime = execTime + (20 * GAME_START_COUNTER_SECONDS); // Game will start in 10 seconds
					ServerManager.getLivingPlayers().forEach(p -> {
						p.setGameMode(GameMode.ADVENTURE);
						p.getInventory().clear();
						// p.getInventory().addItem(new ItemStack(Material.COMPASS));
					});
				}
				long passedSeconds = (execTime - gameStartTime) / 20;
				for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
					p.spigot().sendMessage(
							ChatMessageType.ACTION_BAR, 
							TextComponent.fromLegacyText("Game will start in " + Math.abs(passedSeconds) + " seconds"));
				}
				if (passedSeconds == 0) {
					initSafeArea();
					SpigotPlugin.server.getScheduler().cancelTask(gameStartTaskId);
				}
			}
		}, 20, 20); // 1 second = 20 ticks
	}
	
	public static void initSafeArea() {
		SpigotPlugin.setPhase(HGPhase.SAFE_AREA);
		safeAreaTime = 0;
		currentHGGameId = DatabaseManager.createHGGame(1);
		ServerManager.getLivingPlayers().forEach(p -> {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("It's starting the Hunger Games!"));
			p.setGameMode(GameMode.SURVIVAL);
			p.teleport(SpigotPlugin.server.getWorld("world").getSpawnLocation());
			//TODO: addPlayerJoin fix first parameter, should be the serverId coming from parameter file
			DatabaseManager.addPlayerJoin(1, currentHGGameId, p);
		});
		ServerManager.giveClasses();
		ServerManager.sendSound(Sound.EVENT_RAID_HORN);
		
		safeAreaTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				long execTime = SpigotPlugin.server.getWorld("world").getTime();
				// First runnable run
				if (safeAreaTime == 0) {
					safeAreaTime = execTime + (20 * SAFE_AREA_COUNTER_SECONDS);
				}
				long passedSeconds = (execTime - safeAreaTime) / 20;

				ServerManager.getLivingPlayers().forEach(p -> p.spigot().sendMessage(
						ChatMessageType.ACTION_BAR, 
						TextComponent.fromLegacyText("Safe area expire in " + Math.abs(passedSeconds) + " seconds")));
				if (passedSeconds == 0) {
					ServerManager.getLivingPlayers().forEach(p -> p.spigot().sendMessage(
							ChatMessageType.ACTION_BAR, 
							TextComponent.fromLegacyText("It's Hunger Games tiiiiiiiiime!")));
					SpigotPlugin.setPhase(HGPhase.PLAYING);
					initCompassTracker();
					lastPlayerVictory();
					SpigotPlugin.server.getScheduler().cancelTask(safeAreaTaskId);
				}
				
			}
		}, 20, 20); // 1 second = 20 ticks
	}
	
	public static void lastPlayerVictory() {
		winnerCelebrationsTime = 0;
		winnerCelebrationsTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				if (ServerManager.getLivingPlayers().size() == 1) {
					// First runnable run
					long execTime = SpigotPlugin.server.getWorld("world").getTime();
					if(winnerCelebrationsTime == 0) {
						SpigotPlugin.setPhase(HGPhase.WINNING);
						Player winner = ServerManager.getLivingPlayers().iterator().next();
						SpigotPlugin.server.broadcastMessage(winner.getName() + " wins the Hunger Games!");
						winner.sendTitle("You win the Hunger Games!", null, 10, 70, 20);
						winnerCelebrationsTime = execTime + (20 * WINNER_CELEBRATIONS_COUNTER_SECONDS);
						//TODO: savePlayerWin fix first parameter, should be the serverId coming from parameter file
						DatabaseManager.savePlayerWin(1, currentHGGameId, winner);
						fireworkEffect(winner);
					}
					
					long passedSeconds = (execTime - winnerCelebrationsTime) / 20;
					
					for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
						p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								TextComponent.fromLegacyText("A new Hunger Games will start in " + Math.abs(passedSeconds) + " seconds. Server will be restarted"));
					}
					if (passedSeconds == 0) {
						SpigotPlugin.server.broadcastMessage("Starting a new Hunger Games Server");
						// initGameStartCounter(); TODO Paused for testing
						SpigotPlugin.setPhase(HGPhase.WAITING_FOR_HG);
						SpigotPlugin.server.shutdown();
						SpigotPlugin.server.getScheduler().cancelTask(winnerCelebrationsTaskId);
					}
				}	
			}
		}, 20, 20); // 1 second = 20 ticks
	}
	
	public static void initCompassTracker() {
		SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				
				SpigotPlugin.server.getLogger().info("Phase: " + SpigotPlugin.getPhase().toString());
				
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
			}
		}, 20, 20); // 1 second = 20 ticks
	}
	
	private static void fireworkEffect(Player winner) {
		fireworksEffectsTime = 0;
		fireworksEffectsTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			@Override
			public void run() {
				long execTime = SpigotPlugin.server.getWorld("world").getTime();
				if (fireworksEffectsTime == 0) {
					fireworksEffectsTime = execTime + (20 * FIREWORKS_EFFECTS_COUNTER_SECONDS);
				}
				long passedSeconds = (execTime - fireworksEffectsTime) / 20;
				
				if (passedSeconds % 4 == 0) {
					Firework firework = winner.getWorld().spawn(winner.getLocation(), Firework.class);
					FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
					data.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().build());
					data.setPower(1);
					firework.setFireworkMeta(data);
				}
				
				if (passedSeconds == 0) {
					SpigotPlugin.server.getScheduler().cancelTask(fireworksEffectsTaskId);
				}
			}
			
		}, 20, 20); // 1 second = 20 ticks	
	}
}
