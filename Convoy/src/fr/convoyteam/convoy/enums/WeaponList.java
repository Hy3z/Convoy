package fr.convoyteam.convoy.enums;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.convoyteam.convoy.BaseWeapon;
import fr.convoyteam.convoy.Main;
import fr.convoyteam.convoy.weapons.*;

public enum WeaponList {
	
	butterflyKnife(makeMenuItem(Material.IRON_SWORD,ChatColor.GOLD+"Couteau Papillon"/*Je sait pas comment utiliser notre traducteur ici*/)
			,ButterflyKnife.class
			,WeaponLevel.PRIMAIRE);
	
	private final ItemStack menuItem;
	private final Class<? extends BaseWeapon> classRef;
	private final WeaponLevel level;
	
	private WeaponList(ItemStack menu, Class<? extends BaseWeapon> clazz,WeaponLevel lvl) {
		menuItem = menu;
		classRef=clazz;
		level=lvl;
	}
	
	private static ItemStack makeMenuItem(Material mat, String name, String... lore) {
		ItemStack itm = new ItemStack(mat);
		ItemMeta meta = itm.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(Arrays.asList(lore));
		itm.setItemMeta(meta);
		return itm;
	}
	
	private ItemStack getMenuItem() {
		return menuItem;
	}
	
	public BaseWeapon makeWeapon(Main main,Player p,Team team) {
		try {
			return classRef.getDeclaredConstructor(new Class[] {Main.class,Player.class,Team.class,WeaponList.class}).newInstance(main,p,team,this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public WeaponLevel getLevel() {
		return level;
	}
	
}
