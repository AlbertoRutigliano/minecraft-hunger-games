package lar.minecraft.hg.managers;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class PlayerManager implements Listener {

	/**
	 * Player death event
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		ServerManager.SendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		
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
		if (ServerManager.getLivingPlayers().size() == 1) {
			// ServerSchedulers.lastPlayerVictory();
			for (Player p : ServerManager.getLivingPlayers()) {
				Firework firework = p.getWorld().spawn(p.getLocation(), Firework.class);
		        FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
		        data.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().build());
		        data.setPower(1);
		        firework.setFireworkMeta(data);
			}
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
