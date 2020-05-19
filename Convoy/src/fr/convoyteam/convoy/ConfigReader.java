package fr.convoyteam.convoy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import fr.convoyteam.convoy.weapons.BasePistol;

public class ConfigReader {
	private Main mainref;
	private File mapFolder;
	private File pistolFolder;
	public ConfigReader(Main _mainref) {
		mainref=_mainref;
		if(!mainref.getDataFolder().exists()) {
			mainref.getDataFolder().mkdir();
		}
		File _mapFolder = new File(mainref.getDataFolder(),"Maps");
		if (!_mapFolder.exists()){
			_mapFolder.mkdir();
		}
		mapFolder=_mapFolder;
		File _pistolFolder = new File(mainref.getDataFolder(),"Pistols");
		if (!_pistolFolder.exists()){
			_pistolFolder.mkdir();
		}
		pistolFolder=_pistolFolder;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

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
	 * Renvoie la configuration du pistolet
	 * @param pistolName Nom du pistolet (sans le .yml)
	 * @return YamlConfiguration
	 */
	public YamlConfiguration getPistolConfig(String pistolName) {
		File file = new File(pistolFolder,pistolName+".yml");
		if (file.exists()) {
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"Error loading pistol: "+ChatColor.AQUA+pistolName);
				return null;
			}
			return config;
		}
		return null;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

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
	
//---------------------------------------------------------------------------------------------------------------------------------------------
	
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
	
//---------------------------------------------------------------------------------------------------------------------------------------------

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
	 * Pour sauvegarder la configuration du pistolet
	 * @param mapName Nom du pistolet (sans le ".yml")
	 * @param config La configuration du pistolet
	 * @return True si tout s'est bien passé
	 */
	public boolean savePistolConfig(String pistolName, YamlConfiguration config) {
		File f = new File(pistolFolder,pistolName+".yml");
		if (!f.exists()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"File: "+ChatColor.AQUA+pistolName+".yml"+ChatColor.RED+" does not exist");
			return false;
		}else try {
			config.save(f);
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"Error on saving changes on pistol: "+ChatColor.AQUA+pistolName);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

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
	
//---------------------------------------------------------------------------------------------------------------------------------------------

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
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Obtenir la liste des pistoles
	 * @return String[] sans les ".yml"
	 */
	public String[] getPistolList() {
		String[] pistolFiles = pistolFolder.list();
		String[] pistolList = new String[]{};
		for(String pistol : pistolFiles) {
			if(pistol.contains(".yml")) {
				pistolList[pistolList.length]=pistol.replace(".yml", "");
			}
		}
		return pistolList;
	}
	/**
	 * Obtenir le BasePistol du pistolet
	 * @param pistolName Nom du pistolet (sans le ".yml")
	 * @return BasePistol
	 */
	public BasePistol getPistol(String pistolName) {
		YamlConfiguration config = getPistolConfig(pistolName);
		if (config!=null) {
			ArrayList<Object> weaponValues = new ArrayList<Object>();
			byte magazineCapacity = (byte)config.getInt("magazineCapacity");
			weaponValues.add(magazineCapacity);
			String fireMode = config.getString("fireMode");
			weaponValues.add(fireMode);
			float fireRate = (float)config.get("fireRate");
			weaponValues.add(fireRate);
			float precision = (float)config.get("precision");
			weaponValues.add(precision);
			float damage = (float)config.get("damage");
			weaponValues.add(damage);
			float bulletSpeed = (float)config.get("bulletSpeed");
			weaponValues.add(bulletSpeed);
			float firingSlow = (float)config.get("firingSlow");
			weaponValues.add(firingSlow);
			float zoomPower = (float)config.get("zoomPower");
			weaponValues.add(zoomPower);
			float carrySlow = (float)config.get("carrySlow");
			weaponValues.add(carrySlow);
			if(!weaponValues.contains(null)) {
				return new BasePistol(magazineCapacity, fireMode, fireRate, damage, precision, bulletSpeed, firingSlow, zoomPower, carrySlow);
			}
		}
		return null;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------

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
	
//---------------------------------------------------------------------------------------------------------------------------------------------

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
	
//---------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Renvoie le block du départ de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return Block
	 */
	public Location getCartStart(String mapName){
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			return config.getLocation("cartStart");
		}
		return null;
	}
	/**
	 * Change la location du block de départ de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @param block Le nouveau block de départ
	 * @return True si tout s'est bien passé
	 */
	public boolean setCartStart(String mapName, Location location) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			config.set("cartStart", location);
			saveMapConfig(mapName, config);
			return true;
		}
		return false;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Renvoie le block de fin de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return Block
	 */
	public Location getCartEnd(String mapName){
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			return config.getLocation("cartEnd");
		}
		return null;
	}
	/**
	 * Change la location du block de fin de la charge
	 * @param mapName Nom de la map (sans le ".yml")
	 * @param block Le nouveau block de fin
	 * @return True si tout s'est bien passé
	 */
	public boolean setCartEnd(String mapName, Location location) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			config.set("cartEnd", location);
			saveMapConfig(mapName, config);
			return true;
		}
		return false;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Met l'information du nombre de rails (pour le pourcentage)
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return True si tout s'est bien passé
	 */
	public boolean setTrackLenght(String mapName) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			Location startLocation = getCartStart(mapName);
			Location endLocation = getCartEnd(mapName);
			if(startLocation!=null&&endLocation!=null) {
				int railLenght = calculateTrackLenght(startLocation,endLocation);
				config.set("trackLenght", railLenght);
				saveMapConfig(mapName, config);
				return true;
			}
		}
		return false;
	}
	/**
	 * Renvoie le nombre de rail de la map
	 * @param mapName Nom de la map (sans le ".yml")
	 * @return -1 si erreur
	 */
	public int getTrackLenght(String mapName) {
		YamlConfiguration config = getMapConfig(mapName);
		if (config!=null) {
			return config.getInt("trackLenght");
		}
		return -1;
	}
	
//---------------------------------------------------------------------------------------------------------------------------------------------
	
	private int calculateTrackLenght(Location startLocation, Location endLocation) {
		World world = startLocation.getWorld();
		int railCount=0;
		Location newLocation=null;
		Location currentLocation=startLocation;
		Location previousLocation=null;
		while(currentLocation.getBlock()!=endLocation.getBlock()) {
			if(railCount!=0) {
				newLocation = getNewLocation(world, currentLocation, previousLocation);
			}else {
				newLocation = getSecondLocation(world, currentLocation);
			}
			previousLocation = currentLocation;
			currentLocation = newLocation;
			railCount++;
		}
		return railCount;
	}
	
	private Location getNewLocation(World world, Location currentLocation, Location previousLocation) {
		Shape railShape = ((Rail)currentLocation.getBlock().getBlockData()).getShape();
		switch(railShape) {
		case ASCENDING_EAST:
			return ascendingRail(currentLocation, previousLocation, new Vector(1,1,0));
		case ASCENDING_NORTH:
			return ascendingRail(currentLocation, previousLocation, new Vector(0,1,-1));
		case ASCENDING_SOUTH:
			return ascendingRail(currentLocation, previousLocation, new Vector(0,1,1));
		case ASCENDING_WEST:
			return ascendingRail(currentLocation, previousLocation, new Vector(-1,1,0));
		case EAST_WEST:
			return straightRail(currentLocation, previousLocation, new Vector(-1,0,0));
		case NORTH_SOUTH:
			return straightRail(currentLocation, previousLocation, new Vector(0,0,1));	
		case NORTH_EAST:
			return (turningRail(currentLocation, previousLocation, new Vector(1,0,0), new Vector(0,0,-1)));
		case NORTH_WEST:
			return (turningRail(currentLocation, previousLocation, new Vector(-1,0,0), new Vector(0,0,-1)));
		case SOUTH_EAST:
			return (turningRail(currentLocation, previousLocation, new Vector(1,0,0), new Vector(0,0,1)));
		case SOUTH_WEST:
			return (turningRail(currentLocation, previousLocation, new Vector(-1,0,0), new Vector(0,0,1)));
		}
		return null;
	}
	private boolean isNewRail(Location currentLocation, Location previousLocation, Vector vector) {
		if(currentLocation.add(vector).getBlock()!=previousLocation.getBlock()){
			return true;
		}
		return false;
	}
	private Location straightRail(Location currentLocation, Location previousLocation, Vector vector) {
		if(isNewRail(currentLocation, previousLocation, vector)) {
			return currentLocation.add(vector);
		}
		return currentLocation.subtract(vector);
	}
	private Location ascendingRail(Location currentLocation, Location previousLocation, Vector vector) {
		if(isNewRail(currentLocation, previousLocation, vector)) {
			return currentLocation.add(vector);
		}
		if(isNewRail(currentLocation, previousLocation, vector.multiply(-1))) {
			return currentLocation.subtract(vector);
		}
		return currentLocation.subtract(vector.setY(0));
	}
	private Location turningRail(Location currentLocation, Location previousLocation, Vector vector1, Vector vector2) {
		if(isNewRail(currentLocation, previousLocation, vector1)) {
			return currentLocation.add(vector1);
		}
		return currentLocation.add(vector2);
	}
	private Location getSecondLocation(World world, Location startLocation) {
		Shape railShape = ((Rail)startLocation.getBlock().getBlockData()).getShape();
		ArrayList<Material> railList = new ArrayList<Material>();
		railList.add(Material.RAIL);
		railList.add(Material.ACTIVATOR_RAIL);
		railList.add(Material.DETECTOR_RAIL);
		railList.add(Material.POWERED_RAIL);
		switch(railShape) {
		case NORTH_SOUTH:
			if(railList.contains(world.getBlockAt(startLocation.add(0, 0, -1)).getType())) {
				return startLocation.add(0,0,-1);
			}
			return startLocation.add(0,0,1);
				//
		case EAST_WEST:
			if(railList.contains(world.getBlockAt(startLocation.add(1, 0, 0)).getType())) {
				return startLocation.add(1,0,0);
			}
				return startLocation.add(-1,0,0);
		default:
			return null;
		}
	}
}
