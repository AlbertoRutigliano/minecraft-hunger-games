package lar.minecraft.hg.managers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import lar.minecraft.hg.PlayerAction;
import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.entity.PlayerClass;
import lar.minecraft.hg.entity.PlayerExtra;
import lar.minecraft.hg.enums.PlayerClassEnum;

public class PlayerClassManager {
	
	public static final Map<PlayerClassEnum, PlayerClass> AVAILABLE_PLAYER_CLASSES = new HashMap<>();
	
	/**
	 * To add a new Player Class, you need:
	 * - 1. add the new command into plugin.yml file
	 * - 2. add a new PlayerClassEnum item into PlayerClassEnum ENUM
	 * - 3. in initPlayerClasses method here, add a new putPlayerClass method call with the new created PlayerClassEnum and his respective Sound as parameters
	 * - 4. then create in PlayerClassManager a PlayerClassEnum_ACTION method (such as BOWMAN_ACTION())
	 */
	public void initPlayerClasses() {
		putPlayerClass(PlayerClassEnum.BOWMAN, Sound.ITEM_CROSSBOW_HIT);
		putPlayerClass(PlayerClassEnum.ARMORED, Sound.ITEM_ARMOR_EQUIP_IRON);
		putPlayerClass(PlayerClassEnum.DOGLOVER, Sound.ENTITY_WOLF_PANT);
		putPlayerClass(PlayerClassEnum.LAVAMAN, Sound.ITEM_BUCKET_FILL_LAVA);
	}
	
	private void putPlayerClass(PlayerClassEnum className, Sound classSound) {
		AVAILABLE_PLAYER_CLASSES.put(className, new PlayerClass(className, classSound, getPlayerAction(className)));
	}
	
	private PlayerAction getPlayerAction(PlayerClassEnum className) {
	    String methodName = className.name().toUpperCase() + "_ACTION";
	    try {
	        Method method = this.getClass().getDeclaredMethod(methodName, Player.class);
	        return (PlayerAction) player -> {
	            try {
	                method.invoke(this, player);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        };
	    } catch (NoSuchMethodException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	public static void giveClasses() {
		for(Player player : ServerManager.getLivingPlayers()) {
			PlayerInventory playerInventory = player.getInventory();
			playerInventory.clear();
			playerInventory.addItem(new ItemStack(Material.COMPASS)); // Give a compass to all players
			PlayerExtra playerExtra = SpigotPlugin.playerExtras.getOrDefault(player.getUniqueId(), null);
			if (playerExtra != null && PlayerClassManager.AVAILABLE_PLAYER_CLASSES.containsKey(playerExtra.getPlayerClass().getName())) {
				playerExtra.getPlayerClass().getAction().perform(player); // Give class items to players who chosed a class
			}
			player.updateInventory();
		}
	}
	
	@SuppressWarnings("unused")
	private void BOWMAN_ACTION(Player player) {
        player.getInventory().addItem(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16));
    }
	
	@SuppressWarnings("unused")
	private void ARMORED_ACTION(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.setHelmet(new ItemStack(Material.IRON_HELMET));
        playerInventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        playerInventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        playerInventory.setBoots(new ItemStack(Material.IRON_BOOTS));
    }

	@SuppressWarnings("unused")
    private void DOGLOVER_ACTION(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        playerInventory.addItem(new ItemStack(Material.BONE, 8));
        Wolf dog = player.getWorld().spawn(player.getLocation(), Wolf.class);
        dog.setOwner(player);
        dog.setAdult();
        dog.setSitting(false);
    }

	@SuppressWarnings("unused")
    private void LAVAMAN_ACTION(Player player) {
        player.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET));
    }
	
}
