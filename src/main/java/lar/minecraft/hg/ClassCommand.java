package lar.minecraft.hg;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class ClassCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmdName = command.getName().toLowerCase();
		Player player = null;
		PlayerInventory playerInventory = null;
		
		if (sender instanceof Player) {
			 player = (Player) sender;
			 playerInventory = player.getInventory();
		}
		
		if (SpigotPlugin.isLobby()) {
			playerInventory.clear();
			player.sendMessage("Giving " + cmdName + " items");
			switch (cmdName) {
				case "bowman":
					playerInventory.setItemInMainHand(new ItemStack(Material.BOW));
					//playerInventory.addItem(new ItemStack(Material.ARROW, 16), new ItemStack(Material.SNOWBALL, 16));
					playerInventory.addItem(new ItemStack(Material.ARROW, 16));
					break;
				case "armored":
					playerInventory.setHelmet(new ItemStack(Material.IRON_HELMET));
					playerInventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
					playerInventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
					playerInventory.setBoots(new ItemStack(Material.IRON_BOOTS));
					break;
				default:
					break;
			}
		} else {
			player.sendMessage("Class selection is available only in LOBBY");
		}
		 
		return true;
	}

}
