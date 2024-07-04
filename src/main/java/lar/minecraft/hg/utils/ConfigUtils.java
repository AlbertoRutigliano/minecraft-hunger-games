package lar.minecraft.hg.utils;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import lar.minecraft.hg.enums.ConfigProperty;

public class ConfigUtils {
	
	private static FileConfiguration config;
	
	public static FileConfiguration getConfig() {
		return config;
	}

	public static void setConfig(FileConfiguration config) {
		ConfigUtils.config = config;
	}

	public static Integer getInt(ConfigProperty property) {
		return config.getInt(property.getKey(), (Integer) property.getDefaultValue());
	}
	
	public static Boolean getBoolean(ConfigProperty property) {
		return config.getBoolean(property.getKey(), (Boolean) property.getDefaultValue());
	}
	
	public static String getString(ConfigProperty property) {
		return config.getString(property.getKey(), (String) property.getDefaultValue());
	}
	
	public static List<?> getList(ConfigProperty property) {
		return config.getList(property.getKey(), (List<?>) property.getDefaultValue());
	}
}
