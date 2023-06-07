package lar.minecraft.hg.managers;

import java.sql.SQLException;
import java.time.LocalDate;

import org.bukkit.entity.Player;

import lar.minecraft.hg.dbModels.servers_players;

public class QueryManager {
	
	/**
	 * Add player win to database
	 * @param player that win the match
	 */
	public static void addPlayerWin(Player player) {
		try {
    		servers_players server_player = new servers_players(1, player.getName(), player.getUniqueId().toString(), 0, null, null);
    		if (DatabaseManager.ExecuteRead(servers_players.class.getSimpleName(), "player_name = '" + player.getName() + "'") == 0) {
				DatabaseManager.ExecuteInsert(servers_players.class.getSimpleName(), server_player);
    		}
			LocalDate now = LocalDate.now();
			String last_win = String.format("%d-%d-%d %d:%d:%d", now.getYear(), now.getMonthValue(), now.getDayOfMonth(), 0, 0, 0);
			DatabaseManager.ExecuteUpdate(servers_players.class.getSimpleName(), "last_win = '" + last_win + "'", "player_name = '" + player.getName() + "'");
			DatabaseManager.ExecuteUpdate(servers_players.class.getSimpleName(), "win_count = win_count + 1", "player_uuid = '" + player.getUniqueId() + "'");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
