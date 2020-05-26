package fr.convoyteam.convoy;

import java.util.WeakHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class InterfaceManager implements Listener {
	private final WeakHashMap<Player,Inventory> playersWeaponMenu = new WeakHashMap<Player,Inventory>();
	

}