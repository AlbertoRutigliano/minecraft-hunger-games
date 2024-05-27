package lar.minecraft.hg.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lar.minecraft.hg.entity.PlayerClass;

public class PlayerClassManager {
	
	public static final Map<String, PlayerClass> AVAILABLE_PLAYER_CLASSES = new HashMap<>();
	
	public void initPlayerClasses() {
		
		AVAILABLE_PLAYER_CLASSES.put("bowman", new PlayerClass("bowman", Sound.ITEM_CROSSBOW_HIT, this::giveBowmanItems));
		AVAILABLE_PLAYER_CLASSES.put("armored", new PlayerClass("armored", Sound.ITEM_ARMOR_EQUIP_IRON, this::giveArmoredItems));
		AVAILABLE_PLAYER_CLASSES.put("doglover", new PlayerClass("doglover", Sound.ENTITY_WOLF_PANT, this::giveDogloverItems));
		AVAILABLE_PLAYER_CLASSES.put("lavaman", new PlayerClass("lavaman", Sound.ITEM_BUCKET_FILL_LAVA, this::giveLavamanItems));

	}
	
	private void giveBowmanItems(Player player) {
        player.getInventory().addItem(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16));
    }
	
	private void giveArmoredItems(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setHelmet(new ItemStack(Material.IRON_HELMET));
        playerInventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        playerInventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        playerInventory.setBoots(new ItemStack(Material.IRON_BOOTS));
    }

    private void giveDogloverItems(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.addItem(new ItemStack(Material.BONE, 8));
        Wolf dog = player.getWorld().spawn(player.getLocation(), Wolf.class);
        dog.setOwner(player);
        dog.setAdult();
        dog.setSitting(false);
    }

    private void giveLavamanItems(Player player) {
        player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET));
    }
	
}
