package lar.minecraft.hg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lar.minecraft.hg.commands.ClassCommand;
import lar.minecraft.hg.commands.ScoreboardCommand;
import lar.minecraft.hg.commands.TestCommand;
import lar.minecraft.hg.enums.ConfigProperty;
import lar.minecraft.hg.enums.HGPhase;
import lar.minecraft.hg.enums.PlayerClass;
import lar.minecraft.hg.managers.DatabaseManager;
import lar.minecraft.hg.managers.PlayerManager;
import lar.minecraft.hg.managers.ServerManager;
import lar.minecraft.hg.utils.ConfigUtils;
import lar.minecraft.hg.utils.MessageUtils;

public class SpigotPlugin extends JavaPlugin {
	
	public static Server server;
	
	public static FileConfiguration config;
	
	public static Properties serverProps = new Properties();
	
	public static HGPhase phase = HGPhase.WAITING;
	
	public static int serverId = 0;
	
	public static World world;
	
	public static Location newSpawnLocation;
	
    @Override
    public void onEnable() {
		server = getServer();
		
		try {
			serverProps.load(new FileInputStream("server.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		world = server.getWorld(serverProps.getProperty("level-name"));
		config = getConfig();
		saveDefaultConfig();
		ConfigUtils.setConfig(config);
		
		// Adjust world spawn location before starting all phases
		// On player join and in game phase players are also teleported in a random location inside world border
		// Used to prevent playing a game blocked underground or in water biome
		newSpawnLocation = world.getSpawnLocation().clone();
		int maxSpawnRetries = 0; // Max retries to find a ground block inside world size
		while (world.getHighestBlockAt(newSpawnLocation).isLiquid() && maxSpawnRetries < 20) {
			newSpawnLocation = ServerManager.getSurfaceRandomLocation(Integer.parseInt(serverProps.getProperty("max-world-size"))
					, world.getSpawnLocation()
					, 0
					, 2
					, 0);
			maxSpawnRetries++;
		}
		if (maxSpawnRetries == 20) {
			ServerManager.restartServer();
		}
		this.getLogger().info("World border center set to: " + newSpawnLocation);
		world.setSpawnLocation(newSpawnLocation);

    	// Create world border
    	world.getWorldBorder().setCenter(newSpawnLocation);
    	world.getWorldBorder().setSize(ConfigUtils.getInt(ConfigProperty.world_border_max_size));
		
		ServerSchedulers.init(this);
    	serverId = ConfigUtils.getInt(ConfigProperty.server_id);
    	world.setDifficulty(Difficulty.NORMAL);
    	
    	// Initialize MessageUtils for messages
    	MessageUtils.init();
    	
    	// Initiate DB connection and connect to database
    	boolean databaseEnabled = ConfigUtils.getBoolean(ConfigProperty.database_enable);
		String dbConnectionString = ConfigUtils.getString(ConfigProperty.database_connection_string); 
		String dbUser = ConfigUtils.getString(ConfigProperty.database_user);
		String dbPassword = ConfigUtils.getString(ConfigProperty.database_password);
    	DatabaseManager.init(databaseEnabled, dbConnectionString, dbUser, dbPassword);
    	
        // Enable test commands
        getCommand("start-hg").setExecutor(new TestCommand(this));
        getCommand("current-phase").setExecutor(new TestCommand(this));
        getCommand("restart-hg-server").setExecutor(new TestCommand(this));
        getCommand("test").setExecutor(new TestCommand(this));
        getCommand("messages").setExecutor(new TestCommand(this));
        
        // Enable game commands
        getCommand("scoreboard").setExecutor(new ScoreboardCommand());
		getCommand("scoreboard").setTabCompleter(new ScoreboardCommand());
        
        // Enable class selection commands
        Arrays.asList(PlayerClass.values()).forEach(c -> {
        	getCommand(c.name()).setExecutor(new ClassCommand());
        });
        
        // Initialize PlayerManager listener
        getServer().getPluginManager().registerEvents(new PlayerManager(), this);
        
        SpigotPlugin.setPhase(HGPhase.WAITING);
        // Initialize Hunger Games waiting phase
        if (ConfigUtils.getBoolean(ConfigProperty.server_auto_start)) {
        	ServerSchedulers.waitingPhase();
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
    	return phase.equals(HGPhase.WAITING);
    }
    
    public static boolean isPlaying() {
    	return phase.equals(HGPhase.PLAYING);
    }
}
