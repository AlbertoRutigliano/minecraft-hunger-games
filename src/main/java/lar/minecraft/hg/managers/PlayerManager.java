package lar.minecraft.hg.managers;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerManager implements Listener {

	/**
	 * Player death event
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		ServerManager.SendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
	}
}
