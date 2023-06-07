package lar.minecraft.hg.dbModels;

import java.sql.Date;

public class servers_players implements imodel {
	public int server_id;
	public String player_name;
	public String player_uuid;
	public int win_count;
	public Date last_win;
	public Date last_play;
	
	public servers_players () {
		
	}
	
	public servers_players (int server_id, String player_name, String player_uuid, int win_count, Date last_win, Date last_play) {
		this.server_id = server_id;
		this.player_name = player_name;
		this.player_uuid = player_uuid;
		this.win_count = win_count;
		this.last_win = last_win;
		this.last_play = last_play;
	}
}
