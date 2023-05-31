package lar.minecraft.hg;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExampleCommand implements CommandExecutor {
    SpigotPlugin plugin;

    public ExampleCommand(SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        if (cmdName.equals("lobby")) {
        	sender.sendMessage("Entering in lobby mode");
        	for (Player p : SpigotPlugin.server.getOnlinePlayers()) {
        		p.setGameMode(GameMode.ADVENTURE);
        	}
        }
        
        if (cmdName.equals("nolobby")) {
        	sender.sendMessage("Entering in survival mode");
        	for (Player p : SpigotPlugin.server.getOnlinePlayers()) {
        		p.setGameMode(GameMode.SURVIVAL);
        	}
        }

        return true;
    }
}
