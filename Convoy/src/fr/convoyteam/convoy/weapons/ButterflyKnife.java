package fr.convoyteam.convoy.weapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
		meta.setDisplayName(ChatColor.RED+getMainRef().getText(getOwner(), "weapons.butterflyknife.name"));
		return null;
	}
	
	@EventHandler
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		Entity atker = event.getDamager();
		if (atker.equals(getOwner()) && victim instanceof Damageable && getMainRef().getInGamePlayers().contains(victim)) {
			Damageable victimd = (Damageable)victim;
			boolean goodItem  = getOwner().getInventory().getItemInMainHand().isSimilar(getWeaponRef());
			boolean def = getOwnerTeam()==Team.DEFENDERS && getMainRef().getPushers().contains(victim);
			boolean atk = getOwnerTeam()==Team.PUSHERS && getMainRef().getDefenders().contains(victim);
			boolean goodAngle = Math.abs(((Player)victim).getEyeLocation().getYaw())<=45;
			if ((def || atk) && goodAngle && goodItem) {
				victimd.damage(victimd.getHealth(), atker);
			}
		}
	}
}
