package lar.minecraft.hg.enums;

import java.util.HashMap;
import java.util.Map;

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
	miner (Sound.ENTITY_ITEM_BREAK, "Dig in, my friend!", false) {
		@Override
		public PlayerAction getAction() {
			return (player) -> {
            	player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE), new ItemStack(Material.TORCH, 8));
            };
		}

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put(Material.STONE_PICKAXE.toString(), 1);
			result.put(Material.TORCH.toString(), 8);
			return result;
		}
	},
    bowman (Sound.ITEM_CROSSBOW_HIT, "Shot other players with a bow and some arrows", false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
            	player.getInventory().addItem(new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16));
            };
        }

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put(Material.BOW.toString(), 1);
			result.put(Material.ARROW.toString(), 16);
			return result;
		}
    },
    armored (Sound.ITEM_ARMOR_EQUIP_LEATHER, "Cover yourself with a full leather armor set", true) {
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

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put("LEATHER ARMOR SET", 1);
			return result;
		}
    },
    doglover (Sound.ENTITY_WOLF_PANT, "Tame the nature and kill other players with your dog", false) {
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

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put("TAMED DOG", 1);
			result.put(Material.BONE.toString(), 8);
			return result;
		}
    },
    lavaman (Sound.ITEM_BUCKET_FILL_LAVA, "Burn BURN BURN!!!", true) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(
                		new ItemStack(Material.LAVA_BUCKET), new ItemStack(Material.LAVA_BUCKET));
            };
        }

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put(Material.LAVA_BUCKET.toString(), 2);
			return result;
		}
    },
    mole (Sound.BLOCK_ROOTED_DIRT_PLACE, "Dig out, my friend!", false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(new ItemStack(Material.DIRT, 32));
            };
        }

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put(Material.DIRT.toString(), 32);
			return result;
		}
    },
    ghost (Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, "Let's get invisible", true) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                ItemStack potion = new ItemStack(Material.POTION, 2);
                PotionMeta meta = (PotionMeta) potion.getItemMeta();
                meta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 1), true); // 600 ticks = 30 seconds
                meta.setDisplayName("Invisibility Potion");
                potion.setItemMeta(meta);
                player.getInventory().addItem(potion);
            };
        }

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put("INVISIBILITY POTION", 2);
			return result;
		}
    },
    teleporter (Sound.ENTITY_ENDERMAN_TELEPORT, "I'm here... not anymore", false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 3));
            };
        }

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put(Material.ENDER_PEARL.toString(), 3);
			return result;
		}
    },
    barbarian (Sound.ENTITY_PLAYER_ATTACK_SWEEP, "WWOOWOWOW", false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
                player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
            };
        }

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put(Material.STONE_SWORD.toString(), 1);
			return result;
		}
    },
    hardcore (Sound.ENTITY_PLAYER_BURP, "Are you crazy!?!?", false) {
        @Override
        public PlayerAction getAction() {
            return (player) -> {
            	PlayerInventory playerInventory = player.getInventory();
                playerInventory.setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
            };
        }

		@Override
		public Map<String, Integer> getMaterials() {
			Map<String, Integer> result = new HashMap<String, Integer>();
			result.put(Material.CARVED_PUMPKIN.toString(), 1);
			return result;
		}
    };

    private Sound sound = Sound.INTENTIONALLY_EMPTY;
    private String description = "";
    private boolean premium = false;

    PlayerClass(Sound sound, String description, boolean premium) {
        this.sound = sound;
        this.description = description;
        this.premium = premium;
    }
    
    PlayerClass() {}

    public Sound getSound() {
        return sound;
    }
    
    public void setSound(Sound sound) {
        this.sound = sound;
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
    abstract public Map<String, Integer> getMaterials();

}
