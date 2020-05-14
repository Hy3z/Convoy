package fr.convoyteam.convoy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigReader {
	private Main mainref;
	private File mapFolder;
	
	public ConfigReader(Main main) {
		mainref=main;
		if(!mainref.getDataFolder().exists()) {
			mainref.getDataFolder().mkdir();
		}
		File folder = new File(mainref.getDataFolder(),"Maps");
		if (!folder.exists()){
			folder.mkdir();
		}
		mapFolder=folder;
	}
	/**
	 * Renvoie la configuration de la map
	 * @param mapName Nom de la map (sans le .yml)
	 * @return YamlConfiguration
	 */
	public YamlConfiguration getMapConfig(String mapName) {
		File file = new File(mapFolder,mapName+".yml");
		if (file.exists()) {
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"Error loading map: "+ChatColor.AQUA+mapName);
				return null;
			}
			return config;
		}
		return null;
	}
	/**
	 * Renvoie la liste des map
	 * @return ArrayList(String) 
	 */
	public ArrayList<String> getMapList() {
		String[] mapList = mapFolder.list();
		ArrayList<String> withYAML=new ArrayList<String>();
		for (String s : mapList) {
			if (s.contains(".yml")) {
				withYAML.add(s.replace(".yml", ""));
			}
		}
		return withYAML;
	}
	/**
	 * Pour sauvegarder la configuration de la map
	 * @param mapName Nom de la map (sans le ".yml")
	 * @param config La configuration de la map
	 * @return True si tout s'est bien passé
	 */
	public boolean saveMapConfig(String mapName, YamlConfiguration config) {
		File f = new File(mapFolder,mapName+".yml");
		if (!f.exists()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"File: "+ChatColor.AQUA+mapName+".yml"+ChatColor.RED+" does not exist");
			return false;
		}else try {
			config.save(f);
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"Error on saving changes on map: "+ChatColor.AQUA+mapName);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * Crée le fichier ".yml" pour la map
	 * @param mapName Nom de la map (sans le ".yml")
	 */
	public boolean createMapConfig(String mapName) {
		File file = new File(mapFolder,mapName+".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
				return true;
			} catch (IOException e) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"Error on config creation for map: "+ChatColor.AQUA+mapName);
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * Pour supprimer la configuration de la map
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return True si tout s'est bien passé
	 */
	public boolean deleteMapConfig(String mapName){
		File file = new File(mapFolder,mapName+".yml");
		if(file.exists()) {
			file.delete();
			return true;
		}else {
			return false;
		}
	}
	/**
	 * Renomme le fichier ".yml" de la map
	 * @param mapName Actuel nom de la map (sans le ".yml")
	 * @param newName Nouveau nom de la map (sans le ".yml")
	 * @return True si tout s'est bien passé
	 */
	public boolean renameMapConfig(String mapName, String newName){
		File oldFile = new File(mapFolder,mapName+".yml");
		if (oldFile.exists()) {
			File newf = new File(mapFolder,mapName+".yml");
			if (oldFile.renameTo(newf)) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+mapName+".yml"+ChatColor.BLUE+" has been renamed to: "+ChatColor.GREEN+newName+".yml");
				return true;
			}else {
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+mapName+".yml"+ChatColor.BLUE+" was unable to be renamed into: "+ChatColor.GREEN+newName+".yml");
				return false;
			}
		}
		else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"File: "+ChatColor.AQUA+mapName+".yml"+ChatColor.RED+" does not exist");
			return false;
		}
	}
	/**
	 * Renvoie la location du spawn des Defender
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return Location
	 */
	public Location getDefenderSpawn(String mapName){
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			return (Location) config.get("defenderSpawn");
		}
		return null;
	}
	/**
	 * Change la location du spawn des Defender
	 * @param mapName Nom de la map (sans le ".yml")
	 * @param location La location du nouveau point
	 * @return True si tout s'est bien passé
	 */
	public boolean setDefenderSpawn(String mapName, Location location) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			config.set("defenderSpawn", location);
			saveMapConfig(mapName, config);
			return true;
		}
		return false;
	}
	/**
	 * Renvoie la location du spawn des Pusher
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return Location
	 */
	public Location getPusherSpawn(String mapName){
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			return (Location) config.get("pusherSpawn");
		}
		return null;
	}
	/**
	 * Change la location du spawn des Pusher
	 * @param mapName Nom de la map (sans le ".yml")
	 * @param location La location du nouveau point
	 * @return True si tout s'est bien passé
	 */
	public boolean setPusherSpawn(String mapName, Location location) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			config.set("pusherSpawn", location);
			saveMapConfig(mapName, config);
			return true;
		}
		return false;
	}
	/**
	 * Renvoie le block du départ de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return Block
	 */
	public Block getCartStart(String mapName){
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			return (Block) config.get("cartStart");
		}
		return null;
	}
	/**
	 * Change le block du départ de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @param block Le nouveau block de départ
	 * @return True si tout s'est bien passé
	 */
	public boolean setCartStart(String mapName, Block block) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			config.set("cartStart", block);
			saveMapConfig(mapName, config);
			return true;
		}
		return false;
	}
	/**
	 * Renvoie le block de fin de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return Block
	 */
	public Block getCartEnd(String mapName){
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			return (Block) config.get("cartEnd");
		}
		return null;
	}
	/**
	 * Change le block de fin de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @param block Le nouveau block de fin
	 * @return True si tout s'est bien passé
	 */
	public boolean getCartEnd(String mapName, Block block) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			config.set("cartEnd", block);
			saveMapConfig(mapName, config);
			return true;
		}
		return false;
	}
}
