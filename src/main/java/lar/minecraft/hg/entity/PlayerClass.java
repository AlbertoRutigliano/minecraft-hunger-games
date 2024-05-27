package lar.minecraft.hg.entity;

import org.bukkit.Sound;

import lar.minecraft.hg.PlayerAction;
import lar.minecraft.hg.enums.PlayerClassEnum;

public class PlayerClass {

	private PlayerClassEnum name;
	private Sound sound;
	private PlayerAction action;

    public PlayerClass(PlayerClassEnum name, Sound sound, PlayerAction action) {
        this.name = name;
        this.sound = sound;
        this.action = action;
    }

    public PlayerClassEnum getName() {
        return name;
    }

    public void setName(PlayerClassEnum name) {
        this.name = name;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public PlayerAction getAction() {
        return action;
    }

    public void setAction(PlayerAction action) {
        this.action = action;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		PlayerClass other = (PlayerClass) obj;
		if (name != other.name)
			return false;
		return true;
	}

	
}
