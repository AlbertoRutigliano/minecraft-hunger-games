package lar.minecraft.hg.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lar.minecraft.hg.ServerSchedulers;
import lar.minecraft.hg.SpigotPlugin;
import lar.minecraft.hg.enums.MessageKey;
import lar.minecraft.hg.managers.ServerManager;
import lar.minecraft.hg.utils.MessageUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class TestCommand implements CommandExecutor {
    
	SpigotPlugin plugin;

    public TestCommand(SpigotPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        if (cmdName.equals("test")) {
        	TextComponent message = new TextComponent("PROVOLA");
        	message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/AlbertoRutigliano/minecraft-hunger-games"));
        	message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Hello World")));
        	sender.spigot().sendMessage(message);
        	
        	Player player = (Player) sender;
        	player.spigot().sendMessage(
					ChatMessageType.ACTION_BAR, 
					new TextComponent(MessageUtils.getMessage(MessageKey.supply_drop, 0, 1, 2)));

        }
        
        if (cmdName.equals("restart-hg-server")) {
        	ServerManager.restartServer();
        }
                
        if (cmdName.equals("start-hg")) {
        	new ServerSchedulers(plugin).waitingPhase();
        }
        
        if (cmdName.equals("current-phase")) {
        	sender.sendMessage(MessageUtils.getMessage(MessageKey.current_phase, SpigotPlugin.getPhase()));
        }
        Arrays.asList(MessageKey.values()).forEach( m -> {
        	
        });
        
        if (cmdName.equals("messages")) {
        	Arrays.asList(MessageKey.values()).forEach( m -> {
        		sender.sendMessage(MessageUtils.getMessage(m));
            });
        }

        return true;
    }
    
   
    

}
