package lar.minecraft.hg.entities;

import java.util.UUID;

import lar.minecraft.hg.enums.PlayerClass;

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
