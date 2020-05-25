package fr.convoyteam.convoy;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.convoyteam.convoy.enums.Team;
import fr.convoyteam.convoy.enums.WeaponList;

public abstract class BaseWeapon implements Listener {
	
	private final WeaponList weaponInfoRef;
	private final ItemStack weaponRef;
	private final Player owner;
	private final Team ownerTeam;
	private final Main mainref;
	
	protected abstract ItemStack makeTheGun();
	
	public BaseWeapon(Main main ,Player own, Team ownT, WeaponList list) {
		this.owner = own;
		this.ownerTeam = ownT;
		this.weaponInfoRef = list;
		this.weaponRef = makeTheGun();
		this.mainref=main;
		main.RegisterListener(this);
	}
	
	private void unregisterEvents() {
		HandlerList.unregisterAll(this);
	}
	
	public void remove() {
		owner.getInventory().remove(weaponRef);
		unregisterEvents();
	}
	
	//
	//
	//GETTERS/SETTERS
	//
	//
	
	public ItemStack getWeaponRef() {
		return weaponRef;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public Team getOwnerTeam() {
		return ownerTeam;
	}
	
	public WeaponList getWeaponListRef() {
		return weaponInfoRef;
	}
	
	public Main getMMainRef() {
		return mainref;
	}
	
}
