package lar.minecraft.hg.entities;

import java.util.UUID;

import org.bukkit.boss.BossBar;

import lar.minecraft.hg.enums.PlayerClass;

public class PlayerExtra {
	
	private UUID uuid;
	private String name;
	private PlayerClass playerClass = null;
	private boolean lastWinner = false;
	private boolean premium = false;
	private int winCount = 0;
	private BossBar bossBar;
	
	public PlayerExtra(UUID uuid, String name) {
		this(uuid, name, false);
	}
	
	public PlayerExtra(UUID uuid, String name, boolean lastWinner) {
		this(uuid, name, lastWinner, false);
	}
	
	public PlayerExtra(UUID uuid, String name, boolean lastWinner, boolean premium) {
		this(uuid, name, lastWinner, false, 0);
	}
	
	public PlayerExtra(UUID uuid, String name, boolean lastWinner, boolean premium, int winCount) {
		this.uuid = uuid;
		this.name = name;
		this.lastWinner = lastWinner;
		this.premium = premium;
		this.winCount = winCount;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public void setPlayerClass(PlayerClass playerClass) {
		this.playerClass = playerClass;
	}

	public boolean isLastWinner() {
		return lastWinner;
	}

	public void setLastWinner(boolean lastWinner) {
		this.lastWinner = lastWinner;
	}

	public boolean isPremium() {
		return premium;
	}

	public void setPremium(boolean premium) {
		this.premium = premium;
	}

	public int getWinCount() {
		return winCount;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}
	
	public BossBar getBossBar() {
		return bossBar;
	}

	public void setBossBar(BossBar bossBar) {
		this.bossBar = bossBar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerExtra other = (PlayerExtra) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
}