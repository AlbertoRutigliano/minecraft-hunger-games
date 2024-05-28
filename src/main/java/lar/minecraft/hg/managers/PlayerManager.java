package lar.minecraft.hg.managers;

import org.bukkit.GameMode;
import org.bukkit.Material;
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

public class PlayerManager implements Listener {

	/**
	 * Player death event
	 */
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		ServerManager.sendSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER);
		Player killedPlayer = event.getEntity().getPlayer();
		killedPlayer.setGameMode(GameMode.SPECTATOR);
		
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
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (SpigotPlugin.isLobby() || SpigotPlugin.isSafeArea() || SpigotPlugin.isWinning() || SpigotPlugin.isWaitingForStart()) {
			event.setCancelled(true);
		}
	}
		
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (SpigotPlugin.isWaitingForStart() || SpigotPlugin.isLobby()) {
			event.getPlayer().setGameMode(GameMode.ADVENTURE);
			event.getPlayer().playSound(event.getPlayer(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 10.0f, 1.0f);
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
