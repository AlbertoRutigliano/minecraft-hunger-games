package lar.minecraft.hg.enums;

public enum HGPhase {
	WAITING,
	LOBBY,
	SAFE_AREA,
	PLAYING,
	WINNING,
	ENDED;
	
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
