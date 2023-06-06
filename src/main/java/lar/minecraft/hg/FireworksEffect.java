package lar.minecraft.hg;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworksEffect implements Listener {
	
	SpigotPlugin plugin;

    public FireworksEffect(SpigotPlugin plugin) {
        this.plugin = plugin;
    }

	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
		if (plugin.getServer().getOnlinePlayers().size() == 1) {
			ServerSchedulers.lastPlayerVictory();
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				Firework firework = p.getWorld().spawn(p.getLocation(), Firework.class);
		        FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
		        data.addEffects(FireworkEffect.builder().withColor(Color.PURPLE).withColor(Color.GREEN).with(Type.BALL_LARGE).withFlicker().build());
		        data.setPower(1);
		        firework.setFireworkMeta(data);
			}
		}
    }
	
}
