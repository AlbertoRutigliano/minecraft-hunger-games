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
	
	public static int worldBorderSize = 128;
	public static int worldBorderMinumumSize = 20;
	
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
    	
    	phase = HGPhase.WAITING_FOR_HG;
    	
    	getServer().getWorld("world").setDifficulty(Difficulty.PEACEFUL); //TODO For test purpose
    	
    	// Create world border
		SpigotPlugin.server.getWorld("world").getWorldBorder().setCenter(SpigotPlugin.server.getWorld("world").getSpawnLocation());
		SpigotPlugin.server.getWorld("world").getWorldBorder().setSize(worldBorderSize);
    	
        // Commands enabled with following method must have entries in plugin.yml
        getCommand("lobby").setExecutor(new TestCommand(this));
        getCommand("nolobby").setExecutor(new TestCommand(this));
        getCommand("start-hg").setExecutor(new TestCommand(this));
        
        getCommand("bowman").setExecutor(new ClassCommand());
        getCommand("armored").setExecutor(new ClassCommand());
        getCommand("doglover").setExecutor(new ClassCommand());
        
        getServer().getPluginManager().registerEvents(new PlayerManager(), this);
        
        //ServerSchedulers.initGameStartCounter();
        
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
    
    public static boolean isWaitingForStart() {
    	return phase.equals(HGPhase.WAITING_FOR_HG);
    }
    
    public static boolean isPlaying() {
    	return phase.equals(HGPhase.PLAYING);
    }
}
