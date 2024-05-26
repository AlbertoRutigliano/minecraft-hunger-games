package lar.minecraft.hg;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Difficulty;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import lar.minecraft.hg.commands.ClassCommand;
import lar.minecraft.hg.commands.TestCommand;
import lar.minecraft.hg.managers.DatabaseManager;
import lar.minecraft.hg.managers.PlayerManager;

public class SpigotPlugin extends JavaPlugin {
	
	public static Server server;
	
	public static HGPhase phase;
	
	public static int serverId;
	
	public static Map<Player, PlayerExt> playerExtension = new HashMap<>();
	
	@Override
    public void onLoad() {
		server = getServer();
		saveDefaultConfig();
    }
	
    @Override
    public void onDisable() {
        // Don't log disabling, Spigot does that for you automatically!
    	try {
			DatabaseManager.disconnectToDatabase();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void onEnable() {
        // Don't log enabling, Spigot does that for you automatically!
    	
    	serverId = getConfig().getInt("server.id");
    	
    	server.getWorld("world").setDifficulty(Difficulty.NORMAL);
    	
    	// Create world border
    	server.getWorld("world").getWorldBorder().setCenter(server.getWorld("world").getSpawnLocation());
    	server.getWorld("world").getWorldBorder().setSize(getConfig().getInt("world-border.max-size", 256));
    	
    	// Instantiate database connection and connect
    	new DatabaseManager(this, true);
    	
        // Commands enabled with following method must have entries in plugin.yml
        getCommand("start-hg").setExecutor(new TestCommand(this));
        getCommand("current-phase").setExecutor(new TestCommand(this));
        
        getCommand("test").setExecutor(new TestCommand(this));
        
        getCommand("restart-hg-server").setExecutor(new TestCommand(this));
        
        ClassCommand.AVAILABLE_CLASSES.forEach( c -> {
        	getCommand(c).setExecutor(new ClassCommand());
        });
        
        getServer().getPluginManager().registerEvents(new PlayerManager(), this);
        
        if (getConfig().getBoolean("server.auto-start", true)) {
        	new ServerSchedulers(this).waitingPhase();
        }
        
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
