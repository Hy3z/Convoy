package fr.convoyteam.convoy.weapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.convoyteam.convoy.BaseWeapon;
import fr.convoyteam.convoy.Main;
import fr.convoyteam.convoy.enums.Team;
import fr.convoyteam.convoy.enums.WeaponList;

public class ButterflyKnife extends BaseWeapon {


	public ButterflyKnife(Main main, Player own, Team ownT, WeaponList list) {
		super(main, own, ownT, list);
	}

	@Override
	protected ItemStack makeTheGun() {
		ItemStack itm = new ItemStack(Material.IRON_SWORD);
		ItemMeta meta = itm.getItemMeta();
		meta.setDisplayName(ChatColor.RED+getMMainRef().getText(getOwner(), "weapons.butterflyknife.name"));
		return null;
	}
}
