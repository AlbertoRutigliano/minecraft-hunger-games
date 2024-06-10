package lar.minecraft.hg.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.PlayerExtra;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.utils.MessageUtils;

public class PlayerManager implements Listener {

	public static Map<UUID, PlayerExtra> playerExtras = new HashMap<>();
	
	private int winnerParticleEffectTaskId = 0;
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		event.setJoinMessage(null);
		if (SpigotPlugin.isWaitingForStart() || SpigotPlugin.isLobby()) {
			// Teleport each player to a random location 
			Location spawnLocation = ServerManager.getSurfaceRandomLocation(30, SpigotPlugin.newSpawnLocation, 0, 2, 0);
			player.teleport(spawnLocation);

			// Set player gamemode, send welcome message and give instructions book
			player.setGameMode(GameMode.ADVENTURE);
			player.playSound(player, Sound.BLOCK_END_PORTAL_FRAME_FILL, 10.0f, 1.0f);
			player.sendMessage(MessageUtils.getMessage(MessageKey.welcome_message, player.getDisplayName()));
			player.getInventory().addItem(ServerManager.getGameInstructionsBook());
			
			// Check if the player is the winner of the last match or he is premium and create PlayerExtra to track it
			String lastWinner = DatabaseManager.getLastWinner(SpigotPlugin.serverId);
			boolean isLastWinner = false;
			
			if (!lastWinner.isEmpty()) {
				isLastWinner = player.getUniqueId().compareTo(UUID.fromString(lastWinner)) == 0 ? true : false;
				if (isLastWinner) {
					player.sendMessage(MessageUtils.getMessage(MessageKey.last_match_win));
					
					// Run a task to spawn particles effects to signal that he is the winner!
					winnerParticleEffectTaskId = SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class),  new Runnable() {
						@Override
						public void run() {
							if (PlayerManager.playerExtras.get(player.getUniqueId()).isLastWinner()) {
								SpigotPlugin.server.getWorld(player.getWorld().getName()).spawnParticle(Particle.DRAGON_BREATH, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 40, -0.5, 0.5, -0.5, 0.01);
							}
						}
					}, 20, 10); // 1 second = 20 ticks
				}
			}
			boolean isPremium = DatabaseManager.isPlayerPremium(player.getUniqueId().toString());
			int winCount = DatabaseManager.getPlayerWinCount(player.getUniqueId().toString());
			PlayerExtra playerExtra = new PlayerExtra(player.getUniqueId(), player.getName(), isLastWinner, isPremium, winCount);
			PlayerManager.playerExtras.put(player.getUniqueId(), playerExtra);
			
			// Used to track player position witouth pressing F3
			createPlayerLocationBossBar(player);
		}
		if (SpigotPlugin.isPlaying() || SpigotPlugin.isWinning() || SpigotPlugin.isSafeArea()) {
			player.setGameMode(GameMode.SPECTATOR);
		}
	}
	
	/**
	 * Player damage event
	 */
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (SpigotPlugin.isLobby() || SpigotPlugin.isSafeArea() || SpigotPlugin.isWinning() || SpigotPlugin.isWaitingForStart()) {
			event.setCancelled(true);
		}
	}
	
	/**
	 * Player death event
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		Player deathPlayer = event.getEntity().getPlayer();
		deathPlayer.getWorld().strikeLightningEffect(deathPlayer.getLocation());
		deathPlayer.setGameMode(GameMode.SPECTATOR);

		// Stop reproducing particles of the winner player
		if (PlayerManager.playerExtras.get(deathPlayer.getUniqueId()).isLastWinner()) {
			SpigotPlugin.server.getScheduler().cancelTask(winnerParticleEffectTaskId);
		}
		
		ServerManager.sendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		retrieveKilledPlayerHead(event);
	}
	
	/**
	 * Player quit event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		event.setQuitMessage(null);
		if (SpigotPlugin.isWinning() || SpigotPlugin.isLobby()) { 
			PlayerManager.playerExtras.remove(event.getPlayer().getUniqueId());
		}
		if (SpigotPlugin.isSafeArea() || SpigotPlugin.isWinning() || SpigotPlugin.isPlaying()) {
			Player player = event.getPlayer();
			player.getWorld().strikeLightningEffect(player.getLocation());
			ServerManager.sendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		}
		// Stop reproducing particles of the winner player
		PlayerExtra playerExtra = PlayerManager.playerExtras.getOrDefault(event.getPlayer().getUniqueId(), null);
		if (playerExtra != null && playerExtra.isLastWinner()) {
			SpigotPlugin.server.getScheduler().cancelTask(winnerParticleEffectTaskId);
		}
	}
	
	/**
	 * Player drop item event
	 * @param event
	 */
	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		// Check if player thrown the Instruction Book and block the event
		if (SpigotPlugin.isWaitingForStart() || SpigotPlugin.isLobby()) { 
			if(event.getItemDrop().getItemStack().getType() == Material.WRITTEN_BOOK) {
				event.setCancelled(true);
			}
		}
	}
	
	public static Player getNearestPlayer(Player player, double range) {
        double distance = Double.POSITIVE_INFINITY;
        Player target = null;
       
        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player)) continue;
            if (entity == player) continue;
            
            double distanceTo = player.getLocation().distance(entity.getLocation());
            if (distanceTo < distance) {
                distance = distanceTo;
                target = (Player) entity;
            }
        }
        return target;
    }
	
	/*
	 * If a Player kill another Player he receive the killedPlayer head as prize
	 */
	private void retrieveKilledPlayerHead(PlayerDeathEvent event) {
		Player killedPlayer = event.getEntity().getPlayer();
		// Check if the killer is a player
        if (event.getEntity().getKiller() != null) {
            // Get the player who was killed and the killer
            Player killer = killedPlayer.getKiller();
            
            // Create the player head item
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(killedPlayer);
                playerHead.setItemMeta(skullMeta);
            }
            // Give the killer the head of the killed player
            killer.getInventory().addItem(playerHead);
        }
	}
	
    
    private void createPlayerLocationBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar(player.getDisplayName() + " location", BarColor.WHITE, BarStyle.SOLID);
        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);
        SpigotPlugin.server.getScheduler().scheduleSyncRepeatingTask(SpigotPlugin.getPlugin(SpigotPlugin.class), new Runnable() {
			@Override
			public void run() {
				if (bossBar != null) {
		            Location loc = player.getLocation();
		            bossBar.setTitle(MessageUtils.getMessage(MessageKey.current_player_location, 
		            		String.format("%.2f", loc.getX()), 
		            		String.format("%.2f", loc.getY()), 
		            		String.format("%.2f", loc.getZ())));
		        }
			}
		}, 0, 5); // 1 second = 20 ticks
    }
}
