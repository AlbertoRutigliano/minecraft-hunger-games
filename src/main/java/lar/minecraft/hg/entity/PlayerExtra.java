package lar.minecraft.hg.entity;

import java.util.UUID;

public class PlayerExtra {
	
	private UUID uuid;
	private PlayerClass playerClass = null;
	
	public PlayerExtra(UUID uuid) {
		this.uuid = uuid;
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
	
	
}
