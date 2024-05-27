package lar.minecraft.hg;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerAction {
    void perform(Player player);
}