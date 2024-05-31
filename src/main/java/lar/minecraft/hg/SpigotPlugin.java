package lar.minecraft.hg;

import java.sql.SQLException;
import java.util.Arrays;

import org.bukkit.Difficulty;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lar.minecraft.hg.commands.ClassCommand;
import lar.minecraft.hg.commands.TestCommand;
import lar.minecraft.hg.enums.HGPhase;
import lar.minecraft.hg.enums.PlayerClass;
import lar.minecraft.hg.managers.DatabaseManager;
import lar.minecraft.hg.managers.PlayerManager;

public class SpigotPlugin extends JavaPlugin {
	
	public static Server server;
	
	public static FileConfiguration config;
	
	public static HGPhase phase;
	
	public static int serverId;
	
	@Override
    public void onLoad() {
		server = getServer();
		config = getConfig();
		saveDefaultConfig();
    }
	
    @Override
    public void onEnable() {
    	serverId = getConfig().getInt("server.id");
    	server.getWorld("world").setDifficulty(Difficulty.NORMAL);
    	
    	// Create world border
    	server.getWorld("world").getWorldBorder().setCenter(server.getWorld("world").getSpawnLocation());
    	server.getWorld("world").getWorldBorder().setSize(getConfig().getInt("world-border.max-size", 256));
    	
    	// Initialize MessageUtils for messages
    	MessageUtils.init();
    	
    	// Instantiate database connection and connect
    	new DatabaseManager(this, true);
    	
        // Enable test commands
        getCommand("start-hg").setExecutor(new TestCommand(this));
        getCommand("current-phase").setExecutor(new TestCommand(this));
        getCommand("restart-hg-server").setExecutor(new TestCommand(this));
        getCommand("test").setExecutor(new TestCommand(this));
        getCommand("messages").setExecutor(new TestCommand(this));
        
        // Enable class selection commands
        Arrays.asList(PlayerClass.values()).forEach(c -> {
        	getCommand(c.name()).setExecutor(new ClassCommand());
        });
        
        // Initialize PlayerManager listener
        getServer().getPluginManager().registerEvents(new PlayerManager(), this);
        
        // Initialize Hunger Games waiting phase
        SpigotPlugin.setPhase(HGPhase.WAITING_FOR_HG);
        if (getConfig().getBoolean("server.auto-start", true)) {
        	new ServerSchedulers(this).waitingPhase();
        }
        
    }
    
    @Override
    public void onDisable() {
    	try {
			DatabaseManager.disconnectToDatabase();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
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
