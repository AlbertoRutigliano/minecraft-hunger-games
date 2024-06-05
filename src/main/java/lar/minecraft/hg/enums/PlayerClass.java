package lar.minecraft.hg.enums;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum PlayerClass {
	miner (Sound.ENTITY_ITEM_BREAK, false) {
		@Override
		public PlayerAction getAction() {
			return (player) -> {
            	player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE), new ItemStack(Material.TORCH, 8));
            };
		}
	},
    bowman (Sound.ITEM_CROSSBOW_HIT, false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
            	player.getInventory().addItem(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16));
            };
        }
    },
    armored (Sound.ITEM_ARMOR_EQUIP_LEATHER, true) {
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
    doglover (Sound.ENTITY_WOLF_PANT, false) {
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
    lavaman (Sound.ITEM_BUCKET_FILL_LAVA, true) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(
                		new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET));
            };
        }
    },
    mole (Sound.BLOCK_ROOTED_DIRT_PLACE, false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(new ItemStack(Material.DIRT, 32));
            };
        }
    },
    ghost (Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, true) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                ItemStack potion = new ItemStack(Material.POTION, 2);
                PotionMeta meta = (PotionMeta) potion.getItemMeta();
                meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1), true); // 600 ticks = 30 seconds
                meta.setDisplayName("Potion of Invisibility");
                potion.setItemMeta(meta);
                player.getInventory().addItem(potion);
            };
        }
    },
    teleporter (Sound.ENTITY_ENDERMAN_TELEPORT, false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 3));
            };
        }
    },
    barbarian (Sound.ENTITY_PLAYER_ATTACK_SWEEP, false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
            };
        }
    },
    hardcore (Sound.ENTITY_PLAYER_BURP, false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
            	PlayerInventory playerInventory = player.getInventory();
                playerInventory.setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
            };
        }
    };

    private Sound sound = Sound.INTENTIONALLY_EMPTY;
    private boolean premium = false;

    PlayerClass(Sound sound, boolean premium) {
        this.sound = sound;
        this.premium = premium;
    }
    
    PlayerClass() {}

    public Sound getSound() {
        return sound;
    }
    
    public void setSound(Sound sound) {
        this.sound = sound;
    }
    
	public boolean isPremium() {
		return premium;
	}

	public void setPremium(boolean premium) {
		this.premium = premium;
	}

    // Define a functional interface for player actions
    public interface PlayerAction {
        void perform(Player player);
    }

    // Abstract method to get the action for each class
    abstract public PlayerAction getAction();

}
