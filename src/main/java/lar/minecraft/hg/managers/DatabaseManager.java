package lar.minecraft.hg.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DatabaseManager {
	private static Plugin plugin;
	private static boolean databaseEnabled = false;
	private static String dbConnectionString;
	private static String dbUser;
	private static String dbPassword;
	private static List<String> dbInitQueries;
	
	public static boolean isDatabaseEnabled() {
		return databaseEnabled;
	}
	
	public static String getDbConnectionString() {
		return dbConnectionString;
	}

	/**
	 * Initiate DB connection and connect to database
	 * @param connectionString Database connection String
	 * @param dbUser Database user (user must have permission to create tables and views)
	 * @param dbPassword Database user password
	 * @param dbInitQueries List of queries to lunch when database connection is initiating
	 */
	public static void init(Plugin plugin, String connectionString, String dbUser, String dbPassword) {
		DatabaseManager.plugin = plugin; 
		DatabaseManager.databaseEnabled = true;
		DatabaseManager.dbConnectionString = connectionString; 
		DatabaseManager.dbUser = dbUser;
		DatabaseManager.dbPassword = dbPassword;
		DatabaseManager.dbInitQueries = Arrays.asList(
				"CREATE TABLE IF NOT EXISTS hg_games ("
					  	+ " server_id int(11) NOT NULL,"
					  	+ " id int(11) NOT NULL,"
					  	+ " winner_uuid varchar(100) DEFAULT NULL,"
					  	+ " win_datetime datetime DEFAULT NULL,"
					  	+ " game_start_datetime datetime DEFAULT NULL,"
					  	+ " game_phase varchar(100) DEFAULT NULL,"
					  	+ " UNIQUE KEY hg_games_server_id_IDX (server_id,id) USING BTREE"
					  	+ " )"
		  		,	"CREATE TABLE IF NOT EXISTS played_hg_games ("
						+ " server_id int(11) NOT NULL,"
					  	+ " id int(11) NOT NULL,"
					  	+ " player_uuid varchar(100) NOT NULL,"
					  	+ " UNIQUE KEY played_hg_games_server_id_IDX (server_id,id,player_uuid) USING BTREE"
					  	+ " )"
		  		,	"CREATE TABLE IF NOT EXISTS players ("
						+ " uuid varchar(100) NOT NULL,"
					  	+ " name varchar(100) NOT NULL,"
					  	+ " premium_expire_date date DEFAULT NULL,"
					  	+ " last_time_online datetime DEFAULT NULL,"
					  	+ " UNIQUE KEY players_uuid_IDX (uuid) USING BTREE"
					  	+ " )"
			  	,	"CREATE OR REPLACE"
						+ " ALGORITHM = UNDEFINED VIEW v_Scoreboard AS ("
						+ " select"
						+ " players.uuid AS uuid,"
						+ " players.name AS name,"
						+ " count(hg_games.winner_uuid) AS wins_count"
						+ " from"
						+ " (players"
						+ " left join hg_games on"
						+ " ((players.uuid = hg_games.winner_uuid)))"
						+ " group by"
						+ " players.uuid);"
				,	"CREATE OR REPLACE"
						+ " ALGORITHM = UNDEFINED VIEW v_players AS ("
						+ " select"
						+ " players.uuid AS uuid,"
					    + " players.name AS name,"
					    + " (case"
					    + " when ((players.premium_expire_date is not null)"
			    		+ " 				        and (curdate() <= players.premium_expire_date)) then 1"
				        + " else 0"
					    + " end) AS premium,"
					    + " players.premium_expire_date AS premium_expire_date,"
					    + " players.last_time_online AS last_time_online"
						+ " from"
						+ " players);"
				);
		
        try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try (Connection dbConnection = getConnection()) {
            if (dbConnection != null && !dbConnection.isClosed()) {
                launchInitQueries(dbConnection);
            }
        } catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Prepare database necessary tables
	 * @return 0 if everything is ok
	 */
	public static void launchInitQueries(Connection dbConnection) throws SQLException {
		plugin.getLogger().info("Launching DB initialization queries");
		for (String query : dbInitQueries) {
			executeUpdate(query);
		}
	}
	
	/**
	 * Save the information of the new match
	 * @param serverId
	 * @return the new hg game Id
	 */
	public static int createHGGame(int serverId){
		if (isDatabaseEnabled()) {
            try (Connection dbConnection = getConnection();
        		PreparedStatement selectStatement = dbConnection.prepareStatement("SELECT MAX(id) AS foundId FROM hg_games WHERE server_id = ?");
        		PreparedStatement insertStatement = dbConnection.prepareStatement("INSERT INTO hg_games (server_id, id) VALUES (?, ?)")) {
            	selectStatement.setInt(1, serverId);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    int hgGameId = 1;
                    if (resultSet.next()) {
                        hgGameId = resultSet.getInt("foundId") + 1;
                    }
                    insertStatement.setInt(1, serverId);
                    insertStatement.setInt(2, hgGameId);
                    insertStatement.executeUpdate();
                    return hgGameId;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
		}
		
		return 0;
	}
	
	/**
	 * Save game starting date time
	 * @param ServerId
	 * @param HGGameId
	 */
	public static void saveStartingDateTime(int ServerId, int HGGameId) {
		executeUpdate(String.format("UPDATE hg_games SET game_start_datetime = NOW() WHERE server_id = %d AND id = %d;", ServerId, HGGameId));
	}
	
	/**
	 * Save game phase
	 * @param ServerId
	 * @param HGGameId
	 */
	public static void saveGamePhase(int ServerId, int HGGameId, String phase) {
		executeUpdate(String.format("UPDATE hg_games SET game_phase = '%s' WHERE server_id = %d AND id = %d;", phase, ServerId, HGGameId));
	}
	
	/**
	 * Add player or update the existing record into players table
	 * @param player
	 */
	public static void addPlayer(Player player) {
		if (isDatabaseEnabled()) {
	        try (Connection dbConnection = getConnection();
        		PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT COUNT(*) AS playerFound FROM players WHERE uuid = ?");
        		PreparedStatement insertStmt = dbConnection.prepareStatement("INSERT INTO players (uuid, name, last_time_online) VALUES (?, ?, NOW())");
        		PreparedStatement updateStmt = dbConnection.prepareStatement("UPDATE players SET last_time_online = NOW() WHERE uuid = ?")) {
	            selectStmt.setString(1, player.getUniqueId().toString());
	            try (ResultSet resultSet = selectStmt.executeQuery()) {
	                if (resultSet.next() && resultSet.getInt("playerFound") == 0) {
	                    insertStmt.setString(1, player.getUniqueId().toString());
	                    insertStmt.setString(2, player.getName());
	                    insertStmt.executeUpdate();
	                } else {
	                    updateStmt.setString(1, player.getUniqueId().toString());
	                    updateStmt.executeUpdate();
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	
	/**
	 * Save the information of all players that joined the game
	 * @param ServerId
	 * @param HGGameId
	 * @param player
	 */
	public static void addPlayerJoin(int serverId, int hgGameId, Player player) {
		addPlayer(player);
		
		if (isDatabaseEnabled()) {
	        try (Connection dbConnection = getConnection();
        		PreparedStatement selectStmt = dbConnection.prepareStatement("SELECT COUNT(*) AS playerFound FROM played_hg_games WHERE server_id = ? AND id = ? AND player_uuid = ?");
	             PreparedStatement insertStmt = dbConnection.prepareStatement("INSERT INTO played_hg_games (server_id, id, player_uuid) VALUES (?, ?, ?)")) {
	            selectStmt.setInt(1, serverId);
	            selectStmt.setInt(2, hgGameId);
	            selectStmt.setString(3, player.getUniqueId().toString());
	            try (ResultSet resultSet = selectStmt.executeQuery()) {
	                if (resultSet.next() && resultSet.getInt("playerFound") == 0) {
	                    insertStmt.setInt(1, serverId);
	                    insertStmt.setInt(2, hgGameId);
	                    insertStmt.setString(3, player.getUniqueId().toString());
	                    insertStmt.executeUpdate();
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	/**
	 * Save the information of all players that joined the game
	 * @param ServerId
	 * @param HGGameId
	 * @param player
	 * @param chosenClass
	 */
	public static void updatePlayerClass(int ServerId, int HGGameId, Player player, String chosenClass) {
		//Add player into players table if not existing
		addPlayer(player);
		//Add player into players that joined server
		addPlayerJoin(ServerId, HGGameId, player);
		//Update player chosen class
		executeUpdate(String.format("UPDATE played_hg_games SET chosen_class = '%s' WHERE server_id = %d AND id = %d AND player_uuid = '%s'", chosenClass, ServerId, HGGameId, player.getUniqueId().toString()));
	}
	
	/**
	 * Save the information of all players that joined the game
	 * @param ServerId
	 * @param HGGameId
	 * @param player
	 * @param chosenClass
	 */
	public static void setPlayerMatchPlayed(int ServerId, int HGGameId, Player player) {
		//Add player into players table if not existing
		addPlayer(player);
		//Add player into players that joined server
		addPlayerJoin(ServerId, HGGameId, player);
		//Update player chosen class
		executeUpdate(String.format("UPDATE played_hg_games SET match_played = true WHERE server_id = %d AND id = %d AND player_uuid = '%s'", ServerId, HGGameId, player.getUniqueId().toString()));
	}
	
	/**
	 * Save the information of the player that won the match
	 * @param ServerId
	 * @param HGGameId
	 * @param player the winner
	 */
	public static void savePlayerWin(int ServerId, int HGGameId, Player player) {
		executeUpdate(String.format("UPDATE hg_games SET winner_uuid = '%s', win_datetime = NOW() WHERE server_id = %d AND id = %d", player.getUniqueId().toString(), ServerId, HGGameId));
	}

	/**
	 * Get the uuid of the player that won last match
	 * @param serverId
	 * @return Player uuid or string empty
	 */
	public static String getLastWinner(int serverId) {
		if (isDatabaseEnabled()) {
	        try (Connection dbConnection = getConnection();
	             PreparedStatement selectStatementmt = dbConnection.prepareStatement("SELECT winner_uuid FROM hg_games WHERE server_id = ? ORDER BY game_start_datetime DESC LIMIT 1")) {
	        	selectStatementmt.setInt(1, serverId);
	            try (ResultSet resultSet = selectStatementmt.executeQuery()) {
	                if (resultSet.next()) {
	                    return resultSet.getString("winner_uuid");
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		}
        
        return "";
    }
	
	/**
	 * Check if the player is Premium
	 * @param playerUUID
	 * @return true if player is premium, false if player is not premium or not exist
	 */
	public static boolean isPlayerPremium(String playerUUID) {
		if (isDatabaseEnabled()) {
	        try (Connection dbConnection = getConnection();
	             PreparedStatement selectStatement = dbConnection.prepareStatement("SELECT premium FROM v_players WHERE uuid = ?")) {
	        	selectStatement.setString(1, playerUUID);
	            try (ResultSet resultSet = selectStatement.executeQuery()) {
	                if (resultSet.next()) {
	                    return resultSet.getBoolean("premium");
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		}
        
        return false;
    }
	
	/**
	 * Get count of match that player has won
	 * @param playerUUID
	 * @return Count of match that player has won
	 */
	public static int getPlayerWinCount(String playerUUID) {
		if (isDatabaseEnabled()) {
	        try (Connection dbConnection = getConnection();
	             PreparedStatement selectStatement = dbConnection.prepareStatement("SELECT COUNT(winner_uuid) AS winCount FROM hg_games WHERE winner_uuid = ?")) {
	        	selectStatement.setString(1, playerUUID);
	            try (ResultSet resultSet = selectStatement.executeQuery()) {
	                if (resultSet.next()) {
	                    return resultSet.getInt("winCount");
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		}
        
        return 0;
    }
	
	/**
	 * Get all players win count
	 * @return Map made up of players names and win count ordered by win count from highest to lowest
	 */
	public static Map<String, Integer> getGlobalScoreboard() {
        Map<String, Integer> result = new HashMap<>();
		if(isDatabaseEnabled()) {
	        try (Connection dbConnection = getConnection();
	             PreparedStatement selectStatementmt = dbConnection.prepareStatement("SELECT name, wins_count FROM v_Scoreboard ORDER BY wins_count DESC");
	             ResultSet resultSet = selectStatementmt.executeQuery()) {
	            while (resultSet.next()) {
	                result.put(resultSet.getString("name"), resultSet.getInt("wins_count"));
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		}
        return result;
    }
	
	/**
	 * Open new DB connection
	 * @return Opened DB connection
	 * @throws SQLException
	 */
	private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbConnectionString, dbUser, dbPassword);
    }
	
	/**
	 * Create an executeUpdate statement and execute it based on the sql query
	 * @param sql Sql query to execute
	 * @throws SQLException
	 */
	private static void executeUpdate(String query, Object... params) {
        if (isDatabaseEnabled()) {
            try (Connection dbConnection = getConnection();
                 PreparedStatement stmt = dbConnection.prepareStatement(query)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error executing query: " + query);
                plugin.getLogger().severe(e.getMessage());
            }
        }
    }
	
}

