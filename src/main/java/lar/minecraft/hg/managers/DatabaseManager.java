package lar.minecraft.hg.managers;
import java.sql.*;
import org.bukkit.entity.Player;

// TODO Make optional connection
public class DatabaseManager {
	
	// TODO Add these properties into config.yml file
	private static String connectionString = "jdbc:mysql://localhost:3306/hunger_games";
	private static String databaseUser = "multicraft";
	private static String databasePassword = "4E3M4dYSlP9g2yC#";
	private static Connection dbConnection;
	
	public static void connectToDatabase() throws SQLException, ClassNotFoundException {
		if (dbConnection != null) {
			if (!dbConnection.isClosed()) {
				return;
			}
		}
		
		Class.forName("com.mysql.jdbc.Driver");
		dbConnection = DriverManager.getConnection(connectionString, databaseUser, databasePassword);
	}
	
	public static void disconnectToDatabase() throws SQLException, ClassNotFoundException {
		if (dbConnection != null) {
			if (!dbConnection.isClosed()) {
				dbConnection.close();				
			}
		}
	}
	
	/**
	 * Save the information of the new match
	 * @param serverId
	 * @return the new hg game Id
	 */
	public static int createHGGame(int ServerId) {
		try {
			connectToDatabase();
			
			int hgGameId = 1;
			Statement statementRead = dbConnection.createStatement();
			Statement statementInsert = dbConnection.createStatement();
			ResultSet resultSet = statementRead.executeQuery(String.format("SELECT MAX(id) AS foundId FROM hg_games WHERE server_id = %d;", ServerId));
			
			while (resultSet.next()) {
				int foundRows = resultSet.getInt("foundId");
				hgGameId = foundRows + 1;
			}
			statementInsert.executeUpdate(String.format("INSERT INTO hg_games (server_id, id) VALUES (%d, %d);", ServerId, hgGameId));

			disconnectToDatabase();
			return hgGameId;
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Save the information of all players that joined the game
	 * @param ServerId
	 * @param HGGameId
	 * @param player
	 */
	public static void addPlayerJoin(int ServerId, int HGGameId, Player player) {
		try {
			connectToDatabase();
			
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
			
			disconnectToDatabase();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Save the information of the player that won the match
	 * @param ServerId
	 * @param HGGameId
	 * @param player the winner
	 */
	public static void savePlayerWin(int ServerId, int HGGameId, Player player) {
		try {
			connectToDatabase();
			
			Statement statementUpdate = dbConnection.createStatement();
			statementUpdate.executeUpdate(String.format("UPDATE hg_games SET winner_uuid = '%s', win_datetime = NOW() WHERE server_id = %d AND id = %d", player.getUniqueId().toString(), ServerId, HGGameId));

			disconnectToDatabase();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

