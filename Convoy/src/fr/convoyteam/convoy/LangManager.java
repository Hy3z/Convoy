package fr.convoyteam.convoy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LangManager {
	
	private static final String DEFAULT_LANGUAGE = "en_GB";
	private final WeakHashMap<Player,String> playerLanguage = new WeakHashMap<Player,String>();
	private final Main mainref;
	private final File langFolder;
	
	public LangManager(Main main) {
		mainref=main;
		if(!mainref.getDataFolder().exists()) {
			mainref.getDataFolder().mkdir();
		}
		File folder = new File(mainref.getDataFolder(),"Lang");
		if (!folder.exists()){
			folder.mkdir();
		}
		langFolder=folder;
	}
	
	//INTERNET
	public String getLanguage(Player p){
		try {
			Object ep = getMethod("getHandle", p.getClass()).invoke(p, (Object[]) null);
			Field f = ep.getClass().getDeclaredField("locale");
			f.setAccessible(true);
			String language = (String) f.get(ep);
			return language;
		}catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return DEFAULT_LANGUAGE;
	}
	
	private Method getMethod(String name, Class<?> clazz) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name)) return m;
		}
		return null;
	}
	public void add(Player p) {
		playerLanguage.put(p,getLanguage(p));
	}
	public String get(Player p , String path) {
		File file = new File(langFolder,playerLanguage.get(p)+".yml");
		if (file.exists()) {
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(file);
				return config.getString(path,"");
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}else {
			file = new File(langFolder,DEFAULT_LANGUAGE+".yml");
			if (file.exists()) {
				YamlConfiguration config = new YamlConfiguration();
				try {
					config.load(file);
					return config.getString(path,"");
				} catch (IOException | InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}
}