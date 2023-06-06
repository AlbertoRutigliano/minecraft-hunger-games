package lar.minecraft.hg.managers;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import lar.minecraft.hg.SpigotPlugin;

public class ServerManager {

	/**
	 * Send sound to all players
	 * 
	 * @param  sound  the sound to reproduce
	 */
	public static void SendSound(Sound sound) {
		for(Player p : SpigotPlugin.server.getOnlinePlayers()) {
			p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
		}
	}
}
