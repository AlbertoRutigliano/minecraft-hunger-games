package lar.minecraft.hg;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Difficulty;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import lar.minecraft.hg.managers.PlayerManager;

public class SpigotPlugin extends JavaPlugin {
	
	public static Server server;
	
	public static HGPhase phase;
	
	public static Map<Player, PlayerExt> playerExtension = new HashMap<>();
	
	@Override
    public void onLoad() {
		server = getServer();
    }
	
    @Override
    public void onDisable() {
        // Don't log disabling, Spigot does that for you automatically!
    }

    @Override
    public void onEnable() {
        // Don't log enabling, Spigot does that for you automatically!
    	
    	phase = HGPhase.PLUGIN_LOADING;
    	
    	getServer().getWorld("world").setDifficulty(Difficulty.PEACEFUL); //TODO For test purpose
    	
        // Commands enabled with following method must have entries in plugin.yml
        getCommand("lobby").setExecutor(new TestCommand(this));
        getCommand("nolobby").setExecutor(new TestCommand(this));
        getCommand("start-hg").setExecutor(new TestCommand(this));
        
        getCommand("bowman").setExecutor(new ClassCommand());
        getCommand("armored").setExecutor(new ClassCommand());
        
        getServer().getPluginManager().registerEvents(new PlayerManager(), this);
        
        ServerSchedulers.initPhaseLogger();
       
    }
    
    public static HGPhase getPhase() {
    	return phase;
    }
    
    public static void setPhase(HGPhase newPhase) {
    	phase = newPhase;
    }
    
    public static boolean isLobby() {
    	return phase.equals(HGPhase.LOBBY);
    }
    
    public static boolean isSafeArea() {
    	return phase.equals(HGPhase.SAFE_AREA);
    }
    
    public static boolean isWinning() {
    	return phase.equals(HGPhase.WINNING);
    }
    
    public static boolean isPluginLoading() {
    	return phase.equals(HGPhase.PLUGIN_LOADING);
    }
    
    public static boolean isPlaying() {
    	return phase.equals(HGPhase.PLAYING);
    }
}
