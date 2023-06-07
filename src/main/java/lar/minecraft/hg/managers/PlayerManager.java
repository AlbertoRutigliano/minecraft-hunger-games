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
		deathPlayer.setGameMode(GameMode.SPECTATOR);
		// deathPlayer.kickPlayer("Si muert");
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
		if (SpigotPlugin.isLobby() || SpigotPlugin.isSafeArea() || SpigotPlugin.isWinning() || SpigotPlugin.isWaitingForStart()) {
			event.setCancelled(true);
		}
	}
	
	@Deprecated
	@EventHandler
	public void onPlayerShootBow(EntityShootBowEvent event) {
		if (SpigotPlugin.isLobby() || SpigotPlugin.isWaitingForStart()) {
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
	
}
