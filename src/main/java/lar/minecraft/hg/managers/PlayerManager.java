package lar.minecraft.hg.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entities.PlayerExtra;

public class PlayerManager implements Listener {

	public static Map<UUID, PlayerExtra> playerExtras = new HashMap<>();
	
	private int winnerParticleEffectTaskId = 0;
	
	/**
	 * Player death event
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		ServerManager.sendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		Player killedPlayer = event.getEntity().getPlayer();
		killedPlayer.setGameMode(GameMode.SPECTATOR);
		
		// Stop reproducing particles of the winner player
		if (PlayerManager.playerExtras.get(killedPlayer.getUniqueId()).isLastWinner()) {
			SpigotPlugin.server.getScheduler().cancelTask(winnerParticleEffectTaskId);
		}
				
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
	
	/**
	 * Player quit event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		if (SpigotPlugin.isSafeArea() || SpigotPlugin.isWinning() || SpigotPlugin.isPlaying()) {
			ServerManager.sendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		}
		// Stop reproducing particles of the winner player
		PlayerExtra playerExtra = PlayerManager.playerExtras.getOrDefault(event.getPlayer().getUniqueId(), null);
		if (playerExtra != null && playerExtra.isLastWinner()) {
			SpigotPlugin.server.getScheduler().cancelTask(winnerParticleEffectTaskId);
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
		
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (SpigotPlugin.isWaitingForStart() || SpigotPlugin.isLobby()) {
			Player player = event.getPlayer();
			
			player.setGameMode(GameMode.ADVENTURE);
			player.playSound(player, Sound.BLOCK_END_PORTAL_FRAME_FILL, 10.0f, 1.0f);
			
			// Check if the player is the winner of the last match or he is premium and create PlayerExtra to track it
			String lastWinner = DatabaseManager.getLastWinner(SpigotPlugin.serverId);
			boolean isLastWinner = false;
			
			if (!lastWinner.isEmpty()) {
				isLastWinner = player.getUniqueId().compareTo(UUID.fromString(lastWinner)) == 0 ? true : false;
				// TODO: migliorare il messaggio e UX
				if (isLastWinner) {
					player.sendMessage("Hai vinto l'ultimo round! Potrai scegliere una classe avanzata.");
					
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
			PlayerExtra playerExtra = new PlayerExtra(player.getUniqueId(), isLastWinner, isPremium);
			PlayerManager.playerExtras.put(player.getUniqueId(), playerExtra);
		}
		if (SpigotPlugin.isPlaying() || SpigotPlugin.isWinning() || SpigotPlugin.isSafeArea()) {
			event.setJoinMessage(null);
			event.getPlayer().setGameMode(GameMode.SPECTATOR);
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
	
}
