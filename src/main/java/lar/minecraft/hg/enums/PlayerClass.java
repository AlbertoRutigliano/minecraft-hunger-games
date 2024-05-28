package lar.minecraft.hg.enums;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public enum PlayerClass {
	MINER (Sound.ENTITY_ITEM_BREAK) {
		@Override
		public PlayerAction getAction() {
			return (player) -> {
            	player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE), new ItemStack(Material.TORCH, 8));
            };
		}
	},
    BOWMAN (Sound.ITEM_CROSSBOW_HIT) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
            	player.getInventory().addItem(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16));
            };
        }
    },
    ARMORED (Sound.ITEM_ARMOR_EQUIP_LEATHER) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
            	PlayerInventory playerInventory = player.getInventory();
                playerInventory.setHelmet(new ItemStack(Material.LEATHER_HELMET));
                playerInventory.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                playerInventory.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                playerInventory.setBoots(new ItemStack(Material.LEATHER_BOOTS));
            };
        }
    },
    DOGLOVER (Sound.ENTITY_WOLF_PANT) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
            	player.getInventory().addItem(new ItemStack(Material.BONE, 8));
                
                Wolf dog = player.getWorld().spawn(player.getLocation(), Wolf.class);
                dog.setOwner(player);
                dog.setAdult();
                dog.setSitting(false);
            };
        }
    },
    LAVAMAN (Sound.ITEM_BUCKET_FILL_LAVA) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(
                		new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET));
            };
        }
    };

    private Sound sound = Sound.INTENTIONALLY_EMPTY;

    PlayerClass(Sound sound) {
        this.sound = sound;
    }
    
    PlayerClass() {}

    public Sound getSound() {
        return sound;
    }

    // Define a functional interface for player actions
    public interface PlayerAction {
        void perform(Player player);
    }

    // Abstract method to get the action for each class
    abstract public PlayerAction getAction();
}
