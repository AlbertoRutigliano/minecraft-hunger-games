package lar.minecraft.hg.enums;

public enum ConfigProperty {

	min_players("min-players", 1), // Minimum players to start the Hunger Games
	chest_spawn_num("chest-spawn-num", 2), // Number of supply drop chest to spawn
	
	// Durations
	duration_lobby("durations.lobby", 20), // Lobby phase duration in seconds where players can login and choose their class
	duration_safe_area("durations.safe-area", 20), // Safe area duration in seconds where players can destroy blocks and collect items but cannot hit other players
	duration_winner_celebrations("durations.winner-celebrations", 15), // Winner celebrations duration in seconds
	duration_fireworks("durations.fireworks", 15), // Fireworks celebrations duration in seconds
	duration_first_supply_drop("durations.first-supply-drop", 5), // How many seconds after the start of the game (after safe-area phase) is the supply chest dropped
	duration_supply_drop("durations.supply-drop", 10), // Interval seconds between every supply chest drop
	duration_idle_timeout("durations.idle-timeout", 0), // How many minutes a player can be stationary during the playing phase before getting kicked
	
	// World border
	world_border_max_size("world-border.max-size", 128), // Max world size dimension in blocks
	world_border_min_size("world-border.min-size", 2), // Min world size dimension in blocks
	world_border_collapse("world-border.collapse", true), // Enable the world collapse feature
	world_border_collapse_counter_seconds("world-border.collapse-counter-seconds", 60), // How many seconds to reduce world border by collapse-radius value
	world_border_collapse_radius("world-border.collapse-radius", 20), // World border collapse radius
	
	// Server
	server_id("server.id", 27), // Server ID
	server_auto_start("server.auto-start", false), // When server is started, automatically start a new Hunger Games session
	server_auto_restart("server.auto-restart", false), // Automatically restart server when Hunger Games session is finished
	
	// Database
	database_enable("database.enable", false), // Enable and connect to a database to save Hunger Games information and statistics
	database_connection_string("database.connection-string", "jdbc:mysql://127.0.0.1:3306/hunger_games"), // Connection string for the database
	database_user("database.user", "test"), // Database connection user credentials
	database_password("database.password", "test"); // Database connection user credentials

	private String key;
	private Object defaultValue;
	    
	ConfigProperty(String propertyKey, Object defaultValue) {
		this.key = propertyKey;
		this.defaultValue = defaultValue;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}
