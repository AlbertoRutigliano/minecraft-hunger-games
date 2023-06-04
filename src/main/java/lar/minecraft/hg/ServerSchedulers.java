package lar.minecraft.hg;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ServerSchedulers {

	private static long gameStartTime = 0;
	private static long safeAreaTime = 0;
	private static long winnerCelebrationsTime = 0;
	
	private static int gameStartTaskId = -1;
	private static int safeAreaTaskId = -1;
	private static int winnerCelebrationsTaskId = -1;
	
	private final static int GAME_START_COUNTER_SECONDS = 10;
	private final static int SAFE_AREA_COUNTER_SECONDS = 20;
	private final static int WINNER_CELEBRATIONS_COUNTER_SECONDS = 30;
	
	public static void initGameStartCounter() {
		gameStartTime = 0;
		gameStartTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				long execTime = SpigotPlugin.server.getWorld("world").getTime();
				// First runnable run
				if (gameStartTime == 0) {
					gameStartTime = execTime + (20 * GAME_START_COUNTER_SECONDS); // Game will start in 10 seconds
					SpigotPlugin.server.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.ADVENTURE));
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
		safeAreaTime = 0;
		SpigotPlugin.server.broadcastMessage("It's starting the Hunger Games!");
		SpigotPlugin.server.getOnlinePlayers().forEach(p -> p.teleport(new Location(SpigotPlugin.server.getWorld("world"), 0, 128, 0)));
		safeAreaTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
			public void run() {
				long execTime = SpigotPlugin.server.getWorld("world").getTime();
				// First runnable run
				if (safeAreaTime == 0) {
					safeAreaTime = execTime + (20 * SAFE_AREA_COUNTER_SECONDS);
				}
				long passedSeconds = (execTime - safeAreaTime) / 20;

				SpigotPlugin.server.getOnlinePlayers().forEach(p -> p.spigot().sendMessage(
						ChatMessageType.ACTION_BAR, 
						TextComponent.fromLegacyText("Safe area expire in " + Math.abs(passedSeconds) + " seconds")));
				if (passedSeconds == 0) {
					SpigotPlugin.server.broadcastMessage("It's Hunger Games tiiiiiiiiime!");
					SpigotPlugin.server.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SURVIVAL));
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
				if (SpigotPlugin.server.getOnlinePlayers().size() == 1) {
					// First runnable run
					long execTime = SpigotPlugin.server.getWorld("world").getTime();
					if(winnerCelebrationsTime == 0) {
						Player winner = SpigotPlugin.server.getOnlinePlayers().iterator().next();
						SpigotPlugin.server.broadcastMessage(winner.getName() + " win the Hunger Games!");
						
						//TODO Add fireworks effects and give prizes
						winnerCelebrationsTime = execTime + (20 * WINNER_CELEBRATIONS_COUNTER_SECONDS);
					}
					long passedSeconds = (execTime - winnerCelebrationsTime) / 20;
					
					for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
						p.spigot().sendMessage(
								ChatMessageType.ACTION_BAR, 
								TextComponent.fromLegacyText("Another Hunger Games will start in " + Math.abs(passedSeconds) + " seconds"));
					}
					if (passedSeconds == 0) {
						SpigotPlugin.server.broadcastMessage("Starting a new Hunger Games");
						initGameStartCounter();
						SpigotPlugin.server.getScheduler().cancelTask(winnerCelebrationsTaskId);
					}
				}	
			}
		}, 20, 20); // 1 second = 20 ticks
	}
}
