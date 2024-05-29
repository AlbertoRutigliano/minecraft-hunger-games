package lar.minecraft.hg.entities;

import java.util.UUID;

import lar.minecraft.hg.enums.PlayerClass;

public class PlayerExtra {
	
	private UUID uuid;
	private PlayerClass playerClass = null;
	private boolean lastWinner = false;
	private boolean premium = false;
	
	public PlayerExtra(UUID uuid) {
		this(uuid, false);
	}
	
	public PlayerExtra(UUID uuid, boolean lastWinner) {
		this(uuid, lastWinner, false);
	}
	
	public PlayerExtra(UUID uuid, boolean lastWinner, boolean premium) {
		this.uuid = uuid;
		this.lastWinner = lastWinner;
		this.premium = premium;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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
