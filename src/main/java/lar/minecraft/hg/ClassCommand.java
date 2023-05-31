package lar.minecraft.hg;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ClassCommand implements CommandExecutor {
	
	 SpigotPlugin plugin;

	    public ClassCommand(SpigotPlugin plugin) {
	        this.plugin = plugin;
	    }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		 String cmdName = command.getName().toLowerCase();
		 if (cmdName.equals("bowman")) {
			 if (sender instanceof Player) {
				 Player player = (Player) sender;
				 player.sendMessage("Giving bowman items");
				 PlayerInventory inventory = player.getInventory();
				 inventory.clear();
				 inventory.setItemInMainHand(new ItemStack(Material.BOW));
				 inventory.addItem(new ItemStack(Material.ARROW, 16), new ItemStack(Material.SNOWBALL, 16));
			 }
		 }
		 if (cmdName.equals("armored")) {
			 if (sender instanceof Player) {
				 Player player = (Player) sender;
				 player.sendMessage("Giving armor items");
				 PlayerInventory inventory = player.getInventory();
				 inventory.clear();
				 inventory.setHelmet(new ItemStack(Material.IRON_HELMET));
				 inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
				 inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
				 inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
			 }
		 }
		return true;
	}

}
