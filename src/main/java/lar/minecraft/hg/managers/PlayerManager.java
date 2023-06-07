package lar.minecraft.hg.managers;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import lar.minecraft.hg.SpigotPlugin;

public class PlayerManager implements Listener {

	/**
	 * Player death event
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		ServerManager.sendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		
		Player deathPlayer = event.getEntity().getPlayer();
		deathPlayer.kickPlayer("Si muert");
		
		/* TODO: Spectator mode (work in progress)
		deathPlayer.setGameMode(GameMode.SPECTATOR);
		if (event.getEntity().getKiller() != null) {
			Player killer = event.getEntity().getKiller();
			deathPlayer.sendMessage("Now you are following " + killer.getName());
			deathPlayer.setSpectatorTarget(killer);
		}*/
		
		// Fireworks effect
		/*if (ServerManager.getLivingPlayers().size() == 1) {
			ServerSchedulers.lastPlayerVictory();
		}*/
	}
	
	/**
	 * Player quit event
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		ServerManager.sendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);

		// Fireworks effect
		/*if (ServerManager.getLivingPlayers().size() == 1) {
			ServerSchedulers.lastPlayerVictory();
		}*/
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		/*if (SpigotPlugin.isLobby() || SpigotPlugin.isSafeArea() || SpigotPlugin.isWinning() || SpigotPlugin.isPluginLoading()) {
			event.setCancelled(true);
		}*/
	}
	
	@Deprecated
	@EventHandler
	public void onPlayerShootBow(EntityShootBowEvent event) {
		if (SpigotPlugin.isLobby() || SpigotPlugin.isPluginLoading()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (SpigotPlugin.isPlaying() || SpigotPlugin.isWinning() || SpigotPlugin.isSafeArea()) {
			event.setJoinMessage(null);
			event.getPlayer().setGameMode(GameMode.SPECTATOR);
		}
	}
	
	/*TODO: For spectator mode (work in progress)
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		if (player.getGameMode().equals(GameMode.SPECTATOR)) {
			Player targetPlayer = ServerManager.getLivingPlayers().get(0);
			player.sendMessage("Now you are following " + targetPlayer.getName());
			player.setSpectatorTarget(targetPlayer);
		}
	}
	*/
	
	
}
