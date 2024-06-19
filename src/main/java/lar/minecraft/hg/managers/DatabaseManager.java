package lar.minecraft.hg.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class DatabaseManager {
	
	private static boolean databaseEnabled = false;
	private static String dbConnectionString;
	private static String dbUser;
	private static String dbPassword;
	private static Connection dbConnection;
	
	public static boolean isDatabaseEnabled() {
		return databaseEnabled;
	}
	
	public static String getDbConnectionString() {
		return dbConnectionString;
	}

	/**
	 * Initiate DB connection and connect to database
	 * @param databaseEnabled If true a Database connection will be instantiated with the passed parameters
	 * @param connectionString Database connection String
	 * @param dbUser Database user (user must have permission to create tables and views)
	 * @param dbPassword Database user password
	 */
	public static void init(boolean databaseEnabled, String connectionString, String dbUser, String dbPassword) {
		DatabaseManager.databaseEnabled = databaseEnabled;
		
		// Set static fields and connect to database
		if (isDatabaseEnabled()) {
			DatabaseManager.dbConnectionString = connectionString; 
			DatabaseManager.dbUser = dbUser;
			DatabaseManager.dbPassword = dbPassword;
			
			try {
				connectToDatabase();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Connect to Database
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void connectToDatabase() throws SQLException, ClassNotFoundException {
		if (isDatabaseEnabled()) {
			if (dbConnection != null) {
				if (!dbConnection.isClosed()) {
					return;
				}
			}
		
			Class.forName("com.mysql.jdbc.Driver");
			dbConnection = DriverManager.getConnection(dbConnectionString, dbUser, dbPassword);
			
			// Create necessary tables if not present
			createTables();
		}
	}
	
	/**
	 * Disconnect from Database
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void disconnectToDatabase() throws SQLException, ClassNotFoundException {
		if (isDatabaseEnabled()) {
			if (dbConnection != null) {
				if (!dbConnection.isClosed()) {
					dbConnection.close();				
				}
			}
		}
	}
	
	/**
	 * Prepare database necessary tables
	 * @return 0 if everything is ok
	 */
	public static int createTables() {
		if (isDatabaseEnabled()) {
			try {
				Statement statementCreate = dbConnection.createStatement();
				statementCreate.executeUpdate("CREATE TABLE IF NOT EXISTS `hg_games` ("
										  	+ " `server_id` int(11) NOT NULL,"
										  	+ " `id` int(11) NOT NULL,"
										  	+ " `winner_uuid` varchar(100) DEFAULT NULL,"
										  	+ " `win_datetime` datetime DEFAULT NULL,"
										  	+ " `game_start_datetime` datetime DEFAULT NULL,"
										  	+ " UNIQUE KEY `hg_games_server_id_IDX` (`server_id`,`id`) USING BTREE"
										  	+ " )");
				statementCreate.executeUpdate("CREATE TABLE IF NOT EXISTS `played_hg_games` ("
											+ " `server_id` int(11) NOT NULL,"
										  	+ " `id` int(11) NOT NULL,"
										  	+ " `player_uuid` varchar(100) NOT NULL,"
										  	+ " UNIQUE KEY `played_hg_games_server_id_IDX` (`server_id`,`id`,`player_uuid`) USING BTREE"
										  	+ " )");
				statementCreate.executeUpdate("CREATE TABLE IF NOT EXISTS `players` ("
											+ " `uuid` varchar(100) NOT NULL,"
										  	+ " `name` varchar(100) NOT NULL,"
										  	+ " `premium_expire_date` date DEFAULT NULL,"
										  	+ " `last_time_online` datetime DEFAULT NULL,"
										  	+ " UNIQUE KEY `players_uuid_IDX` (`uuid`) USING BTREE"
										  	+ " )");
				statementCreate.executeUpdate("CREATE OR REPLACE"
											+ " ALGORITHM = UNDEFINED VIEW `hunger_games`.`v_Scoreboard` AS ("
											+ " select"
											+ "     `hunger_games`.`players`.`name` AS `name`,"
											+ "     count(`hunger_games`.`hg_games`.`winner_uuid`) AS `wins_count`"
											+ " from"
											+ "     (`hunger_games`.`hg_games`"
											+ " right outer join `hunger_games`.`players` on"
											+ "     ((`hunger_games`.`players`.`uuid` = `hunger_games`.`hg_games`.`winner_uuid`)))"
											+ " group by"
											+ "     `hunger_games`.`hg_games`.`winner_uuid`);");
				statementCreate.executeUpdate("CREATE OR REPLACE"
											+ " ALGORITHM = UNDEFINED VIEW `v_players` AS ("
											+ " select"
											+ "`players`.`uuid` AS `uuid`,"
										    + " `players`.`name` AS `name`,"
										    + " (case"
										    + " when ((`players`.`premium_expire_date` is not null)"
							        		+ " 				        and (curdate() <= `players`.`premium_expire_date`)) then 1"
									        + " else 0"
										    + " end) AS `premium`,"
										    + " `players`.`premium_expire_date` AS `premium_expire_date`,"
										    + " `players`.`last_time_online` AS `last_time_online`"
											+ " from"
											+ "`players`);");
				statementCreate.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	/**
	 * Save the information of the new match
	 * @param serverId
	 * @return the new hg game Id
	 */
	public static int createHGGame(int ServerId){
		if (isDatabaseEnabled()) {
			try {
				int hgGameId = 1;
				Statement statementRead = dbConnection.createStatement();
				Statement statementInsert = dbConnection.createStatement();
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT MAX(id) AS foundId FROM hg_games WHERE server_id = %d;", ServerId));
				
				while (resultSet.next()) {
					int foundRows = resultSet.getInt("foundId");
					hgGameId = foundRows + 1;
				}
				statementInsert.executeUpdate(String.format("INSERT INTO hg_games (server_id, id) VALUES (%d, %d);", ServerId, hgGameId));

				statementRead.close();
				statementInsert.close();
				
				return hgGameId;
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
		if (isDatabaseEnabled()) {
			try {
				Statement statementUpdate = dbConnection.createStatement();
				statementUpdate.executeUpdate(String.format("UPDATE hg_games SET game_start_datetime = NOW() WHERE server_id = %d AND id = %d;", ServerId, HGGameId));
				statementUpdate.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Add player or update the existing record into players table
	 * @param player
	 */
	public static void addPlayer(Player player) {
		if (isDatabaseEnabled()) {
			try {
				Statement statementRead = dbConnection.createStatement();
				Statement statementInsert = dbConnection.createStatement();
				
				//Add player into players table if not existing
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT COUNT(*) AS playerFound FROM players WHERE uuid = '%s';", player.getUniqueId().toString()));
				while (resultSet.next()) {
					int foundRows = resultSet.getInt("playerFound");
					if (foundRows == 0) {
						statementInsert.executeUpdate(String.format("INSERT INTO players (uuid, name, last_time_online) VALUES ('%s', '%s', NOW());", player.getUniqueId(), player.getName()));
					}else {
						statementInsert.executeUpdate(String.format("UPDATE players SET last_time_online = NOW() WHERE uuid = '%s';", player.getUniqueId()));
					}
				}
				
				statementRead.close();
				statementInsert.close();
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
	public static void addPlayerJoin(int ServerId, int HGGameId, Player player) {
		if (isDatabaseEnabled()) {
			try {
				Statement statementRead = dbConnection.createStatement();
				Statement statementInsert = dbConnection.createStatement();
				
				//Add player into players table if not existing
				DatabaseManager.addPlayer(player);
	
				//Add player into played hg games if not existing
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT COUNT(*) AS playerFound FROM played_hg_games WHERE server_id = %d AND id = %d AND player_uuid = '%s';", ServerId, HGGameId, player.getUniqueId().toString()));			
				while (resultSet.next()) {
					int foundRows = resultSet.getInt("playerFound");
					if (foundRows == 0) {
						statementInsert.executeUpdate(String.format("INSERT INTO played_hg_games (server_id, id, player_uuid) VALUES ('%d', '%d', '%s');", ServerId, HGGameId, player.getUniqueId(), player.getName()));
					}
				}
				statementRead.close();
				statementInsert.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Save the information of the player that won the match
	 * @param ServerId
	 * @param HGGameId
	 * @param player the winner
	 */
	public static void savePlayerWin(int ServerId, int HGGameId, Player player) {
		if (isDatabaseEnabled()) {
			try {
				Statement statementUpdate = dbConnection.createStatement();
				statementUpdate.executeUpdate(String.format("UPDATE hg_games SET winner_uuid = '%s', win_datetime = NOW() WHERE server_id = %d AND id = %d", player.getUniqueId().toString(), ServerId, HGGameId));
				statementUpdate.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get the uuid of the player that won last match
	 * @param serverId
	 * @return Player uuid or string empty
	 */
	public static String getLastWinner(int ServerId){
		if (isDatabaseEnabled()) {
			try {
				Statement statementRead = dbConnection.createStatement();
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT winner_uuid FROM hg_games WHERE server_id = %d ORDER BY game_start_datetime DESC LIMIT 1;", ServerId));
				String winnerUUID = "";
				
				while (resultSet.next()) {
					if (resultSet.getString("winner_uuid") != null) {
						winnerUUID = resultSet.getString("winner_uuid");
					}
				}
				statementRead.close();
				
				return winnerUUID;
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
	public static boolean isPlayerPremium(String playerUUID){
		if (isDatabaseEnabled()) {
			try {
				Statement statementRead = dbConnection.createStatement();
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT premium FROM v_players WHERE uuid = '%s';", playerUUID));
				boolean isPremium = false;
				
				while (resultSet.next()) {
					isPremium = resultSet.getBoolean("premium");
				}
				statementRead.close();
				
				return isPremium;
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
			try {
				Statement statementRead = dbConnection.createStatement();
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT COUNT(winner_uuid) AS winCount FROM hg_games WHERE winner_uuid = '%s';", playerUUID));
				int winCount = 0;
				
				while (resultSet.next()) {
					winCount = resultSet.getInt("winCount");
				}
				statementRead.close();
				
				return winCount;
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
		if (isDatabaseEnabled()) {
			try {
				Statement statementRead = dbConnection.createStatement();
				ResultSet resultSet = statementRead.executeQuery("SELECT name, wins_count FROM v_Scoreboard ORDER BY wins_count");
				
				while (resultSet.next()) {
					result.put(resultSet.getString("name"), resultSet.getInt("wins_count"));
				}
				statementRead.close();
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
}

