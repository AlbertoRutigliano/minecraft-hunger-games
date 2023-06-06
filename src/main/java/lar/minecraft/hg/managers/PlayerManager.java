package lar.minecraft.hg.managers;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import lar.minecraft.hg.ServerSchedulers;

public class PlayerManager implements Listener {

	/**
	 * Player death event
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		ServerManager.SendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		
		if (ServerManager.getLivingPlayers().size() == 1) {
			ServerSchedulers.lastPlayerVictory();
			for (Player p : ServerManager.getLivingPlayers()) {
				Firework firework = p.getWorld().spawn(p.getLocation(), Firework.class);
		        FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
		        data.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().build());
		        data.setPower(1);
		        firework.setFireworkMeta(data);
			}
		}
	}
		
}
