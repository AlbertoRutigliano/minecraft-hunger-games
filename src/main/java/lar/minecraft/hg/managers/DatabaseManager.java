package lar.minecraft.hg.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.entity.Player;

import lar.minecraft.hg.enums.ConfigProperty;
import lar.minecraft.hg.utils.ConfigUtils;

public class DatabaseManager {
	
	private static boolean databaseEnabled = false;
	private static String dbConnectionString;
	private static String dbUser;
	private static String dbPassword;
	private static Connection dbConnection;
	
	public static boolean isDatabaseEnabled() {
		return databaseEnabled;
	}

	public DatabaseManager(boolean directlyConnect) {
		databaseEnabled = ConfigUtils.getBoolean(ConfigProperty.database_enable);
		
		// Directly connect to database when creating Database Manager
		if (isDatabaseEnabled()) {
			dbConnectionString = ConfigUtils.getString(ConfigProperty.database_connection_string); 
			dbUser = ConfigUtils.getString(ConfigProperty.database_user);
			dbPassword = ConfigUtils.getString(ConfigProperty.database_password);
			
			if (directlyConnect) {
				try {
					connectToDatabase();
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
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
				statementCreate.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS hg_games (server_id int NOT NULL, id int NOT NULL, winner_uuid varchar(100), win_datetime datetime)"));
				statementCreate.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS played_hg_games (server_id int NOT NULL, id int NOT NULL, player_uuid varchar(100))"));
				statementCreate.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS players (uuid varchar(100), name varchar(100))"));
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
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT COUNT(*) AS playerFound FROM players WHERE uuid = '%s';", player.getUniqueId().toString()));
				while (resultSet.next()) {
					int foundRows = resultSet.getInt("playerFound");
					if (foundRows == 0) {
						statementInsert.executeUpdate(String.format("INSERT INTO players (uuid, name) VALUES ('%s', '%s');", player.getUniqueId(), player.getName()));
					}
				}
	
				//Add player into played hg games if not existing
				resultSet = statementRead.executeQuery(String.format("SELECT COUNT(*) AS playerFound FROM played_hg_games WHERE server_id = %d AND id = %d AND player_uuid = '%s';", ServerId, HGGameId, player.getUniqueId().toString()));			
				while (resultSet.next()) {
					int foundRows = resultSet.getInt("playerFound");
					if (foundRows == 0) {
						statementInsert.executeUpdate(String.format("INSERT INTO played_hg_games (server_id, id, player_uuid) VALUES ('%d', '%d', '%s');", ServerId, HGGameId, player.getUniqueId(), player.getName()));
					}
				}
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
				ResultSet resultSet = statementRead.executeQuery(String.format("SELECT premium FROM players WHERE uuid = '%s';", playerUUID));
				boolean isPremium = false;
				
				while (resultSet.next()) {
					isPremium = resultSet.getBoolean("premium");
				}
				return isPremium;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
}

