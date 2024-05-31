package lar.minecraft.hg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import lar.minecraft.hg.enums.MessageKey;

public class MessageUtils {

	private static FileConfiguration languageConfig;
	private static Logger logger;
	
    public static void init() {       
    	logger = SpigotPlugin.getPlugin(SpigotPlugin.class).getLogger();
        // Load the language file
        File languageFile = new File(SpigotPlugin.getPlugin(SpigotPlugin.class).getDataFolder(), "messages.yml");
        if (!languageFile.exists()) {
            try (InputStream in = SpigotPlugin.getPlugin(SpigotPlugin.class).getResource("messages.yml")) {
			    if (in != null) {
			        Files.copy(in, languageFile.toPath());
			        logger.info("Default messages.yml has been created.");
			    } else {
			    	logger.warning("Default messages.yml not found in the JAR.");
			    }
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not create messages.yml", e);
			}
        }
        
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public static String getMessage(MessageKey key, Object... placeholders) {
    	return getMessage(key.toString(), placeholders);
    }
    // Method to get a message from the language file and replace placeholders by position
    public static String getMessage(String key, Object... placeholders) {
        if (languageConfig == null || !languageConfig.contains(key)) {
            logger.warning("Message key '" + key + "' not found in language file.");
            return "Message not found for key: " + key;
        }

        String message;
        if (languageConfig.isList(key)) {
            // Handle case where message is a list and pick a random message
            List<String> messages = languageConfig.getStringList(key);
            if (messages.isEmpty()) {
                logger.warning("Message key '" + key + "' contains no messages.");
                return "Message not found for key: " + key;
            }
            message = messages.get(new Random().nextInt(messages.size()));
        } else {
            // Handle case where message is a single string
            message = languageConfig.getString(key);
        }

        // Replace placeholders in the message
        for (int i = 0; i < placeholders.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(placeholders[i]));
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
